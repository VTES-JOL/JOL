package net.deckserver.dwr.model;

import net.deckserver.dwr.bean.ChatEntryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerModel implements Comparable {

    private static Logger logger = LoggerFactory.getLogger(PlayerModel.class);
    private final String player;
    private String view;
    private String[] games = new String[0];
    private String game = null;
    private List<ChatEntryBean> chats = new ArrayList<>();
    private String tmpDeck;
    private String tmpDeckName;
    private Collection<String> changedGames = new HashSet<>();

    public PlayerModel(String name, List<ChatEntryBean> chatin) {
        logger.trace("Creating new Player model for {}", name);
        this.player = name;
        setView("main");
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

    public void enterGame(String game) {
        setView("game");
        Collection<String> c = new ArrayList<>(Arrays.asList(games));
        if (!c.contains(game)) {
            c.add(game);
            games = c.toArray(games);
        }
        if (!game.equals(this.game)) {
            JolAdmin.getInstance().getGameModel(game).resetView(player);
        }
        this.game = game;
    }

    public void recordAccess() {
        if (player != null) JolAdmin.getInstance().recordPlayerAccess(player);
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public synchronized void chat(ChatEntryBean chat) {
        chats.add(chat);
    }

    public synchronized List<ChatEntryBean> getChat() {
        List<ChatEntryBean> output = new ArrayList<>(chats);
        Collections.copy(output, chats);
        chats.clear();
        return output;
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

    public void submitDeck(String name, String deck) {
        clearDeck();
        JolAdmin.getInstance().createDeck(player, name, deck);
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

    /**
    * Can this user promote others?
    **/
    public boolean canPromote() {
        return isSuperUser();
    }

    public boolean hasChats() {
        return !chats.isEmpty();
    }

    public void changeGame(String name) {
        changedGames.add(name);
    }

}
