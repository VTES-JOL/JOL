package net.deckserver;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.jsp.DeckParams;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.Deck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final String url = "https://www.google.com/recaptcha/api/siteverify";
    private static final String secret = System.getenv("JOL_RECAPTCHA_SECRET");
    private final static String USER_AGENT = "Mozilla/5.0";


    public static String getPlayer(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("meth");
    }

    private static void setPlayer(HttpServletRequest request, String player) {
        request.getSession().setAttribute("meth", player);
    }

    public static PlayerModel getPlayerModel(HttpServletRequest request, AdminBean abean) {
        String player = getPlayer(request);
        PlayerModel model;
        if (player == null) {
            model = (PlayerModel) request.getSession().getAttribute("guest");
            if (model == null) {
                model = abean.getPlayerModel(null);
                request.getSession().setAttribute("guest", model);
            }
        } else {
            model = abean.getPlayerModel(player);
        }
        return model;
    }

    public static void checkParams(HttpServletRequest request, ServletContext ctx) {
        AdminBean abean = AdminBean.INSTANCE;
        String player = (String) request.getSession().getAttribute("meth");
        String login = request.getParameter("login");
        logger.trace("Get request {} from player {}", request.getRequestURI(), player);
        if (login != null) {
            if (login.equals("Log in")) {
                player = request.getParameter("dsuserin");
                String password = request.getParameter("dspassin");
                if (player != null
                        && JolAdmin.getInstance().authenticate(player,
                        password)) {
                    setPlayer(request, player);
                    logger.debug("Logged in player {}", player);
                } else {
                    logger.debug("Log in failed for player {}", player);
                    player = null;
                }
            } else if (login.equals("Log out")) {
                logger.debug("Log out: " + player);
                request.getSession().removeAttribute("meth");
                abean.remove(player);
                player = null;
            }
        } else if ("Register".equals(request.getParameter("register"))) {
            player = request.getParameter("newplayer");
            String email = request.getParameter("newemail");
            String password = request.getParameter("newpassword");
            String captchaResponse = request.getParameter("g-recaptcha-response");
            try {
                boolean verify = verify(captchaResponse);
                if (verify && JolAdmin.getInstance().registerPlayer(player, password, email)) {
                    setPlayer(request, player);
                    logger.debug("registered " + player);
                } else {
                    logger.error("registration failed for " + player);
                    player = null;
                }
            } catch (IOException e) {
                logger.error("Unable to verify recaptcha", e);
                player = null;
            }
        }
        PlayerModel model = getPlayerModel(request, abean);
        if (player == null) {
            request.getSession().setAttribute("guest", model);
        }
        String gamename = request.getPathInfo();
        if (gamename != null && gamename.length() > 0)
            gamename = gamename.substring(1);
        if (gamename != null && gamename.length() > 0
                && JolAdmin.getInstance().existsGame(gamename)) {
            logger.debug("Setting game to be " + gamename);

            model.enterGame(abean, gamename);
        }
    }

    public static String sanitizeName(String name) {
        StringCharacterIterator it = new StringCharacterIterator(name);
        StringBuilder res = new StringBuilder();
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (Character.isJavaIdentifierPart(c))
                res.append(c);
            else if (Character.isWhitespace(c))
                res.append(' ');
        }
        return res.toString();
    }

    public static void shuffle(Object[] obj, int num) {
        Random rnd = ThreadLocalRandom.current();
        if (num <= 0 || num > obj.length) {
            num = obj.length;
        }
        for (int i = num - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            Object a = obj[index];
            obj[index] = obj[i];
            obj[i] = a;
        }
    }

    public static void shuffle(Object[] obj) {
        shuffle(obj, 0);
    }

    public static Map<String, TreeMap<CardEntry, Integer>> getDeckHtmlMap(final Deck deck) {
        CardEntry[] cards = deck.getCards();
        Comparator<CardEntry> comp = (c1, c2) -> {
            int i1 = deck.getQuantity(c1);
            int i2 = deck.getQuantity(c2);
            if (i1 == i2) {
                return c1.getName().compareTo(c2.getName());
            }
            return i2 - i1;
        };
        Map<String, TreeMap<CardEntry, Integer>> ret = new HashMap<>();
        for (CardEntry card : cards) {
            String type = card.getType();
            if (!ret.containsKey(type)) ret.put(type, new TreeMap<>(comp));
            ret.get(type).put(card, deck.getQuantity(card));
        }
        return ret;
    }

    public static Map<String, TreeMap<CardEntry, Integer>> getDeckHtmlMap(DeckParams params) {
        return getDeckHtmlMap(params.getDeckObj());
    }

    public static int sumMap(Collection<Integer> c) {
        if (c == null) return 0;
        return c.stream().mapToInt(i -> i).sum();
    }

    public static int calc(OffsetDateTime from) {
        OffsetDateTime to = OffsetDateTime.now();
        long interval = Duration.between(from, to).getSeconds();
        if (interval < 10000) return 5000;
        if (interval < 60000) return 10000;
        if (interval < 300000) return 30000;
        return 60000;
    }

    public static boolean verify(String gRecaptchaResponse) throws IOException {
        if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
            return false;
        }

        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            // add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String postParams = "secret=" + secret + "&response=" + gRecaptchaResponse;

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            logger.trace("\nSending 'POST' request to URL : " + url);
            logger.trace("Post parameters : " + postParams);
            logger.trace("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            logger.trace(response.toString());

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
}
