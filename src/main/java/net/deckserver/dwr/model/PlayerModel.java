package net.deckserver.dwr.model;

import lombok.Getter;
import lombok.Setter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.services.DeckService;
import net.deckserver.services.GlobalChatService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.deck.DeckParser;
import net.deckserver.storage.json.deck.ExtendedDeck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class PlayerModel {

    private final static Logger logger = LoggerFactory.getLogger(PlayerModel.class);
    private final String player;
    private final Set<String> games = new HashSet<>();
    private final List<ChatEntryBean> chats = new ArrayList<>();
    private String game = null;
    @Getter
    private String view;
    @Setter
    private String message;
    @Getter
    @Setter
    private ExtendedDeck deck;
    @Getter
    @Setter
    private String contents;
    @Getter
    @Setter
    private String deckFilter = GameFormat.STANDARD.getLabel();

    public PlayerModel(String name, boolean loadChat) {
        this.player = name;
        setView("main");
        if (loadChat) {
            resetChats();
        }
    }

    public String getPlayerName() {
        return player;
    }

    public Set<String> getCurrentGames() {
        return games;
    }

    public String getCurrentGame() {
        return game;
    }

    public void enterGame(String gameName) {
        setView("game");
        if (RegistrationService.isInGame(gameName, player)) {
            games.add(gameName);
        }
        if (!gameName.equals(this.game)) {
            JolAdmin.getGameModel(gameName).resetView(player);
        }
        this.game = gameName;
    }

    public void removeGame(String gameName) {
        games.remove(gameName);
    }

    public void setView(String view) {
        this.view = view;
        this.game = null;
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

    public void resetChats() {
        List<ChatEntryBean> globalChat = GlobalChatService.getChats();
        chats.addAll(globalChat);
    }

    public boolean hasChats() {
        return !chats.isEmpty();
    }

    public void loadDeck(String deckName) {
        try {
            String deckId = JolAdmin.getDeckId(player, deckName);
            DeckFormat deckFormat = JolAdmin.getDeckFormat(player, deckName);
            if (deckFormat.equals(DeckFormat.LEGACY)) {
                this.contents = DeckService.getLegacyContents(deckId).trim();
                this.deck = DeckParser.parseDeck(contents);
            } else {
                this.contents = DeckService.getDeckContents(deckId).trim();
                this.deck = DeckService.getDeck(deckId);
            }
            this.deck.getDeck().setName(deckName);
        } catch (IOException e) {
            logger.error("Unable to set deck", e);
            this.deck = null;
            this.contents = null;
        }
    }

    public void clearDeck() {
        this.deck = null;
        this.contents = null;
    }

    public String getMessage() {
        String result = this.message != null ? this.message : null;
        this.message = null;
        return result;
    }

}
