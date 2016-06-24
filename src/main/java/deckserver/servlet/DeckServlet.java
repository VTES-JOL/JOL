/*
 * DeckServlet.java
 *
 * Created on March 8, 2004, 10:02 PM
 */

package deckserver.servlet;

import deckserver.interfaces.NormalizeDeck;
import deckserver.cards.NormalizeDeckFactory;
import deckserver.interfaces.CardEntry;
import deckserver.interfaces.CardSearch;
import deckserver.interfaces.CardSet;
import deckserver.interfaces.Deck;
import deckserver.util.AdminFactory;
import deckserver.util.DeckParams;
import deckserver.util.WebParams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Joe User
 */
public class DeckServlet extends GameServlet {

    /**
     *
     */
    private static final long serialVersionUID = 4431615563798940125L;

    public static int sumMap(Collection<?> c) {
        if (c == null) return 0;
        int ret = 0;
        for (Object aC : c) ret += (Integer) aC;
        return ret;
    }

    public static Map<String, TreeMap<CardEntry, Integer>> getDeckHtmlMap(DeckParams params) {
        return getDeckHtmlMap(params.getDeckObj());
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

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    protected void processRequest(WebParams params, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String player = params.getPlayer();
        String deck = request.getParameter("deck");
        String deckname = request.getParameter("deckname");
        String editdeck = request.getParameter("editdeck");
        String noinit = request.getParameter("editinit");
        if (editdeck != null && noinit == null) {
            deck = admin.getDeck(player, editdeck);
            if (deck != null) deckname = editdeck;
        }
        if (deck == null) deck = "";
        if (deck.equals("Your deck here")) deck = "";

        CardSearch cs = AdminFactory.get(getServletContext()).getBaseCards();
        NormalizeDeck nd = NormalizeDeckFactory.getNormalizer(cs, deck);
        if (request.getParameter("submit") != null && player != null && deckname != null && deckname.length() > 0) {
            if (admin.createDeck(player, deckname, nd.getFilteredDeck())) {
                getServletContext().getRequestDispatcher("/toplayer.jsp").forward(request, response);
            } else {
                params.addStatusMsg("Deck submission failed.");
            }
        }

        String[] newcards = request.getParameterValues("newcard");
        if (newcards != null) {
            for (String newcard : newcards) {
                CardEntry card = cs.getCardById(newcard);
                nd.addCard(card);
            }
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String type = request.getParameter("type");
        String query = request.getParameter("query");
        if (query == null) query = "";
        if (type == null) type = "All";
        CardEntry[] entries = new CardEntry[0];
        if (query.length() > 0) {
            CardSet set = type.equals("All") ? cs.getAllCards() : cs.searchByType(cs.getAllCards(), type);
            set = cs.searchByText(set, query);
            CardSet set2 = NormalizeDeckFactory.findCardName(cs, query, new LinkedList<>());
            Collection<CardEntry> c = new HashSet<>(Arrays.asList(set.getCardArray()));
            c.addAll(Arrays.asList(set2.getCardArray()));
            entries = c.toArray(entries);
        }
        DeckParams p = new DeckParams(deckname, type, query, entries, nd);
        request.setAttribute("dparams", p);
        try {
            getServletContext().getRequestDispatcher("/WEB-INF/jsps/deckconstruction.jsp").include(request, response);
        } catch (Throwable t) {
            String msg = "Error in deck " + deckname + " for player " + player;
            throw new IOException(msg, t);
        }
        out.close();
    }

    public String getServletInfo() {
        return "Deck Creation Servlet";
    }

}
