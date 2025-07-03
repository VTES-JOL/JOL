package net.deckserver.game.storage.cards;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardSet {
    private String id;
    private String code;
    private String name;
}
