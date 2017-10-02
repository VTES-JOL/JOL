package net.deckserver.storage;

import lombok.Data;

@Data
public class DeckItem {
    private String cardId;
    private Integer count;
    private String comment;
}
