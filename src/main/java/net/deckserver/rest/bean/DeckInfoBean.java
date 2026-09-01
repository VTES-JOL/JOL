package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.DeckFormat;

import java.util.Set;

public class DeckInfoBean {
    private final DeckFormat deckFormat;
    @Getter
    private final String name;
    /** Stable per-deck id — the key the SPA deck editor addresses decks by. */
    @Getter
    private final String deckId;
    @Getter
    private final Set<String> gameFormats;
    @Getter
    private final String comments;

    public DeckInfoBean(String playerName, String deckName) {
        this.name = deckName;
        this.deckId = JolAdmin.getDeckId(playerName, deckName);
        this.deckFormat = JolAdmin.getDeckFormat(playerName, deckName);
        this.gameFormats = JolAdmin.getTags(playerName, deckName);
        if (this.deckFormat != DeckFormat.LEGACY) {
            this.comments = JolAdmin.getDeckComment(playerName, deckName).split("\n")[0];
        } else {
            this.comments = "";
        }
    }

    public String getDeckFormat() {
        return deckFormat.toString();
    }

}

