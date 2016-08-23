package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.Utils;
import deckserver.game.cards.CardEntry;
import deckserver.game.cards.Deck;
import deckserver.game.cards.DeckFactory;
import deckserver.util.DeckParams;
import org.directwebremoting.WebContextFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class DeckEditBean {

    private String text = null;

    private String format = null;

    private String[] errors = null;

    private int lib = 0, crypt = 0;

    private String groups = null;

    public DeckEditBean(String player, String name) {
        JolAdmin admin = JolAdmin.INSTANCE;
        String deck = admin.getDeck(player, name);
        text = init(player, name, deck, false);
    }

    public DeckEditBean(String deck, boolean doshuffle) {
        init(null, null, deck, doshuffle);
    }

    private String init(String player, String name, String deck, boolean shuffle) {
        JolAdmin admin = JolAdmin.INSTANCE;
        Deck nd = DeckFactory.constructDeck(admin
                .getAllCards(), deck);
        try {
            HttpServletRequest request = WebContextFactory.get()
                    .getHttpServletRequest();
            if (shuffle) {
                Map<String, TreeMap<CardEntry, Integer>> map = Utils
                        .getDeckHtmlMap(nd);
                Collection<CardEntry> c = new ArrayList<>();
                Collection<CardEntry> v = new ArrayList<>();
                nd.getCards();
                for (String t : map.keySet()) {
                    Collection<CardEntry> l = v;
                    if (t.equalsIgnoreCase("Vampire")
                            || t.equalsIgnoreCase("Imbued")) {
                        l = c;
                    }
                    for (CardEntry card : map.get(t).keySet()) {
                        int num = map.get(t).get(card);
                        for (int k = 0; k < num; k++)
                            l.add(card);
                    }
                }
                CardEntry[] carr = c.toArray(new CardEntry[0]);
                Utils.shuffle(carr);
                CardEntry[] larr = v.toArray(new CardEntry[0]);
                Utils.shuffle(larr);
                Map<String, CardEntry[]> dp = new HashMap<>();
                dp.put("crypt", carr);
                dp.put("library", larr);
                request.setAttribute("sparams", dp);
                format = WebContextFactory.get().forwardToString(
                        "/WEB-INF/jsps/shuffle.jsp");
            } else {
                DeckParams dp = new DeckParams(null, null, null, null, nd);
                request.setAttribute("dparams", dp);
                format = WebContextFactory.get().forwardToString(
                        "/WEB-INF/jsps/dwrdeck.jsp");
            }
        } catch (Exception e) {
            String msg = "Error in deck " + name + " for player " + player;
            format = "Error parsing deck.";
        }
        lib = nd.getLibSize();
        crypt = nd.getCryptSize();
        groups = nd.getGroups();
        errors = nd.getErrorLines();
        return nd.getDeckString();
    }

    public String[] getErrors() {
        return errors;
    }

    public String getFormat() {
        return format;
    }

    public String getText() {
        return text;
    }

    public int getCrypt() {
        return crypt;
    }

    public int getLib() {
        return lib;
    }

    public String getGroups() {
        return groups;
    }
}
