package net.deckserver.services;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class AuthServiceTest {

    @Test
    void issueTokens_setsHttpOnlySecureCookies_andAccessTokenResolvesUsername() {
        String player = uniquePlayer();

        List<NewCookie> issuedCookies = AuthService.issueTokens(player, true, headers(Map.of(), "JUnit-Agent"));

        assertThat(issuedCookies, hasSize(2));
        for (NewCookie cookie : issuedCookies) {
            assertThat(cookie.isSecure(), is(true));
            assertThat(cookie.isHttpOnly(), is(true));
            // MicroProfile Config resolves application.properties via plain
            // classpath discovery, independent of whether Quarkus's own
            // runtime/CDI container is running — so quarkus.http.root-path
            // resolves here too, even in this plain JUnit test.
            assertThat(cookie.getPath(), is("/jol"));
        }

        NewCookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();
        assertThat(AuthService.parseAccessToken(accessCookie.getValue()), is(Optional.of(player)));
    }

    @Test
    void currentUsername_returnsEmpty_whenNoCookiePresent() {
        assertThat(AuthService.currentUsername(headers(Map.of(), "JUnit-Agent")), is(Optional.empty()));
    }

    @Test
    void currentUsername_returnsUsername_forValidAccessToken() {
        String player = uniquePlayer();
        List<NewCookie> issuedCookies = AuthService.issueTokens(player, false, headers(Map.of(), "JUnit-Agent"));
        NewCookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();

        Optional<String> resolved = AuthService.currentUsername(
                headers(Map.of(accessCookie.getName(), toRequestCookie(accessCookie)), "JUnit-Agent"));

        assertThat(resolved, is(Optional.of(player)));
    }

    @Test
    void currentUsername_returnsEmpty_forTamperedToken() {
        String player = uniquePlayer();
        List<NewCookie> issuedCookies = AuthService.issueTokens(player, false, headers(Map.of(), "JUnit-Agent"));
        NewCookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();
        Cookie tampered = new Cookie.Builder(accessCookie.getName()).value(accessCookie.getValue() + "x").build();

        assertThat(AuthService.currentUsername(headers(Map.of(tampered.getName(), tampered), "JUnit-Agent")),
                is(Optional.empty()));
    }

    @Test
    void currentUsername_returnsEmpty_forExpiredToken() throws Exception {
        String expired = TokenService.issueForTest(
                uniquePlayer(), Set.of(),
                Instant.now().minus(Duration.ofHours(1)),
                Instant.now().minus(Duration.ofMinutes(5)));

        assertThat(AuthService.parseAccessToken(expired), is(Optional.empty()));
    }

    @Test
    void authenticate_returnsUsername_withoutRotation_whenAccessTokenValid() {
        String player = uniquePlayer();
        List<NewCookie> issuedCookies = AuthService.issueTokens(player, false, headers(Map.of(), "JUnit-Agent"));
        NewCookie accessCookie = issuedCookies.stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();

        AuthService.AuthResult result = AuthService.authenticate(
                headers(Map.of(accessCookie.getName(), toRequestCookie(accessCookie)), "JUnit-Agent"));

        assertThat(result.username(), is(Optional.of(player)));
        assertThat("a valid access token should short-circuit before touching the refresh flow",
                result.cookiesToSet(), is(empty()));
    }

    @Test
    void authenticate_returnsEmpty_whenNoCookiesAtAll() {
        AuthService.AuthResult result = AuthService.authenticate(headers(Map.of(), "JUnit-Agent"));

        assertThat(result.username(), is(Optional.empty()));
        assertThat(result.cookiesToSet(), is(empty()));
    }

    @Test
    void authenticate_silentlyRefreshesAndRotates_whenAccessExpiredButRefreshValid() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);
        Cookie refreshCookie = new Cookie.Builder("jol_rt").value(issued.cookieValue()).build();

        AuthService.AuthResult result = AuthService.authenticate(
                headers(Map.of("jol_rt", refreshCookie), "JUnit-Agent"));

        assertThat(result.username(), is(Optional.of(player)));
        assertThat(result.cookiesToSet(), hasSize(2));
        NewCookie newAccess = result.cookiesToSet().stream().filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();
        NewCookie newRefresh = result.cookiesToSet().stream().filter(c -> !looksLikeJwt(c)).findFirst().orElseThrow();
        assertThat(AuthService.parseAccessToken(newAccess.getValue()), is(Optional.of(player)));
        assertThat("refresh token must rotate on use", newRefresh.getValue(), not(equalTo(issued.cookieValue())));

        // a concurrent request racing this same rotation (still holding the original,
        // now-rotated-away cookie) must transparently get back the same rotation rather
        // than being treated as a stolen-token replay and logged out
        Optional<RefreshTokenService.Rotated> raced = RefreshTokenService.validateAndRotate(issued.cookieValue());
        assertThat(raced.map(RefreshTokenService.Rotated::cookieValue), is(Optional.of(newRefresh.getValue())));
    }

    @Test
    void authenticate_treatsAccessTokenIssuedBeforeARoleChangeAsStale() throws Exception {
        String player = uniquePlayer();
        NewCookie access = AuthService.issueTokens(player, false, headers(Map.of(), "JUnit-Agent")).stream()
                .filter(AuthServiceTest::looksLikeJwt).findFirst().orElseThrow();
        Cookie accessCookie = toRequestCookie(access);

        // valid until a role change is recorded for this player...
        assertThat(AuthService.authenticate(headers(Map.of("jol_at", accessCookie), "JUnit-Agent")).username(),
                is(Optional.of(player)));

        Thread.sleep(1100); // token iat has 1s granularity; make the bump land strictly after it
        PlayerService.bumpMinTokenIssuedAt(player);

        // ...after which the same token no longer authenticates on its own (and with
        // no refresh cookie present, the request is simply unauthenticated).
        AuthService.AuthResult afterChange =
                AuthService.authenticate(headers(Map.of("jol_at", accessCookie), "JUnit-Agent"));
        assertThat(afterChange.username(), is(Optional.empty()));
    }

    @Test
    void authenticate_clearsRefreshCookie_whenRefreshTokenInvalid() {
        Cookie bogusRefresh = new Cookie.Builder("jol_rt").value("not-a-real-token.value").build();

        AuthService.AuthResult result = AuthService.authenticate(
                headers(Map.of("jol_rt", bogusRefresh), "JUnit-Agent"));

        assertThat(result.username(), is(Optional.empty()));
        assertThat(result.cookiesToSet(), hasSize(1));
        assertThat(result.cookiesToSet().get(0).getMaxAge(), is(0));
    }

    @Test
    void clearAuth_revokesRefreshToken_andClearsBothCookies() {
        String player = uniquePlayer();
        RefreshTokenService.Issued issued = RefreshTokenService.issue(player, "JUnit-Agent", true);
        Cookie refreshCookie = new Cookie.Builder("jol_rt").value(issued.cookieValue()).build();
        Cookie accessCookie = new Cookie.Builder("jol_at").value("irrelevant").build();

        List<NewCookie> responseCookies = AuthService.clearAuth(
                headers(Map.of("jol_at", accessCookie, "jol_rt", refreshCookie), "JUnit-Agent"));

        assertThat(responseCookies, hasSize(2));
        assertThat(responseCookies, everyItem(hasProperty("maxAge", is(0))));
        assertThat("revoked refresh token must no longer validate",
                RefreshTokenService.validateAndRotate(issued.cookieValue()), is(Optional.empty()));
    }

    private static boolean looksLikeJwt(NewCookie cookie) {
        return AuthService.parseAccessToken(cookie.getValue()).isPresent();
    }

    private static Cookie toRequestCookie(NewCookie newCookie) {
        return new Cookie.Builder(newCookie.getName()).value(newCookie.getValue()).build();
    }

    private static String uniquePlayer() {
        return "AuthServiceTest-" + UUID.randomUUID();
    }

    /** Minimal HttpHeaders fake — AuthService only calls getCookies() and getHeaderString("User-Agent"). */
    private static HttpHeaders headers(Map<String, Cookie> cookies, String userAgent) {
        return (HttpHeaders) Proxy.newProxyInstance(
                AuthServiceTest.class.getClassLoader(),
                new Class<?>[]{HttpHeaders.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getCookies" -> cookies;
                    case "getHeaderString" -> "User-Agent".equals(args[0]) ? userAgent : null;
                    case "getMediaType" -> MediaType.APPLICATION_JSON_TYPE;
                    case "toString" -> "FakeHttpHeaders";
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    default -> defaultReturn(method.getReturnType());
                });
    }

    private static Object defaultReturn(Class<?> returnType) {
        if (!returnType.isPrimitive()) return null;
        if (returnType == boolean.class) return false;
        if (returnType == void.class) return null;
        return 0;
    }
}
