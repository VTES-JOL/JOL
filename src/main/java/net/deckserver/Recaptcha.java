package net.deckserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Recaptcha {

    private static final String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private static final String secret = System.getenv("JOL_RECAPTCHA_SECRET");
    private static final Logger logger = LoggerFactory.getLogger(Recaptcha.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static boolean verify(String gRecaptchaResponse) {
        if (gRecaptchaResponse == null || gRecaptchaResponse.isEmpty()) {
            return false;
        }

        try {
            BufferedReader in = getBufferedReader(gRecaptchaResponse);
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //parse JSON response and return 'success' value
            JsonNode jsonNode = MAPPER.readTree(response.toString());
            return jsonNode.path("success").asBoolean();
        } catch (Exception e) {
            logger.error("Unable to verify recaptcha", e);
            return false;
        }
    }

    private static BufferedReader getBufferedReader(String gRecaptchaResponse) throws URISyntaxException, IOException {
        URL obj = new URI(url).toURL();
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // add request header
        con.setRequestMethod("POST");

        String postParams = "secret=" + secret + "&response=" + gRecaptchaResponse;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        return new BufferedReader(new InputStreamReader(con.getInputStream()));
    }
}
