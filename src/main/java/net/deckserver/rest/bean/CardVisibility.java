package net.deckserver.rest.bean;

import net.deckserver.storage.json.game.CardData;

/**
 * The single "can this viewer see this card's identity" decision for the game
 * board snapshot. Every node in {@link GameSnapshotFactory}'s card tree runs
 * through {@link #visibleTo} — nothing else in the factory should test
 * {@code card.isFaceDown()} or re-implement the region/owner cascade.
 *
 * <p>The rule, in order:
 * <ol>
 *   <li>A face-down card is visible only to its controller — the player whose
 *       board (region) it currently sits on. This beats every clause below,
 *       including the "foreign card" override, so a face-down card played into
 *       an opponent's region is hidden from its own owner too.</li>
 *   <li>Otherwise, visible if the region itself is visible to the viewer
 *       (region.jsp's {@code ${visible}} cascade, already resolved by the
 *       caller into {@code regionVisible} — {@link net.deckserver.game.enums.RegionType#isVisible}
 *       plus the open-hand override).</li>
 *   <li>Otherwise, visible if the card is "foreign" — its owner differs from the
 *       region's owning player (a stolen vampire, a loaned card): those are
 *       always shown in whichever region they land in.</li>
 * </ol>
 *
 * <p>A future judge "see everything" toggle is a single extra clause here
 * ({@code || allSeeing}) and threading one boolean from
 * {@link GameSnapshotFactory#build} — deliberately localised to this method.
 */
final class CardVisibility {

    private CardVisibility() {}

    static boolean visibleTo(CardData card, String regionOwner, String viewer, boolean regionVisible) {
        if (card.isFaceDown()) {
            return regionOwner.equals(viewer);
        }
        if (regionVisible) {
            return true;
        }
        return !card.getOwnerName().equals(regionOwner);
    }
}
