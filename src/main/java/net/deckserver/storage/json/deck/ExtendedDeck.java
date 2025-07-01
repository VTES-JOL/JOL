package net.deckserver.storage.json.deck;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedDeck {
    private Deck deck;
    private DeckStats stats;
    private List<String> errors = new ArrayList<>();
}
