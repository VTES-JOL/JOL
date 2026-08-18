package net.deckserver.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/**
 * Cookie-based auth: a short-lived JWT access token is what every request actually
 * checks; a long-lived opaque refresh token (see {@link RefreshTokenService}) is used
 * only to silently reissue an access token once it expires, without requiring the
 * user to log in again. Replaces the old HttpSession "meth" attribute.
 */
public final class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String ACCESS_COOKIE = "jol_at";
    private static final String REFRESH_COOKIE = "jol_rt";
    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);

    private static final SecretKey KEY = loadOrCreateKey();

    private AuthService() {
    }

    /** Full auth check for an inbound request: valid access token, or a silent refresh via the refresh cookie. */
    public static Optional<String> authenticate(HttpServletRequest req, HttpServletResponse resp) {
        Optional<String> fromAccessToken = cookieValue(req, ACCESS_COOKIE).flatMap(AuthService::parseAccessToken);
        if (fromAccessToken.isPresent()) return fromAccessToken;

        Optional<String> refreshCookie = cookieValue(req, REFRESH_COOKIE);
        if (refreshCookie.isEmpty()) return Optional.empty();

        Optional<RefreshTokenService.Rotated> rotated = RefreshTokenService.validateAndRotate(refreshCookie.get());
        if (rotated.isEmpty()) {
            clearCookie(REFRESH_COOKIE, req, resp);
            return Optional.empty();
        }

        setAccessCookie(rotated.get().playerName(), req, resp);
        setRefreshCookie(rotated.get().cookieValue(), rotated.get().remember(), req, resp);
        return Optional.of(rotated.get().playerName());
    }

    /** Access-token-only check, with no rotation/side effects — safe to call outside a response context. */
    public static Optional<String> currentUsername(HttpServletRequest req) {
        return cookieValue(req, ACCESS_COOKIE).flatMap(AuthService::parseAccessToken);
    }

    public static void issueTokens(String playerName, boolean remember, HttpServletRequest req, HttpServletResponse resp) {
        setAccessCookie(playerName, req, resp);
        RefreshTokenService.Issued issued = RefreshTokenService.issue(playerName, deviceLabel(req), remember);
        setRefreshCookie(issued.cookieValue(), remember, req, resp);
    }

    public static void clearAuth(HttpServletRequest req, HttpServletResponse resp) {
        cookieValue(req, REFRESH_COOKIE).ifPresent(RefreshTokenService::revoke);
        clearCookie(ACCESS_COOKIE, req, resp);
        clearCookie(REFRESH_COOKIE, req, resp);
    }

    public static Optional<String> parseAccessToken(String jwt) {
        try {
            Claims claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(jwt).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static void setAccessCookie(String playerName, HttpServletRequest req, HttpServletResponse resp) {
        String jwt = Jwts.builder()
                .subject(playerName)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(ACCESS_TTL)))
                .signWith(KEY)
                .compact();
        Cookie cookie = new Cookie(ACCESS_COOKIE, jwt);
        configureCookie(cookie, req);
        cookie.setMaxAge((int) ACCESS_TTL.toSeconds());
        resp.addCookie(cookie);
    }

    private static void setRefreshCookie(String value, boolean remember, HttpServletRequest req, HttpServletResponse resp) {
        Cookie cookie = new Cookie(REFRESH_COOKIE, value);
        configureCookie(cookie, req);
        if (remember) {
            cookie.setMaxAge((int) Duration.ofDays(30).toSeconds());
        }
        resp.addCookie(cookie);
    }

    private static void clearCookie(String name, HttpServletRequest req, HttpServletResponse resp) {
        Cookie cookie = new Cookie(name, "");
        configureCookie(cookie, req);
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }

    private static void configureCookie(Cookie cookie, HttpServletRequest req) {
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        String contextPath = req.getContextPath();
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
    }

    private static Optional<String> cookieValue(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return Optional.empty();
        return Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }

    private static String deviceLabel(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        if (ua == null) return "Unknown device";
        return ua.length() > 200 ? ua.substring(0, 200) : ua;
    }

    private static SecretKey loadOrCreateKey() {
        Path path = DataPaths.path("jwt_secret.key");
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
