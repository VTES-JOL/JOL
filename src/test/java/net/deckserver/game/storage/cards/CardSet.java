package net.deckserver.game.storage.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CardSet extends BaseCard {
    private String id;
    private String code;
    private String name;
}
