package net.deckserver.services;

import io.azam.ulidj.ULID;
import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.RefreshTokenRepository;
import net.deckserver.storage.json.system.RefreshTokenInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Persists "remember me" refresh tokens, one row per logged-in device. A refresh
 * cookie value is "{id}.{secret}": the id gives O(1) lookup, the secret is what's
 * hashed and compared (see Barry Jaspan's persistent-login-cookie pattern). On
 * every use the secret is rotated; a presented secret that no longer matches the
 * stored hash means an old, already-rotated-away token was replayed, so the row
 * is revoked defensively.
 */
@Singleton
@Startup
public class RefreshTokenService extends PersistedService {

    private static final RefreshTokenRepository refreshTokenRepository = new RefreshTokenRepository();
    private static RefreshTokenService instance() {
        return resolve(RefreshTokenService.class, RefreshTokenService::new);
    }
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TTL_REMEMBER_MILLIS = TimeUnit.DAYS.toMillis(30);
    private static final long TTL_SESSION_MILLIS = TimeUnit.HOURS.toMillis(12);
    private static final long ABSOLUTE_MAX_AGE_MILLIS = TimeUnit.DAYS.toMillis(90);

    private final Map<String, List<RefreshTokenInfo>> tokensByPlayer = new HashMap<>();

    /**
     * Tracks the most recent rotation per token id, purely in memory (never persisted —
     * losing this on restart just narrows the grace window back to zero, which is safe).
     * See {@link #validateAndRotate} for why this exists: without it, a burst of ordinary
     * concurrent requests (e.g. GamePage's game-view query, NavContext's poll, and the WS
     * handshake all silently refreshing at once) that all present the *same*
     * not-yet-rotated cookie would have every request after the first treated as a replayed/
     * stolen token and the row deleted outright — logging the player out for no actual
     * security reason.
     */
    private final Map<String, RecentRotation> recentRotations = new HashMap<>();

    private static final long ROTATION_GRACE_MILLIS = TimeUnit.SECONDS.toMillis(10);

    private record RecentRotation(String previousSecretHash, Rotated rotated, long rotatedAtMillis) {
    }

    RefreshTokenService() {
        super("RefreshTokenService", 0);
    }

    public static PersistedService getInstance() {
        return instance();
    }

    public record Issued(String cookieValue, String id) {
    }

    public static synchronized Issued issue(String playerName, String deviceLabel, boolean remember) {
        String id = ULID.random();
        String secret = randomSecret();
        long now = System.currentTimeMillis();
        long ttl = remember ? TTL_REMEMBER_MILLIS : TTL_SESSION_MILLIS;

        RefreshTokenInfo info = new RefreshTokenInfo();
        info.setId(id);
        info.setPlayerName(playerName);
        info.setSecretHash(hash(secret));
        info.setDeviceLabel(deviceLabel);
        info.setCreatedAt(now);
        info.setLastUsedAt(now);
        info.setExpiresAt(now + ttl);
        info.setRemember(remember);

        instance().jpaWriteThenMutate(
                em -> refreshTokenRepository.save(em, playerName, info),
                () -> instance().tokensByPlayer.computeIfAbsent(playerName, k -> new ArrayList<>()).add(info));
        return new Issued(id + "." + secret, id);
    }

    public record Rotated(String playerName, String cookieValue, boolean remember) {
    }

    public static synchronized Optional<Rotated> validateAndRotate(String cookieValue) {
        if (cookieValue == null) return Optional.empty();
        int dot = cookieValue.indexOf('.');
        if (dot < 0) return Optional.empty();
        String id = cookieValue.substring(0, dot);
        String secret = cookieValue.substring(dot + 1);
        long now = System.currentTimeMillis();

        // A concurrent request that started before this cookie was rotated (by another
        // request racing it) still presents the pre-rotation secret. Rather than treating
        // that as a replayed/stolen token, hand back the same rotation the winning request
        // already produced, as long as it happened recently enough to plausibly be the same
        // race rather than an actual stale/stolen cookie surfacing much later.
        RecentRotation recent = instance().recentRotations.get(id);
        if (recent != null && now - recent.rotatedAtMillis() <= ROTATION_GRACE_MILLIS
                && matchesHash(secret, recent.previousSecretHash())) {
            return Optional.of(recent.rotated());
        }

        for (List<RefreshTokenInfo> tokens : instance().tokensByPlayer.values()) {
            for (RefreshTokenInfo info : tokens) {
                if (!info.getId().equals(id)) continue;

                if (info.getExpiresAt() < now || !matchesHash(secret, info.getSecretHash())) {
                    instance().jpaWriteThenMutate(
                            em -> refreshTokenRepository.delete(em, id),
                            () -> tokens.remove(info));
                    instance().recentRotations.remove(id);
                    return Optional.empty();
                }

                String previousSecretHash = info.getSecretHash();
                long previousLastUsedAt = info.getLastUsedAt();
                long previousExpiresAt = info.getExpiresAt();

                String newSecret = randomSecret();
                long newExpiresAt = Math.min(now + (info.isRemember() ? TTL_REMEMBER_MILLIS : TTL_SESSION_MILLIS),
                        info.getCreatedAt() + ABSOLUTE_MAX_AGE_MILLIS);

                instance().jpaWriteWithRollback(
                        () -> {
                            info.setSecretHash(hash(newSecret));
                            info.setLastUsedAt(now);
                            info.setExpiresAt(newExpiresAt);
                        },
                        em -> refreshTokenRepository.save(em, info.getPlayerName(), info),
                        () -> {
                            info.setSecretHash(previousSecretHash);
                            info.setLastUsedAt(previousLastUsedAt);
                            info.setExpiresAt(previousExpiresAt);
                        });

                Rotated rotated = new Rotated(info.getPlayerName(), id + "." + newSecret, info.isRemember());
                instance().recentRotations.put(id, new RecentRotation(previousSecretHash, rotated, now));
                return Optional.of(rotated);
            }
        }
        return Optional.empty();
    }

    private static boolean matchesHash(String secret, String expectedHash) {
        return MessageDigest.isEqual(
                HexFormat.of().parseHex(expectedHash),
                HexFormat.of().parseHex(hash(secret)));
    }

    /** Test-only seam: simulates the rotation grace window having elapsed for a token id. */
    static synchronized void expireRotationGraceForTest(String id) {
        instance().recentRotations.remove(id);
    }

    public static synchronized void revoke(String cookieValue) {
        if (cookieValue == null) return;
        int dot = cookieValue.indexOf('.');
        String id = dot < 0 ? cookieValue : cookieValue.substring(0, dot);
        instance().jpaWriteThenMutate(
                em -> refreshTokenRepository.delete(em, id),
                () -> instance().tokensByPlayer.values().forEach(tokens -> tokens.removeIf(t -> t.getId().equals(id))));
        instance().recentRotations.remove(id);
    }

    public static synchronized void revoke(String playerName, String id) {
        instance().jpaWriteThenMutate(
                em -> refreshTokenRepository.delete(em, id),
                () -> {
                    List<RefreshTokenInfo> tokens = instance().tokensByPlayer.get(playerName);
                    if (tokens != null) tokens.removeIf(t -> t.getId().equals(id));
                });
        instance().recentRotations.remove(id);
    }

    public static synchronized void revokeAll(String playerName) {
        List<RefreshTokenInfo> tokens = instance().tokensByPlayer.get(playerName);
        List<String> ids = tokens == null ? List.of() : tokens.stream().map(RefreshTokenInfo::getId).toList();
        instance().jpaWriteThenMutate(
                em -> refreshTokenRepository.deleteAllForPlayer(em, playerName),
                () -> instance().tokensByPlayer.remove(playerName));
        ids.forEach(instance().recentRotations::remove);
    }

    public static synchronized List<RefreshTokenInfo> list(String playerName) {
        return List.copyOf(instance().tokensByPlayer.getOrDefault(playerName, List.of()));
    }

    public static synchronized void cleanupExpired() {
        long now = System.currentTimeMillis();
        instance().jpaWriteThenMutate(
                em -> refreshTokenRepository.deleteExpired(em, now),
                () -> {
                    instance().tokensByPlayer.values().forEach(tokens -> tokens.removeIf(t -> t.getExpiresAt() < now));
                    instance().tokensByPlayer.entrySet().removeIf(e -> e.getValue().isEmpty());
                });
        instance().recentRotations.values().removeIf(r -> now - r.rotatedAtMillis() > ROTATION_GRACE_MILLIS);
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background flush needed
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            refreshTokenRepository.findAll(em).forEach(entity -> {
                RefreshTokenInfo info = entity.toRefreshTokenInfo();
                tokensByPlayer.computeIfAbsent(info.getPlayerName(), k -> new ArrayList<>()).add(info);
            });
            logger.info("Loaded refresh tokens for {} players from JPA",
                    tokensByPlayer.size());
        } catch (Exception e) {
            logger.error("JPA load failed for RefreshTokenService", e);
        }
    }
}
