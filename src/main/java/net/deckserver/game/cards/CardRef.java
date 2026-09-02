package net.deckserver.game.cards;

/**
 * Minimal card identity for rendering a chat / log card reference
 * ({@link #token()}). Everything the client needs to draw the link, nothing
 * more. {@code name} is the plain printed name (no {@code "(G# ADV)"}
 * qualifier) — the link text matches what a player typed in {@code [brackets]}.
 */
public record CardRef(String id, String name, boolean playtest, boolean advanced) {

    public static CardRef of(Card card) {
        boolean advanced = card instanceof CryptCard crypt && crypt.advanced();
        return new CardRef(card.id(), card.name(), card.playtest(), advanced);
    }

    /**
     * Plain-text chat token: {@code [card:<id>:<name>]}, with a trailing
     * {@code :adv} for an advanced crypt card. Resolved to a component by the
     * React client ({@code parseMessageTokens.ts}).
     */
    public String token() {
        return "[card:" + id + ":" + name + (advanced ? ":adv" : "") + "]";
    }
}
