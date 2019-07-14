package net.deckserver.game.storage.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class LibraryCardMode {
    /**
     * Disciplines required to use this mode.
     */
    private List<String> disciplines;

    private String text;

    /**
     * Where the card is played, if not the ash heap.
     */
    private String targetRegion;
}
