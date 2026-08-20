package net.deckserver.dwr.model;

import lombok.Getter;
import lombok.Setter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.ChatEntryBean;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.services.DeckService;
import net.deckserver.services.GlobalChatService;
import net.deckserver.storage.json.deck.DeckParser;
import net.deckserver.storage.json.deck.ExtendedDeck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class PlayerModel {

    private final static Logger logger = LoggerFactory.getLogger(PlayerModel.class);
    private final String player;
    private String lastSeenChat = null;
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
    private String deckFilter = "ALL";

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

    public String getCurrentGame() {
        return game;
    }

    public void enterGame(String gameName) {
        setView("game");
        if (!gameName.equals(this.game)) {
            JolAdmin.resetView(player, gameName);
        }
        this.game = gameName;
    }

    public void setView(String view) {
        this.view = view;
        this.game = null;
    }

    public List<ChatEntryBean> getChat() {
        List<ChatEntryBean> output = GlobalChatService.getChatsSince(lastSeenChat);
        if (!output.isEmpty()) {
            lastSeenChat = output.get(output.size() - 1).getTimestamp();
        }
        return output;
    }

    public void resetChats() {
        lastSeenChat = null;
    }

    /** Marks a batch of history (already shown to the player some other way) as seen, so a
     * subsequent getChat() only returns what's genuinely new after it — not a re-delivery. */
    public void markChatsSeenThrough(List<ChatEntryBean> entries) {
        if (!entries.isEmpty()) {
            lastSeenChat = entries.get(entries.size() - 1).getTimestamp();
        }
    }

    public boolean hasChats() {
        return GlobalChatService.hasChatsSince(lastSeenChat);
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
