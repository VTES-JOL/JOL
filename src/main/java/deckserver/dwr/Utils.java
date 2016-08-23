package deckserver.dwr;

import deckserver.client.JolAdmin;
import deckserver.dwr.bean.AdminBean;
import deckserver.game.cards.CardEntry;
import deckserver.game.cards.Deck;
import deckserver.util.DeckParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private static final DateFormat format = new SimpleDateFormat("d-MMM HH:mm zz ");
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getPlayer(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("meth");
    }

    private static void setPlayer(HttpServletRequest request, String player) {
        request.getSession().setAttribute("meth", player);
    }

    static PlayerModel getPlayerModel(HttpServletRequest request, AdminBean abean) {
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

    static void checkParams(HttpServletRequest request, ServletContext ctx) {
        AdminBean abean = AdminBean.INSTANCE;
        String player = (String) request.getSession().getAttribute("meth");
        String login = request.getParameter("login");
        logger.trace("Get request {} from player {}", request.getRequestURI(), player);
        if (login != null) {
            if (login.equals("Log in")) {
                player = request.getParameter("dsuserin");
                String password = request.getParameter("dspassin");
                if (player != null
                        && JolAdmin.INSTANCE.authenticate(player,
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
            if (JolAdmin.INSTANCE.registerPlayer(player, password, email)) {
                setPlayer(request, player);
                logger.debug("registered " + player);
            } else {
                logger.error("registration failed for " + player);
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
                && JolAdmin.INSTANCE.existsGame(gamename)) {
            logger.debug("Setting game to be " + gamename);

            model.enterGame(abean, gamename);
        }
    }

    static public String getDate() {
        return Utils.format.format(new Date());
    }

    static String sanitizeName(String name) {
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
}
