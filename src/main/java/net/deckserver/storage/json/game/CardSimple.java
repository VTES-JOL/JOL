package net.deckserver.storage.json.game;

import lombok.Data;

@Data
public class CardSimple {
    String id;
    String name;
    String owner;
    String region;

    public CardSimple() {
    }

    public CardSimple(String cardId, String name, String ownerName, String region) {
        this.id = cardId;
        this.name = name;
        this.owner = ownerName;
        this.region = region;
    }
}
