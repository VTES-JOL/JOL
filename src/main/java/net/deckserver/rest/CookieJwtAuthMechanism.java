package net.deckserver.rest;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import net.deckserver.services.AuthService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.RefreshTokenService;

import java.util.Optional;
import java.util.Set;

/**
 * The single {@link HttpAuthenticationMechanism} for the app: turns the cookie
 * pair ({@code jol_at} access JWT, {@code jol_rt} opaque refresh token) into a
 * Quarkus {@link SecurityIdentity} whose roles are the access token's
 * {@code groups} claim — so {@code @RolesAllowed} and the
 * {@code quarkus.http.auth.permission.*} policies in application.properties work
 * against it.
 * <p>
 * Replaces {@code SecurityFilter} (deleted): the blanket "401 unless a valid
 * session" is now the {@code authenticated} permission policy on {@code /jol/api/*},
 * and the silent refresh-token rotation + stale-subject cookie clearing that the
 * old JAX-RS response filter did is done here, writing {@code Set-Cookie} straight
 * onto the Vert.x response (which a request-phase JAX-RS filter couldn't reach).
 * <p>
 * All the actual decision logic still lives in
 * {@link AuthService#authenticate(Optional, Optional)} — this class only adapts it
 * to the Vert.x auth SPI and runs it on a worker thread (it may hit the DB via
 * {@code RefreshTokenService}).
 */
@ApplicationScoped
public class CookieJwtAuthMechanism implements HttpAuthenticationMechanism {

    private static final RuntimeDelegate.HeaderDelegate<NewCookie> COOKIE_DELEGATE =
            RuntimeDelegate.getInstance().createHeaderDelegate(NewCookie.class);

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        return Uni.createFrom().item(() -> resolve(context))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    private SecurityIdentity resolve(RoutingContext context) {
        Optional<String> accessCookie = cookie(context, AuthService.ACCESS_COOKIE);
        Optional<String> refreshCookie = cookie(context, AuthService.REFRESH_COOKIE);
        if (accessCookie.isEmpty() && refreshCookie.isEmpty()) {
            return null;
        }

        AuthService.AuthResult result = AuthService.authenticate(accessCookie, refreshCookie);
        result.cookiesToSet().forEach(c -> addSetCookie(context, c));

        if (result.username().isEmpty()) {
            return null;
        }

        String username = result.username().get();
        // A correctly-signed, unexpired token whose subject no longer exists —
        // e.g. the DB was swapped under a live browser session. Clear both
        // cookies (revoking any orphaned refresh token) and stay anonymous so
        // the permission layer returns a clean 401 the SPA can redirect on,
        // rather than letting the request through to a 500 in the first service
        // call that looks the player up.
        if (!PlayerService.existsPlayer(username)) {
            refreshCookie.ifPresent(RefreshTokenService::revoke);
            AuthService.clearAuthCookies().forEach(c -> addSetCookie(context, c));
            return null;
        }

        return QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(username))
                .addRoles(result.roles())
                .build();
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        // Bare 401, no WWW-Authenticate / redirect — api/client.ts turns any 401
        // into a client-side redirect to /jol/login itself.
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        // We build the SecurityIdentity ourselves; no IdentityProvider involved.
        return Set.of();
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return Uni.createFrom().item(
                new HttpCredentialTransport(HttpCredentialTransport.Type.COOKIE, AuthService.ACCESS_COOKIE));
    }

    private static Optional<String> cookie(RoutingContext context, String name) {
        Cookie cookie = context.request().getCookie(name);
        return Optional.ofNullable(cookie).map(Cookie::getValue).filter(v -> !v.isBlank());
    }

    private static void addSetCookie(RoutingContext context, NewCookie cookie) {
        context.response().headers().add("Set-Cookie", COOKIE_DELEGATE.toString(cookie));
    }
}
