package net.deckserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Recaptcha {

    private static final String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private static final String secret = System.getenv("JOL_RECAPTCHA_SECRET");
    private static final Logger logger = LoggerFactory.getLogger(Recaptcha.class);

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
            JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            return jsonObject.getBoolean("success");
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
