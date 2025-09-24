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
    private Target target;

    public enum Target {
        /**
         * Played to player's ready region.
         */
        READY_REGION,
        /**
         * Played on the minion playing the card.
         */
        SELF,
        /**
         * Played on any card on the table.
         * Used when a more specific parser for "Put this card on" is not
         * implemented.
         */
        SOMETHING,
        /**
         * Removed from game.
         */
        REMOVE_FROM_GAME,
        /**
         * Played to player's inactive/uncontrolled region.
         */
        INACTIVE_REGION,
        /**
         * Played on a minion the player controls.
         */
        MINION_YOU_CONTROL
    }
}
