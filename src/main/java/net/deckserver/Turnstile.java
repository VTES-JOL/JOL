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

/**
 * Server-side verification of a Cloudflare Turnstile token (the widget the
 * React register form renders — see {@code pages/login/TurnstileWidget.tsx}).
 * The secret comes from {@code JOL_TURNSTILE_SECRET}; the legacy
 * {@code JOL_RECAPTCHA_SECRET} name is still read as a fallback so an existing
 * deployment's env file keeps working across the rename.
 */
public class Turnstile {

    private static final String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private static final String secret = firstNonBlank(
            System.getenv("JOL_TURNSTILE_SECRET"), System.getenv("JOL_RECAPTCHA_SECRET"));
    private static final Logger logger = LoggerFactory.getLogger(Turnstile.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static boolean verify(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            BufferedReader in = getBufferedReader(token);
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
            logger.error("Unable to verify Turnstile token", e);
            return false;
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return b;
    }

    private static BufferedReader getBufferedReader(String token) throws URISyntaxException, IOException {
        URL obj = new URI(url).toURL();
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // add request header
        con.setRequestMethod("POST");

        String postParams = "secret=" + secret + "&response=" + token;

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
