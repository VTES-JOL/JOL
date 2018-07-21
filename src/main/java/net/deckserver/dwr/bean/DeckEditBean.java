package net.deckserver.dwr.bean;

import net.deckserver.Utils;
import net.deckserver.dwr.jsp.DeckParams;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.Deck;
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
        JolAdmin admin = JolAdmin.getInstance();
        String deck = admin.getDeck(player, name);
        text = init(player, name, deck, false);
    }

    public DeckEditBean(String deck, boolean doshuffle) {
        init(null, null, deck, doshuffle);
    }

    private String init(String player, String name, String deck, boolean shuffle) {
        JolAdmin admin = JolAdmin.getInstance();
        Deck nd = new Deck(admin.getAllCards(), deck);
        try {
            HttpServletRequest request = WebContextFactory.get()
                    .getHttpServletRequest();
            if (shuffle) {
                Map<String, TreeMap<CardEntry, Integer>> map = Utils
                        .getDeckHtmlMap(nd);
                List<CardEntry> c = new ArrayList<>();
                List<CardEntry> v = new ArrayList<>();
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
                Utils.shuffle(c);
                Utils.shuffle(v);
                Map<String, List<CardEntry>> dp = new HashMap<>();
                dp.put("crypt", c);
                dp.put("library", v);
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
        return deck;
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
