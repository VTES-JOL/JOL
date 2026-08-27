package net.deckserver.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Cookie-based auth: a short-lived JWT access token is what every request actually
 * checks; a long-lived opaque refresh token (see {@link RefreshTokenService}) is used
 * only to silently reissue an access token once it expires, without requiring the
 * user to log in again. Replaces the old HttpSession "meth" attribute.
 * <p>
 * Ported from a javax.servlet HttpServletRequest/HttpServletResponse-based API to a
 * jakarta.ws.rs HttpHeaders-in / NewCookie-out one (Phase 3 of the Quarkus migration —
 * see quarkus-poc/FINDINGS.md): Quarkus REST's request pipeline never gives filters or
 * resources a live, directly-mutable response object the way a Servlet container did,
 * so cookie writes are now values returned to the caller (SecurityFilter, AuthResource)
 * to attach to the outgoing Response themselves, rather than a side effect this class
 * performs directly.
 */
public final class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    public static final String ACCESS_COOKIE = "jol_at";
    public static final String REFRESH_COOKIE = "jol_rt";
    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);

    private static final SecretKey KEY = loadOrCreateKey();

    private AuthService() {
    }

    /** The outcome of an auth check: who's authenticated (if anyone), plus any cookies the caller must attach to its response. */
    public record AuthResult(Optional<String> username, List<NewCookie> cookiesToSet) {
        public static AuthResult of(String username) {
            return new AuthResult(Optional.of(username), List.of());
        }

        public static AuthResult unauthenticated() {
            return new AuthResult(Optional.empty(), List.of());
        }
    }

    /** Full auth check for an inbound request: valid access token, or a silent refresh via the refresh cookie. */
    public static AuthResult authenticate(HttpHeaders headers) {
        return authenticate(cookieValue(headers, ACCESS_COOKIE), cookieValue(headers, REFRESH_COOKIE));
    }

    /**
     * Same check as {@link #authenticate(HttpHeaders)}, taking raw cookie values directly —
     * for callers with no {@link HttpHeaders} to hand it, namely the WebSocket handshake
     * (see JolWebSocketEndpoint.Configurator), which only has the raw Cookie header off the
     * handshake request.
     */
    public static AuthResult authenticate(Optional<String> accessTokenCookie, Optional<String> refreshTokenCookie) {
        Optional<String> fromAccessToken = accessTokenCookie.flatMap(AuthService::parseAccessToken);
        if (fromAccessToken.isPresent()) return AuthResult.of(fromAccessToken.get());

        if (refreshTokenCookie.isEmpty()) return AuthResult.unauthenticated();

        Optional<RefreshTokenService.Rotated> rotated = RefreshTokenService.validateAndRotate(refreshTokenCookie.get());
        if (rotated.isEmpty()) {
            return new AuthResult(Optional.empty(), List.of(clearCookie(REFRESH_COOKIE)));
        }

        List<NewCookie> cookies = new ArrayList<>();
        cookies.add(accessCookie(rotated.get().playerName()));
        cookies.add(refreshCookie(rotated.get().cookieValue(), rotated.get().remember()));
        return new AuthResult(Optional.of(rotated.get().playerName()), cookies);
    }

    /** Access-token-only check, with no rotation/side effects — safe to call outside a response context. */
    public static Optional<String> currentUsername(HttpHeaders headers) {
        return cookieValue(headers, ACCESS_COOKIE).flatMap(AuthService::parseAccessToken);
    }

    public static List<NewCookie> issueTokens(String playerName, boolean remember, HttpHeaders headers) {
        RefreshTokenService.Issued issued = RefreshTokenService.issue(playerName, deviceLabel(headers), remember);
        return List.of(accessCookie(playerName), refreshCookie(issued.cookieValue(), remember));
    }

    public static List<NewCookie> clearAuth(HttpHeaders headers) {
        cookieValue(headers, REFRESH_COOKIE).ifPresent(RefreshTokenService::revoke);
        return List.of(clearCookie(ACCESS_COOKIE), clearCookie(REFRESH_COOKIE));
    }

    public static Optional<String> parseAccessToken(String jwt) {
        try {
            Claims claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(jwt).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static NewCookie accessCookie(String playerName) {
        String jwt = Jwts.builder()
                .subject(playerName)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(ACCESS_TTL)))
                .signWith(KEY)
                .compact();
        return buildCookie(ACCESS_COOKIE, jwt, (int) ACCESS_TTL.toSeconds());
    }

    private static NewCookie refreshCookie(String value, boolean remember) {
        int maxAge = remember ? (int) Duration.ofDays(30).toSeconds() : NewCookie.DEFAULT_MAX_AGE;
        return buildCookie(REFRESH_COOKIE, value, maxAge);
    }

    private static NewCookie clearCookie(String name) {
        return buildCookie(name, "", 0);
    }

    private static NewCookie buildCookie(String name, String value, int maxAge) {
        return new NewCookie.Builder(name)
                .value(value)
                .path(contextPath())
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }

    /** Mirrors the old HttpServletRequest.getContextPath() — quarkus.http.root-path is the app's fixed equivalent. */
    private static String contextPath() {
        String rootPath = ConfigProvider.getConfig()
                .getOptionalValue("quarkus.http.root-path", String.class)
                .orElse("/");
        return rootPath.isEmpty() ? "/" : rootPath;
    }

    private static Optional<String> cookieValue(HttpHeaders headers, String name) {
        Cookie cookie = headers.getCookies().get(name);
        return Optional.ofNullable(cookie).map(Cookie::getValue);
    }

    private static String deviceLabel(HttpHeaders headers) {
        String ua = headers.getHeaderString("User-Agent");
        if (ua == null) return "Unknown device";
        return ua.length() > 200 ? ua.substring(0, 200) : ua;
    }

    /**
     * Loads the JWT signing key from the PEM-adjacent file named by the JWT_SECRET_FILE
     * env var (same pattern as VAPID_KEY_FILE for web push), generating and persisting a
     * new one on first run if the file doesn't exist yet. Falls back to a directory next
     * to the working directory when unset, so local dev/test need no configuration.
     */
    private static SecretKey loadOrCreateKey() {
        String keyFile = System.getenv("JWT_SECRET_FILE");
        Path path = Path.of(keyFile != null && !keyFile.isBlank() ? keyFile : "jwt_secret.key");
        try {
            byte[] keyBytes;
            if (Files.exists(path)) {
                keyBytes = Base64.getDecoder().decode(Files.readString(path).strip());
            } else {
                keyBytes = new byte[32];
                new SecureRandom().nextBytes(keyBytes);
                Files.writeString(path, Base64.getEncoder().encodeToString(keyBytes));
                logger.info("Generated new JWT signing key at {}", path);
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load or create JWT signing key at " + path, e);
        }
    }
}
