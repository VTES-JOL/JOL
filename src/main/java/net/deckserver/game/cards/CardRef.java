package net.deckserver.game.cards;

/**
 * Minimal card identity for rendering a chat / log card-link
 * ({@code ParserService#generateCardLink}). Everything the anchor markup needs,
 * nothing more. {@code name} is the plain printed name (no {@code "(G# ADV)"}
 * qualifier) — the link text matches what a player typed in {@code [brackets]}.
 */
public record CardRef(String id, String name, boolean playtest, boolean advanced) {

    public static CardRef of(Card card) {
        boolean advanced = card instanceof CryptCard crypt && crypt.advanced();
        return new CardRef(card.id(), card.name(), card.playtest(), advanced);
    }
}
