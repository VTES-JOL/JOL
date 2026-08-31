package net.deckserver.services;

import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class TokenServiceTest {

    @Test
    void issue_thenVerify_roundTripsSubjectAndGroups() throws Exception {
        String jwt = TokenService.issue("Alice", Set.of("ADMIN", "JUDGE"));

        Optional<JwtClaims> claims = TokenService.verify(jwt);
        assertThat(claims.isPresent(), is(true));
        assertThat(claims.get().getSubject(), is("Alice"));
        assertThat(claims.get().getIssuer(), is(TokenService.ISSUER));
        assertThat(TokenService.subject(jwt), is(Optional.of("Alice")));
        assertThat(TokenService.groups(jwt), containsInAnyOrder("ADMIN", "JUDGE"));
    }

    @Test
    void groups_isEmptyList_forRolelessToken() {
        String jwt = TokenService.issue("Bob", Set.of());
        assertThat(TokenService.groups(jwt), is(empty()));
        assertThat(TokenService.subject(jwt), is(Optional.of("Bob")));
    }

    @Test
    void verify_rejectsExpiredToken() {
        String expired = TokenService.issueForTest("Carol", Set.of("ADMIN"),
                Instant.now().minus(Duration.ofHours(2)),
                Instant.now().minus(Duration.ofMinutes(10)));

        assertThat(TokenService.verify(expired), is(Optional.empty()));
        assertThat(TokenService.subject(expired), is(Optional.empty()));
        assertThat(TokenService.groups(expired), is(empty()));
    }

    @Test
    void verify_rejectsTamperedToken() {
        String jwt = TokenService.issue("Dave", Set.of());
        assertThat(TokenService.verify(jwt.substring(0, jwt.length() - 2) + "xy"), is(Optional.empty()));
    }

    @Test
    void verify_rejectsGarbage() {
        assertThat(TokenService.verify("not-a-jwt"), is(Optional.empty()));
        assertThat(TokenService.subject(""), is(Optional.empty()));
    }
}
