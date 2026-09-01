package net.deckserver.game.cards;

import java.util.List;

/**
 * One way a library card can be played. A simple card has a single mode; cards
 * with discipline-gated options (e.g. Earth Control: {@code [pro]} vs
 * {@code [PRO]}) have one mode per option.
 *
 * <p>{@code disciplines} — the discipline code(s) this mode requires (empty for
 * a card with no discipline requirement). {@code text} — the mode's rules text
 * (already symbol-parsed HTML). {@code target} — where the card is played, or
 * null when it resolves to the ash heap like an ordinary action.
 */
public record PlayMode(List<String> disciplines, String text, PlayTarget target) {

    public PlayMode {
        disciplines = disciplines == null ? List.of() : List.copyOf(disciplines);
    }
}
