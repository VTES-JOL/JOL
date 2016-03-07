package deckserver.dwr.bean;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import uk.ltd.getahead.dwr.WebContextFactory;
import util.Shuffle;
import cards.local.*;
import cards.model.CardEntry;
import deckserver.servlet.DeckServlet;
import deckserver.util.DeckParams;
import deckserver.util.MailUtil;

import nbclient.vtesmodel.JolAdminFactory;

public class DeckEditBean {

	private String text = null;

	private String format = null;

	private String[] errors = null;

	private int lib = 0, crypt = 0;

	private String groups = null;

	public DeckEditBean(String player, String name) {
		JolAdminFactory admin = JolAdminFactory.INSTANCE;
		String deck = admin.getDeck(player, name);
		text = init(player, name, deck, false);
	}

	private String init(String player, String name, String deck, boolean shuffle) {
		JolAdminFactory admin = JolAdminFactory.INSTANCE;
		NormalizeDeck nd = NormalizeDeckFactory.constructDeck(admin
				.getBaseCards(), deck);
		try {
			HttpServletRequest request = WebContextFactory.get()
					.getHttpServletRequest();
			if (shuffle) {
				Map<String, TreeMap<CardEntry, Integer>> map = DeckServlet
						.getDeckHtmlMap(nd);
				Collection<CardEntry> c = new ArrayList<CardEntry>();
				Collection<CardEntry> v = new ArrayList<CardEntry>();
				nd.getCards();
				for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
					String t = i.next();
					Collection<CardEntry> l = v;
					if (t.equalsIgnoreCase("Vampire")
							|| t.equalsIgnoreCase("Imbued")) {
						l = c;
					}
					for (Iterator<CardEntry> j = map.get(t).keySet().iterator(); j
							.hasNext();) {
						CardEntry card = j.next();
						int num = map.get(t).get(card).intValue();
						for (int k = 0; k < num; k++)
							l.add(card);
					}
				}
				CardEntry[] carr = c.toArray(new CardEntry[0]);
				carr = (CardEntry[]) Shuffle.shuffle(carr);
				CardEntry[] larr = v.toArray(new CardEntry[0]);
				larr = (CardEntry[]) Shuffle.shuffle(larr);
				Map<String, CardEntry[]> dp = new HashMap<String, CardEntry[]>();
				dp.put("crypt", carr);
				dp.put("library", larr);
				request.setAttribute("sparams", dp);
				format = WebContextFactory.get().forwardToString(
						"/WEB-INF/jsps/topframe/shuffle.jsp");
			} else {
				DeckParams dp = new DeckParams(null, null, null, null, nd);
				request.setAttribute("dparams", dp);
				format = WebContextFactory.get().forwardToString(
						"/WEB-INF/jsps/topframe/showdeck.jsp");
			}
		} catch (Exception e) {
		    String msg = "Error in deck " + name + " for player " + player;
                        MailUtil.sendError(msg,e);
			format = "Error parsing deck.";
		}
		lib = nd.getLibSize();
		crypt = nd.getCryptSize();
		groups = nd.getGroups();
		errors = nd.getErrorLines();
		return nd.getDeckString();
	}

	public DeckEditBean(String deck, boolean doshuffle) {
		init(null, null, deck, doshuffle);
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
