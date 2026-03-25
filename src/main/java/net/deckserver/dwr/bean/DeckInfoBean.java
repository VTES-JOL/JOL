package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.services.DeckService;
import net.deckserver.storage.json.deck.Crypt;

import java.util.Set;

public class DeckInfoBean {
    private final DeckFormat deckFormat;
    @Getter
    private final String name;
    @Getter
    private final Set<String> gameFormats;
    @Getter
    private final String comments;
    @Getter
    private final Set<String> clans;

    public DeckInfoBean(String playerName, String deckName) {
        this.name = deckName;
        this.deckFormat = JolAdmin.getDeckFormat(playerName, deckName);
        this.gameFormats = JolAdmin.getTags(playerName, deckName);
        this.comments = JolAdmin.getDeckComment(playerName, deckName).split("\n")[0];
        this.clans = JolAdmin.getCryptClans(playerName, deckName);
    }

    public String getDeckFormat() {
        return deckFormat.toString();
    }

}

