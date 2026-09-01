package net.deckserver.game.cards;

/**
 * Where a library card (or one of its modes) is played, when that is not the
 * ash heap. Ported from the old {@code LibraryCardMode.Target} — drives the
 * play-card modal's target-picker flow on the client
 * ({@code needsTargetPicker} in {@code cardCommands.ts}).
 */
public enum PlayTarget {
    /** Played to the acting player's ready region. */
    READY_REGION,
    /** Played on the minion playing the card (equipment, retainers, powers). */
    SELF,
    /** Played on any card on the table — generic "put this card on" fallback. */
    SOMETHING,
    /** Removed from the game. */
    REMOVE_FROM_GAME,
    /** Played to the acting player's inactive / uncontrolled region. */
    INACTIVE_REGION,
    /** Played on a minion the acting player controls. */
    MINION_YOU_CONTROL
}
