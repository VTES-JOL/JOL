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
        /**
         * Played to player's ready region.
         */
        READY_REGION,
        /**
         * Played on the minion playing the card.
         * Presents a list of the player's ready and torpored minions for
         * selection.
         */
        SELF,
        /**
         * Played on any card on the table.
         * Used when a more specific parser for "Put this card on" is not
         * implemented. This target presents a list of all cards on the table
         * for selection.
         */
        SOMETHING
    }

    /**
     * Where the card is played, if not the ash heap.
     */
    private Target target;
}
