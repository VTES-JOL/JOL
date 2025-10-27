package net.deckserver.push;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.Map;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(of = {"auth", "key", "endpoint"})
public class Subscription {

    private String auth;
    private String key;
    @Setter
    private String endpoint;

    public Subscription() {}

    // Safari/Chrome nested structure
    @JsonProperty("keys")
    private void unpackKeys(Map<String, String> keys) {
        if (keys != null) {
            if (keys.containsKey("auth")) {
                this.auth = keys.get("auth");
            }
            if (keys.containsKey("p256dh")) {
                this.key = keys.get("p256dh");
            }
        }
    }

    // Firefox legacy fields
    @JsonProperty("auth")
    public void setAuth(String auth) {
        this.auth = auth;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    // Handle Firefox's p256dh at top level
    @JsonProperty("p256dh")
    public void setP256dh(String p256dh) {
        if (this.key == null) {
            this.key = p256dh;
        }
    }

    /**
     * Returns the base64 encoded auth string as a byte[]
     */
    public byte[] getAuthAsBytes() {
        return Base64.getDecoder().decode(getAuth());
    }

    /**
     * Returns the base64 encoded public key string as a byte[]
     */
    public byte[] getKeyAsBytes() {
        return Base64.getDecoder().decode(getKey());
    }

    /**
     * Returns the base64 encoded public key as a PublicKey object
     */
    public PublicKey getUserPublicKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        KeyFactory kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
        ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        ECPoint point = ecSpec.getCurve().decodePoint(getKeyAsBytes());
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
        return kf.generatePublic(pubSpec);
    }
}
