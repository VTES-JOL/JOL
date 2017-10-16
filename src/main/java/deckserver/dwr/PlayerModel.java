package deckserver.dwr;

import deckserver.client.JolAdmin;
import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.DeckSummaryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerModel implements Comparable {

    private static Logger logger = LoggerFactory.getLogger(PlayerModel.class);
    private final String player;
    private String view;
    private String[] games = new String[0];
    private String game = null;
    private Collection<String> chats = new ArrayList<>();
    private String tmpDeck;
    private String tmpDeckName;
    private List<DeckSummaryBean> decks = new ArrayList<>();
    private Collection<String> removedGames = new ArrayList<>(2);
    private Collection<String> changedGames = new ArrayList<>();

    public PlayerModel(AdminBean abean, String name, List<String> chatin) {
        logger.trace("Creating new Player model for {}", name);
        this.player = name;
        setView("main");
        changedGames.addAll(abean.getActiveGames().stream().map(GameModel::getName).collect(Collectors.toList()));
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

    void enterGame(AdminBean abean, String game) {
        setView("game");
        Collection<String> c = new ArrayList<>(Arrays.asList(games));
        if (!c.contains(game)) {
            c.add(game);
            games = c.toArray(games);
        }
        if (!game.equals(this.game)) {
            abean.getGameModel(game).resetView(player);
        }
        this.game = game;
    }

    public void recordAccess() {
        if (player != null) JolAdmin.getInstance().recordAccess(player);
    }

    public long getTimestamp() {
        return JolAdmin.getInstance().getLastAccess(player).getTime();
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
        String[] ret = chats.toArray(new String[0]);
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
            JolAdmin.getInstance().createDeck(player, tmpDeckName, tmpDeck);
        }
    }

    public void removeDeck() {
        decks = null;
    }

    public void submitDeck(String name, String deck) {
        clearDeck();
        JolAdmin.getInstance().createDeck(player, name, deck);
        decks = null;
    }

    public List<String> getDecks() {
        return Arrays.asList(JolAdmin.getInstance().getDeckNames(player));
    }

    public boolean isAdmin() {
        return JolAdmin.getInstance().isAdmin(player);
    }

    public boolean isSuperUser() {
        return JolAdmin.getInstance().isSuperUser(player);
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
