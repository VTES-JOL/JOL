package net.deckserver.services;

import io.azam.ulidj.ULID;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.RefreshTokenRepository;
import net.deckserver.storage.json.system.RefreshTokenInfo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Persists "remember me" refresh tokens, one row per logged-in device. A refresh
 * cookie value is "{id}.{secret}": the id gives O(1) lookup, the secret is what's
 * hashed and compared (see Barry Jaspan's persistent-login-cookie pattern). On
 * every use the secret is rotated; a presented secret that no longer matches the
 * stored hash means an old, already-rotated-away token was replayed, so the row
 * is revoked defensively.
 */
public class RefreshTokenService extends PersistedService {

    private static final RefreshTokenRepository refreshTokenRepository = new RefreshTokenRepository();
    private static final RefreshTokenService INSTANCE = new RefreshTokenService();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TTL_REMEMBER_MILLIS = TimeUnit.DAYS.toMillis(30);
    private static final long TTL_SESSION_MILLIS = TimeUnit.HOURS.toMillis(12);
    private static final long ABSOLUTE_MAX_AGE_MILLIS = TimeUnit.DAYS.toMillis(90);

    private final Map<String, List<RefreshTokenInfo>> tokensByPlayer = new HashMap<>();

    private RefreshTokenService() {
        super("RefreshTokenService", 0);
        load();
    }

    public static PersistedService getInstance() {
        return INSTANCE;
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

        INSTANCE.jpaWriteThenMutate(
                em -> refreshTokenRepository.save(em, playerName, info),
                () -> INSTANCE.tokensByPlayer.computeIfAbsent(playerName, k -> new ArrayList<>()).add(info));
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

        for (List<RefreshTokenInfo> tokens : INSTANCE.tokensByPlayer.values()) {
            for (RefreshTokenInfo info : tokens) {
                if (!info.getId().equals(id)) continue;

                long now = System.currentTimeMillis();
                if (info.getExpiresAt() < now || !MessageDigest.isEqual(
                        HexFormat.of().parseHex(info.getSecretHash()),
                        HexFormat.of().parseHex(hash(secret)))) {
                    INSTANCE.jpaWriteThenMutate(
                            em -> refreshTokenRepository.delete(em, id),
                            () -> tokens.remove(info));
                    return Optional.empty();
                }

                String previousSecretHash = info.getSecretHash();
                long previousLastUsedAt = info.getLastUsedAt();
                long previousExpiresAt = info.getExpiresAt();

                String newSecret = randomSecret();
                long newExpiresAt = Math.min(now + (info.isRemember() ? TTL_REMEMBER_MILLIS : TTL_SESSION_MILLIS),
                        info.getCreatedAt() + ABSOLUTE_MAX_AGE_MILLIS);

                INSTANCE.jpaWriteWithRollback(
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

                return Optional.of(new Rotated(info.getPlayerName(), id + "." + newSecret, info.isRemember()));
            }
        }
        return Optional.empty();
    }

    public static synchronized void revoke(String cookieValue) {
        if (cookieValue == null) return;
        int dot = cookieValue.indexOf('.');
        String id = dot < 0 ? cookieValue : cookieValue.substring(0, dot);
        INSTANCE.jpaWriteThenMutate(
                em -> refreshTokenRepository.delete(em, id),
                () -> INSTANCE.tokensByPlayer.values().forEach(tokens -> tokens.removeIf(t -> t.getId().equals(id))));
    }

    public static synchronized void revoke(String playerName, String id) {
        INSTANCE.jpaWriteThenMutate(
                em -> refreshTokenRepository.delete(em, id),
                () -> {
                    List<RefreshTokenInfo> tokens = INSTANCE.tokensByPlayer.get(playerName);
                    if (tokens != null) tokens.removeIf(t -> t.getId().equals(id));
                });
    }

    public static synchronized void revokeAll(String playerName) {
        INSTANCE.jpaWriteThenMutate(
                em -> refreshTokenRepository.deleteAllForPlayer(em, playerName),
                () -> INSTANCE.tokensByPlayer.remove(playerName));
    }

    public static synchronized List<RefreshTokenInfo> list(String playerName) {
        return List.copyOf(INSTANCE.tokensByPlayer.getOrDefault(playerName, List.of()));
    }

    public static synchronized void cleanupExpired() {
        long now = System.currentTimeMillis();
        INSTANCE.jpaWriteThenMutate(
                em -> refreshTokenRepository.deleteExpired(em, now),
                () -> {
                    INSTANCE.tokensByPlayer.values().forEach(tokens -> tokens.removeIf(t -> t.getExpiresAt() < now));
                    INSTANCE.tokensByPlayer.entrySet().removeIf(e -> e.getValue().isEmpty());
                });
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
