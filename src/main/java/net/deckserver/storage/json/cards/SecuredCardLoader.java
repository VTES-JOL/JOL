package net.deckserver.storage.json.cards;

import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SecuredCardLoader {

    private final String resourceUrl;
    private final String keyPairId;
    private final String keyPairPath;

    public SecuredCardLoader() {
        this.resourceUrl = System.getenv("CARD_URL");
        this.keyPairId = System.getenv("CARD_KEY_ID");
        this.keyPairPath = System.getenv("CARD_KEY_FILE");
    }

    public URL generateSignedUrl() throws Exception {
        CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();
        Instant expirationDate = Instant.now().plus(7, ChronoUnit.DAYS);
        CannedSignerRequest cannedRequest = CannedSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .privateKey(Paths.get(keyPairPath))
                .keyPairId(keyPairId)
                .expirationDate(expirationDate)
                .build();
        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedRequest);
        return URI.create(signedUrl.url()).toURL();
    }
}
