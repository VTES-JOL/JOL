package net.deckserver.services;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import net.deckserver.game.enums.PlayerRole;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.jwt.JwtClaims;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cookie-based auth: a short-lived RS256 JWT access token (minted/verified by
 * {@link TokenService}, carrying the player's roles in its {@code groups} claim) is
 * what every request actually checks; a long-lived opaque refresh token (see
 * {@link RefreshTokenService}) is used only to silently reissue an access token once
 * it expires, without requiring the user to log in again. Replaces the old HttpSession
 * "meth" attribute.
 * <p>
 * Ported from a javax.servlet HttpServletRequest/HttpServletResponse-based API to a
 * jakarta.ws.rs HttpHeaders-in / NewCookie-out one: Quarkus REST's request pipeline never gives filters or
 * resources a live, directly-mutable response object the way a Servlet container did,
 * so cookie writes are now values returned to the caller (SecurityFilter, AuthResource)
 * to attach to the outgoing Response themselves, rather than a side effect this class
 * performs directly.
 */
public final class AuthService {

    public static final String ACCESS_COOKIE = "jol_at";
    public static final String REFRESH_COOKIE = "jol_rt";
    private static final Duration ACCESS_TTL = TokenService.ACCESS_TTL;

    private AuthService() {
    }

    /**
     * The outcome of an auth check: who's authenticated (if anyone), the role
     * names carried in their access token's {@code groups} claim, plus any
     * cookies the caller must attach to its response.
     */
    public record AuthResult(Optional<String> username, Set<String> roles, List<NewCookie> cookiesToSet) {
        public static AuthResult of(String username, Set<String> roles) {
            return new AuthResult(Optional.of(username), Set.copyOf(roles), List.of());
        }

        public static AuthResult unauthenticated() {
            return new AuthResult(Optional.empty(), Set.of(), List.of());
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
        Optional<JwtClaims> claims = accessTokenCookie.flatMap(TokenService::verify);
        if (claims.isPresent()) {
            String subject = TokenService.subjectOf(claims.get());
            // Honour a role change made this run: a token issued before the
            // player's role last changed is treated as stale and falls through
            // to the refresh-rotation path below, which re-reads live roles.
            boolean fresh = subject != null
                    && TokenService.issuedAtSecondsOf(claims.get()) >= PlayerService.minTokenIssuedAt(subject);
            if (fresh) {
                return AuthResult.of(subject, Set.copyOf(TokenService.groupsOf(claims.get())));
            }
        }

        if (refreshTokenCookie.isEmpty()) return AuthResult.unauthenticated();

        Optional<RefreshTokenService.Rotated> rotated = RefreshTokenService.validateAndRotate(refreshTokenCookie.get());
        if (rotated.isEmpty()) {
            return new AuthResult(Optional.empty(), Set.of(), List.of(clearCookie(REFRESH_COOKIE)));
        }

        String playerName = rotated.get().playerName();
        Set<String> roles = currentRoleNames(playerName);
        List<NewCookie> cookies = new ArrayList<>();
        cookies.add(accessCookie(playerName, roles));
        cookies.add(refreshCookie(rotated.get().cookieValue(), rotated.get().remember()));
        return new AuthResult(Optional.of(playerName), roles, cookies);
    }

    /** Access-token-only check, with no rotation/side effects — safe to call outside a response context. */
    public static Optional<String> currentUsername(HttpHeaders headers) {
        return cookieValue(headers, ACCESS_COOKIE).flatMap(AuthService::parseAccessToken);
    }

    public static List<NewCookie> issueTokens(String playerName, boolean remember, HttpHeaders headers) {
        RefreshTokenService.Issued issued = RefreshTokenService.issue(playerName, deviceLabel(headers), remember);
        return List.of(accessCookie(playerName, currentRoleNames(playerName)),
                refreshCookie(issued.cookieValue(), remember));
    }

    public static List<NewCookie> clearAuth(HttpHeaders headers) {
        cookieValue(headers, REFRESH_COOKIE).ifPresent(RefreshTokenService::revoke);
        return clearAuthCookies();
    }

    /** Just the two max-age-0 eviction cookies — no server-side revoke (caller decides that). */
    public static List<NewCookie> clearAuthCookies() {
        return List.of(clearCookie(ACCESS_COOKIE), clearCookie(REFRESH_COOKIE));
    }

    public static Optional<String> parseAccessToken(String jwt) {
        return TokenService.subject(jwt);
    }

    private static NewCookie accessCookie(String playerName, Set<String> roleNames) {
        String jwt = TokenService.issue(playerName, roleNames);
        return buildCookie(ACCESS_COOKIE, jwt, (int) ACCESS_TTL.toSeconds());
    }

    /**
     * Role names to bake into a freshly-minted access token. Read here — at issue
     * and at every silent refresh-token rotation — so a granted/revoked role takes
     * effect within one access-token lifetime ({@link TokenService#ACCESS_TTL})
     * without any explicit token invalidation. Defensive against a subject that no
     * longer exists (DB swapped under a live session): SecurityFilter / the WS
     * handshake reject that case separately, so an empty role set here is harmless.
     */
    private static Set<String> currentRoleNames(String playerName) {
        if (!PlayerService.existsPlayer(playerName)) {
            return Set.of();
        }
        return PlayerService.get(playerName).getRoles().stream()
                .map(PlayerRole::name)
                .collect(Collectors.toSet());
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
}
