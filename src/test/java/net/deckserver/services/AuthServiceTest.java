package net.deckserver.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import javax.crypto.SecretKey;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class AuthServiceTest {

    @Test
    void issueTokens_setsHttpOnlySecureCookies_andAccessTokenResolvesUsername() {
        String player = uniquePlayer();
        List<Cookie> issuedCookies = new ArrayList<>();

        AuthService.issueTokens(player, true, request(null, "/jol", "JUnit-Agent"), response(issuedCookies));

        assertThat(issuedCookies, hasSize(2));
        for (Cookie cookie : issuedCookies) {
            assertThat(cookie.getSecure(), is(true));
            assertThat(cookie.isHttpOnly(), is(true));
            assertThat(cookie.getPath(), is("/jol"));
        }

        Cookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();
        assertThat(AuthService.parseAccessToken(accessCookie.getValue()), is(Optional.of(player)));
    }

    @Test
    void issueTokens_defaultsCookiePathToRoot_whenContextPathEmpty() {
        List<Cookie> issuedCookies = new ArrayList<>();

        AuthService.issueTokens(uniquePlayer(), false, request(null, "", "JUnit-Agent"), response(issuedCookies));

        assertThat(issuedCookies, everyItem(hasProperty("path", is("/"))));
    }

    @Test
    void currentUsername_returnsEmpty_whenNoCookiePresent() {
        assertThat(AuthService.currentUsername(request(null, "/jol", "JUnit-Agent")), is(Optional.empty()));
    }

    @Test
    void currentUsername_returnsUsername_forValidAccessToken() {
        String player = uniquePlayer();
        List<Cookie> issuedCookies = new ArrayList<>();
        AuthService.issueTokens(player, false, request(null, "/jol", "JUnit-Agent"), response(issuedCookies));
        Cookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();

        Optional<String> resolved = AuthService.currentUsername(
                request(new Cookie[]{accessCookie}, "/jol", "JUnit-Agent"));

        assertThat(resolved, is(Optional.of(player)));
    }

    @Test
    void currentUsername_returnsEmpty_forTamperedToken() {
        String player = uniquePlayer();
        List<Cookie> issuedCookies = new ArrayList<>();
        AuthService.issueTokens(player, false, request(null, "/jol", "JUnit-Agent"), response(issuedCookies));
        Cookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();
        Cookie tampered = new Cookie(accessCookie.getName(), accessCookie.getValue() + "x");

        assertThat(AuthService.currentUsername(request(new Cookie[]{tampered}, "/jol", "JUnit-Agent")),
                is(Optional.empty()));
    }

    @Test
    void currentUsername_returnsEmpty_forExpiredToken() throws Exception {
        String expired = Jwts.builder()
                .subject(uniquePlayer())
                .issuedAt(Date.from(Instant.now().minus(Duration.ofHours(1))))
                .expiration(Date.from(Instant.now().minus(Duration.ofMinutes(1))))
                .signWith(loadTestSigningKey())
                .compact();

        assertThat(AuthService.parseAccessToken(expired), is(Optional.empty()));
    }

    @Test
    void authenticate_returnsUsername_withoutRotation_whenAccessTokenValid() {
        String player = uniquePlayer();
        List<Cookie> issuedCookies = new ArrayList<>();
        AuthService.issueTokens(player, false, request(null, "/jol", "JUnit-Agent"), response(issuedCookies));
        Cookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();

        List<Cookie> secondResponseCookies = new ArrayList<>();
        Optional<String> result = AuthService.authenticate(
                request(new Cookie[]{accessCookie}, "/jol", "JUnit-Agent"), response(secondResponseCookies));

        assertThat(result, is(Optional.of(player)));
        assertThat("a valid access token should short-circuit before touching the refresh flow",
                secondResponseCookies, is(empty()));
    }

    @Test
    void authenticate_returnsEmpty_whenNoCookiesAtAll() {
        List<Cookie> responseCookies = new ArrayList<>();
        Optional<String> result = AuthService.authenticate(
                request(null, "/jol", "JUnit-Agent"), response(responseCookies));

        assertThat(result, is(Optional.empty()));
        assertThat(responseCookies, is(empty()));
    }

    @Test
    void authenticate_silentlyRefreshesAndRotates_whenAccessExpiredButRefreshValid() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);
        Cookie refreshCookie = new Cookie("jol_rt", issued.cookieValue());

        List<Cookie> responseCookies = new ArrayList<>();
        Optional<String> result = AuthService.authenticate(
                request(new Cookie[]{refreshCookie}, "/jol", "JUnit-Agent"), response(responseCookies));

        assertThat(result, is(Optional.of(player)));
        assertThat(responseCookies, hasSize(2));
        Cookie newAccess = responseCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();
        Cookie newRefresh = responseCookies.stream().filter(c -> !looksLikeJwt(c)).findFirst().orElseThrow();
        assertThat(AuthService.parseAccessToken(newAccess.getValue()), is(Optional.of(player)));
        assertThat("refresh token must rotate on use", newRefresh.getValue(), not(equalTo(issued.cookieValue())));

        // the original (now-rotated-away) refresh cookie must no longer be usable
        assertThat(RefreshTokenService.validateAndRotate(issued.cookieValue()), is(Optional.empty()));
    }

    @Test
    void authenticate_clearsRefreshCookie_whenRefreshTokenInvalid() {
        Cookie bogusRefresh = new Cookie("jol_rt", "not-a-real-token.value");

        List<Cookie> responseCookies = new ArrayList<>();
        Optional<String> result = AuthService.authenticate(
                request(new Cookie[]{bogusRefresh}, "/jol", "JUnit-Agent"), response(responseCookies));

        assertThat(result, is(Optional.empty()));
        assertThat(responseCookies, hasSize(1));
        assertThat(responseCookies.get(0).getMaxAge(), is(0));
    }

    @Test
    void clearAuth_revokesRefreshToken_andClearsBothCookies() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);
        Cookie refreshCookie = new Cookie("jol_rt", issued.cookieValue());
        Cookie accessCookie = new Cookie("jol_at", "irrelevant");

        List<Cookie> responseCookies = new ArrayList<>();
        AuthService.clearAuth(request(new Cookie[]{accessCookie, refreshCookie}, "/jol", "JUnit-Agent"),
                response(responseCookies));

        assertThat(responseCookies, hasSize(2));
        assertThat(responseCookies, everyItem(hasProperty("maxAge", is(0))));
        assertThat("revoked refresh token must no longer validate",
                RefreshTokenService.validateAndRotate(issued.cookieValue()), is(Optional.empty()));
    }

    private static boolean looksLikeJwt(Cookie cookie) {
        return AuthService.parseAccessToken(cookie.getValue()).isPresent();
    }

    private static String uniquePlayer() {
        return "AuthServiceTest-" + UUID.randomUUID();
    }

    private static SecretKey loadTestSigningKey() throws Exception {
        Path keyFile = Path.of("src", "test", "resources", "data", "jwt_secret.key");
        byte[] keyBytes = Base64.getDecoder().decode(Files.readString(keyFile).strip());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static HttpServletRequest request(Cookie[] cookies, String contextPath, String userAgent) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                AuthServiceTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getCookies" -> cookies;
                    case "getContextPath" -> contextPath;
                    case "getHeader" -> "User-Agent".equals(args[0]) ? userAgent : null;
                    case "toString" -> "FakeHttpServletRequest";
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    default -> defaultReturn(method.getReturnType());
                });
    }

    private static HttpServletResponse response(List<Cookie> sink) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                AuthServiceTest.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("addCookie".equals(method.getName())) {
                        sink.add((Cookie) args[0]);
                        return null;
                    }
                    if ("toString".equals(method.getName())) return "FakeHttpServletResponse";
                    if ("equals".equals(method.getName())) return proxy == args[0];
                    if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                    return defaultReturn(method.getReturnType());
                });
    }

    private static Object defaultReturn(Class<?> returnType) {
        if (!returnType.isPrimitive()) return null;
        if (returnType == boolean.class) return false;
        if (returnType == void.class) return null;
        return 0;
    }
}