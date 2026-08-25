package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import io.azam.ulidj.ULID;
import net.deckserver.storage.json.system.RefreshTokenInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final Path PERSISTENCE_PATH = DataPaths.path("refreshTokens.json");
    private static final RefreshTokenService INSTANCE = new RefreshTokenService();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TTL_REMEMBER_MILLIS = TimeUnit.DAYS.toMillis(30);
    private static final long TTL_SESSION_MILLIS = TimeUnit.HOURS.toMillis(12);
    private static final long ABSOLUTE_MAX_AGE_MILLIS = TimeUnit.DAYS.toMillis(90);

    private final Map<String, List<RefreshTokenInfo>> tokensByPlayer = new HashMap<>();

    private RefreshTokenService() {
        super("RefreshTokenService", 5);
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

        INSTANCE.tokensByPlayer.computeIfAbsent(playerName, k -> new ArrayList<>()).add(info);
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
                    tokens.remove(info);
                    return Optional.empty();
                }

                String newSecret = randomSecret();
                info.setSecretHash(hash(newSecret));
                info.setLastUsedAt(now);
                long ttl = info.isRemember() ? TTL_REMEMBER_MILLIS : TTL_SESSION_MILLIS;
                info.setExpiresAt(Math.min(now + ttl, info.getCreatedAt() + ABSOLUTE_MAX_AGE_MILLIS));

                return Optional.of(new Rotated(info.getPlayerName(), id + "." + newSecret, info.isRemember()));
            }
        }
        return Optional.empty();
    }

    public static synchronized void revoke(String cookieValue) {
        if (cookieValue == null) return;
        int dot = cookieValue.indexOf('.');
        String id = dot < 0 ? cookieValue : cookieValue.substring(0, dot);
        INSTANCE.tokensByPlayer.values().forEach(tokens -> tokens.removeIf(t -> t.getId().equals(id)));
    }

    public static synchronized void revoke(String playerName, String id) {
        List<RefreshTokenInfo> tokens = INSTANCE.tokensByPlayer.get(playerName);
        if (tokens != null) tokens.removeIf(t -> t.getId().equals(id));
    }

    public static synchronized void revokeAll(String playerName) {
        INSTANCE.tokensByPlayer.remove(playerName);
    }

    public static synchronized List<RefreshTokenInfo> list(String playerName) {
        return List.copyOf(INSTANCE.tokensByPlayer.getOrDefault(playerName, List.of()));
    }

    public static synchronized void cleanupExpired() {
        long now = System.currentTimeMillis();
        INSTANCE.tokensByPlayer.values().forEach(tokens -> tokens.removeIf(t -> t.getExpiresAt() < now));
        INSTANCE.tokensByPlayer.entrySet().removeIf(e -> e.getValue().isEmpty());
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
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }
        try {
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), tokensByPlayer);
        } catch (IOException e) {
            logger.error("Unable to save refresh token data", e);
        }
    }

    @Override
    protected void load() {
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing refresh token file found");
            return;
        }
        try {
            Map<String, List<RefreshTokenInfo>> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {
            });
            tokensByPlayer.putAll(loaded);
            logger.info("Loaded refresh tokens for {} players", tokensByPlayer.size());
        } catch (IOException e) {
            logger.error("Unable to load refresh token data", e);
        }
    }
}
