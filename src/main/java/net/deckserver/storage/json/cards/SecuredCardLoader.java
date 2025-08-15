package net.deckserver.storage.json.cards;

import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SecuredCardLoader {

    private final String baseUrl;
    private final String keyPairId;
    private final String keyPairPath;
    private final String resourceUrl;

    public SecuredCardLoader(String resourceUrl) {
        this.baseUrl = System.getenv("BASE_URL");
        this.keyPairId = System.getenv("KEY_ID");
        this.keyPairPath = System.getenv("KEY_FILE");
        this.resourceUrl = resourceUrl;
    }

    public URL generateSignedUrl() throws Exception {
        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(buildRequest());
        return URI.create(signedUrl.url()).toURL();
    }

    private CannedSignerRequest buildRequest() throws Exception {
        Instant expirationDate = Instant.now().plus(7, ChronoUnit.DAYS);
        return CannedSignerRequest.builder()
                .resourceUrl(baseUrl + resourceUrl)
                .privateKey(Paths.get(keyPairPath))
                .keyPairId(keyPairId)
                .expirationDate(expirationDate)
                .build();
    }

    public CookiesForCannedPolicy generateCookies() throws Exception {
        return CloudFrontUtilities.create().getCookiesForCannedPolicy(buildRequest());
    }
}
