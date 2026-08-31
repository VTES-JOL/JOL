package net.deckserver.services;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Mints and verifies the short-lived access token ({@code jol_at} cookie) as an
 * asymmetric RS256 JWT carrying the standard MicroProfile-JWT claims — notably
 * {@code groups}, the player's {@link net.deckserver.game.enums.PlayerRole}
 * names, which Quarkus's {@code quarkus-smallrye-jwt} mechanism turns into
 * {@code SecurityIdentity} roles for {@code @RolesAllowed}. Replaces the
 * hand-rolled HMAC tokens {@link AuthService} used to build with jjwt.
 * <p>
 * Signing/verification go through jose4j directly (not {@code smallrye-jwt-build}
 * / an injected {@code JWTParser}) so this behaves identically inside and outside
 * a Quarkus runtime — service-level tests boot an H2 EntityManagerFactory with no
 * Quarkus container at all (see {@code JolServiceExtension}).
 * <p>
 * Keys are PEM files named by {@code JWT_PRIVATE_KEY_FILE} / {@code JWT_PUBLIC_KEY_FILE};
 * when unset they fall back to the committed dev keypair on the classpath
 * ({@code jwt/jwt-dev-*.pem}), the same "checked-in dev credential, real file in
 * prod" pattern as {@code dev-keystore.p12}. The public key file must also be
 * pointed at by {@code mp.jwt.verify.publickey.location} in application.properties
 * so Quarkus's own verifier and this class agree.
 */
public final class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    /** Must match {@code mp.jwt.verify.issuer} in application.properties. */
    public static final String ISSUER = "https://deckserver.net/jol";
    public static final String GROUPS_CLAIM = "groups";
    static final Duration ACCESS_TTL = Duration.ofMinutes(15);
    private static final int CLOCK_SKEW_SECONDS = 30;

    private static final String DEV_PRIVATE_KEY_RESOURCE = "jwt/jwt-dev-private.pem";
    private static final String DEV_PUBLIC_KEY_RESOURCE = "jwt/jwt-dev-public.pem";

    private static final PrivateKey PRIVATE_KEY = loadPrivateKey();
    private static final PublicKey PUBLIC_KEY = loadPublicKey();

    private TokenService() {
    }

    /** Builds a signed access token for {@code subject} with the given role names as its {@code groups} claim. */
    public static String issue(String subject, Set<String> roles) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(ISSUER);
        claims.setSubject(subject);
        claims.setClaim("upn", subject);
        claims.setStringListClaim(GROUPS_CLAIM, new ArrayList<>(roles));
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        claims.setExpirationTime(NumericDate.fromMilliseconds(
                System.currentTimeMillis() + ACCESS_TTL.toMillis()));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(PRIVATE_KEY);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setHeader("typ", "JWT");
        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalStateException("Unable to sign access token", e);
        }
    }

    /** Test seam: mint a token with explicit issue/expiry instants (e.g. an already-expired one). */
    static String issueForTest(String subject, Set<String> roles, java.time.Instant issuedAt, java.time.Instant expiresAt) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(ISSUER);
        claims.setSubject(subject);
        claims.setClaim("upn", subject);
        claims.setStringListClaim(GROUPS_CLAIM, new ArrayList<>(roles));
        claims.setIssuedAt(NumericDate.fromMilliseconds(issuedAt.toEpochMilli()));
        claims.setGeneratedJwtId();
        claims.setExpirationTime(NumericDate.fromMilliseconds(expiresAt.toEpochMilli()));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(PRIVATE_KEY);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setHeader("typ", "JWT");
        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Verifies signature, issuer and expiry; empty when the token is invalid, expired or malformed. */
    public static Optional<JwtClaims> verify(String jwt) {
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setRequireSubject()
                .setExpectedIssuer(ISSUER)
                .setVerificationKey(PUBLIC_KEY)
                .setAllowedClockSkewInSeconds(CLOCK_SKEW_SECONDS)
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.RSA_USING_SHA256))
                .build();
        try {
            return Optional.of(consumer.processToClaims(jwt));
        } catch (InvalidJwtException e) {
            return Optional.empty();
        }
    }

    /** The token's subject (player name) when the token is valid. */
    public static Optional<String> subject(String jwt) {
        return verify(jwt).map(TokenService::subjectOf).filter(Objects::nonNull);
    }

    /** The token's {@code groups} claim (role names) when the token is valid; empty list otherwise. */
    public static List<String> groups(String jwt) {
        return verify(jwt).map(TokenService::groupsOf).orElse(List.of());
    }

    /** Subject of already-verified claims. */
    public static String subjectOf(JwtClaims claims) {
        try {
            return claims.getSubject();
        } catch (MalformedClaimException e) {
            return null;
        }
    }

    /** {@code iat} of already-verified claims, in epoch seconds; 0 if absent/malformed. */
    public static long issuedAtSecondsOf(JwtClaims claims) {
        try {
            return claims.getIssuedAt() == null ? 0L : claims.getIssuedAt().getValue();
        } catch (MalformedClaimException e) {
            return 0L;
        }
    }

    /** {@code groups} claim of already-verified claims; never null. */
    public static List<String> groupsOf(JwtClaims claims) {
        try {
            List<String> groups = claims.getStringListClaimValue(GROUPS_CLAIM);
            return groups == null ? List.of() : groups;
        } catch (MalformedClaimException e) {
            return List.of();
        }
    }

    private static PrivateKey loadPrivateKey() {
        byte[] der = decodePem(readKeyMaterial("JWT_PRIVATE_KEY_FILE", DEV_PRIVATE_KEY_RESOURCE), "PRIVATE KEY");
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to load JWT signing (private) key", e);
        }
    }

    private static PublicKey loadPublicKey() {
        byte[] der = decodePem(readKeyMaterial("JWT_PUBLIC_KEY_FILE", DEV_PUBLIC_KEY_RESOURCE), "PUBLIC KEY");
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to load JWT verification (public) key", e);
        }
    }

    private static String readKeyMaterial(String envVar, String classpathResource) {
        String file = System.getenv(envVar);
        if (file != null && !file.isBlank()) {
            try {
                logger.info("Loading JWT key from {}={}", envVar, file);
                return Files.readString(Path.of(file), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read JWT key file " + file + " (" + envVar + ")", e);
            }
        }
        try (InputStream in = TokenService.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalStateException("Bundled dev JWT key " + classpathResource + " not found on classpath");
            }
            logger.info("{} unset — using bundled dev JWT key {}", envVar, classpathResource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read bundled dev JWT key " + classpathResource, e);
        }
    }

    private static byte[] decodePem(String pem, String label) {
        String base64 = pem
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
