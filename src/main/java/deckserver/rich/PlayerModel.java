package deckserver.rich;

import deckserver.dwr.bean.DeckSummaryBean;
import deckserver.util.MailUtil;
import nbclient.vtesmodel.JolAdminFactory;

import java.util.*;

public class PlayerModel implements Comparable {

    private final String player;
    private String view;
    private String[] games = new String[0];
    private String game = null;
    private Collection<String> chats = new ArrayList<String>();
    private String tmpDeck;
    private String tmpDeckName;
    private DeckSummaryBean[] decks = null;
    private Collection<String> removedGames = new ArrayList<String>(2);
    private Collection<String> changedGames = new ArrayList<String>();

    public PlayerModel(AdminBean abean, String name, List<String> chatin) {
        this.player = name;
        setView("main");
        for (Iterator<GameModel> i = abean.getActiveGames().iterator();
             i.hasNext(); ) {
            changedGames.add(i.next().getName());
        }
        chats.addAll(chatin);
    }

    public String getPlayer() {
        return player;
    }

    public String[] getCurrentGames() {
        return games;
    }

    public String getCurrentGame() {
        return game;
    }

    public void enterGame(AdminBean abean, String game) {
        setView("game");
        //		GameModel model = abean.getGameModel(game);
        Collection<String> c = new ArrayList<String>(Arrays.asList(games));
        if ( /*model.getPlayers().contains(player) && */!c.contains(game)) {
            c.add(game);
            games = (String[]) c.toArray(games);
        }
        if (!game.equals(this.game)) {
            abean.getGameModel(game).resetView(player);
        }
        this.game = game;
    }

    public void leaveGame(String game) {
        Collection<String> c = Arrays.asList(games);
        c.remove(game);
        games = c.toArray(games);
        if (game.equals(this.game))
            this.game = null;
        if (games.length > 0)
            this.game = games[0];
    }

    public void recordAccess() {
        if (player != null)
            JolAdminFactory.INSTANCE.recordAccess(player);
    }

    public long getTimestamp() {
        return JolAdminFactory.INSTANCE.getLastAccess(player).getTime();
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public synchronized void chat(String chat) {
        chats.add(chat);
    }

    public synchronized String[] getChat() {
        String[] ret = (String[]) chats.toArray(new String[0]);
        chats.clear();
        return ret;
    }

    public int compareTo(Object arg0) {
        return player.compareToIgnoreCase(((PlayerModel) arg0).getPlayer());
    }

    public void setTmpDeck(String name, String deck) {
        this.tmpDeckName = name;
        this.tmpDeck = deck;
    }

    public void clearDeck() {
        this.tmpDeckName = null;
        this.tmpDeck = null;
    }

    public void saveDeck() {
        if (tmpDeckName != null) {
            JolAdminFactory.INSTANCE.createDeck(player, tmpDeckName, tmpDeck);
        }
    }

    public void submitDeck(String name, String deck) {
        clearDeck();
        JolAdminFactory.INSTANCE.createDeck(player, name, deck);
        decks = null;
    }

    // TODO optimize deck cache???

    public DeckSummaryBean[] getDecks() {
        if (decks == null) {
            String[] names = JolAdminFactory.INSTANCE.getDeckNames(player);
            Arrays.sort(names);
            Collection<DeckSummaryBean> c = new ArrayList<DeckSummaryBean>(names.length);
            for (int i = 0; i < names.length; i++) {
                try {
                    c.add(new DeckSummaryBean(this, names[i]));
                } catch (Throwable t) {
                    MailUtil.sendError("DeckSummaryBean Error for " + player +
                            " and deck " + names[i], t);
                }

            }
            decks = c.toArray(new DeckSummaryBean[0]);
        }
        return decks;
    }

    public boolean isAdmin() {
        return JolAdminFactory.INSTANCE.isAdmin(player);
    }

    public boolean isSuper() {
        return JolAdminFactory.INSTANCE.isSuperUser(player);
    }

    public boolean hasChats() {
        return !chats.isEmpty();
    }

    public void removeGame(String name) {
        removedGames.add(name);
    }

    public void changeGame(String name) {
        changedGames.add(name);
    }

    public Collection<String> getChangedGames() {
        return changedGames;
    }

    public Collection<String> getRemovedGames() {
        return removedGames;
    }

    public void clearGames() {
        changedGames.clear();
        removedGames.clear();
    }
}
