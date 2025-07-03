package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.storage.json.system.DeckFormat;

import java.util.Set;

public class DeckInfoBean {
    private final DeckFormat deckFormat;
    @Getter
    private final String name;
    @Getter
    private final Set<String> gameFormats;

    public DeckInfoBean(String playerName, String deckName) {
        JolAdmin admin = JolAdmin.INSTANCE;
        this.name = deckName;
        this.deckFormat = admin.getDeckFormat(playerName, deckName);
        this.gameFormats = admin.getTags(playerName, deckName);
    }

    public String getDeckFormat() { return deckFormat.toString(); }

}

