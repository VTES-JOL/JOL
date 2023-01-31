package net.deckserver.storage.json.deck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedDeck {
    private Deck deck;
    private DeckStats stats;
}
