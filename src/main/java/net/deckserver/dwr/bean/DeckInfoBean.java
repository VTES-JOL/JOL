package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.storage.json.system.DeckFormat;

public class DeckInfoBean {
    private final DeckFormat deckFormat;
    private final String name;

    public DeckInfoBean(String playerName, String deckName) {
        JolAdmin admin = JolAdmin.INSTANCE;
        this.name = deckName;
        this.deckFormat = admin.getDeckFormat(playerName, deckName);
    }

    public String getDeckFormat() { return deckFormat.toString(); }

    public String getName() {
        return name;
    }

}

