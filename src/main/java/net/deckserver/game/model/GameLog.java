package net.deckserver.game.model;

/**
 * Small formatting helpers shared by every game-log line {@link JolGame} emits.
 *
 * <p>The house style (see {@code docs/reviews/game-log-review.md}): one line is
 * one sentence, present tense, third person, with the actor implied by the
 * bold {@code source} column the UI renders — the body never begins with, or
 * repeats, the actor's own name. Regions use
 * {@link net.deckserver.game.enums.RegionType#logLabel()}. Ownership is
 * rendered with {@link #possessive(String, String)}.
 */
final class GameLog {

    private GameLog() {
    }

    /**
     * Normalise a log body: trim, collapse internal whitespace runs to a
     * single space, and guarantee exactly one terminal {@code . ! ?}.
     */
    static String sentence(String body) {
        if (body == null) {
            return "";
        }
        String s = body.trim().replaceAll("\\s{2,}", " ");
        if (s.isEmpty()) {
            return s;
        }
        char last = s.charAt(s.length() - 1);
        if (last != '.' && last != '!' && last != '?') {
            s = s + ".";
        }
        return s;
    }

    /**
     * {@code "their"} when the owner is the acting player, otherwise
     * {@code "<Owner>'s"}.
     */
    static String possessive(String ownerName, String actorName) {
        return ownerName.equals(actorName) ? "their" : ownerName + "'s";
    }

    /** {@code n + " " + singular} with a trailing {@code s} when {@code n != 1}. */
    static String plural(long n, String singular) {
        return n + " " + singular + (n == 1 ? "" : "s");
    }
}
