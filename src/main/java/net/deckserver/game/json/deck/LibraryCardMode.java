package net.deckserver.game.json.deck;

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

    public enum Target {
        READY_REGION, SELF
    }

    /**
     * Where the card is played, if not the ash heap.
     */
    private Target target;
}
