package net.deckserver.game.model;

import java.util.regex.Pattern;

/**
 * Helpers for classifying games by their (free-text) name.
 *
 * <p>NOTE: {@link #isTournament(String)} is mirrored, deliberately, in
 * {@code migrate-to-db.sh} (section 18, the metric_event backfill) as a Python
 * regex. Keep the two in sync — the script is run once per DB reseed, not on
 * every deploy, so a drift there is only caught at the next reseed.
 */
public final class GameNames {

    private static final Pattern TOURNAMENT_TABLE = Pattern.compile("Round\\s+\\d+\\s*-\\s*Table\\s+\\d+");

    private GameNames() {
    }

    /** A tournament round/final table, as opposed to a casual game. */
    public static boolean isTournament(String gameName) {
        return gameName != null
                && (gameName.contains("Final Table") || TOURNAMENT_TABLE.matcher(gameName).find());
    }
}
