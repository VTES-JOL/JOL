package net.deckserver.game;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

/**
 * Single source of truth for how a game win is derived from final victory-point totals.
 *
 * <p>The game win goes to the player with the <em>strictly highest</em> victory-point total,
 * provided that total is at least {@link #GAME_WIN_THRESHOLD}. If the highest qualifying total
 * is shared by two or more players there is no game win. The result does not depend on the
 * iteration order of the players.
 */
public final class GameOutcome {

    /** Minimum victory points a player must reach to be eligible for the game win. */
    public static final double GAME_WIN_THRESHOLD = 2.0;

    private static final double EPSILON = 1e-6;

    private GameOutcome() {
    }

    /**
     * @param players       the players to consider
     * @param victoryPoints extracts a player's final victory-point total
     * @return the winning player, or empty if nobody reaches the threshold or the lead is tied
     */
    public static <T> Optional<T> gameWinner(Collection<T> players, ToDoubleFunction<? super T> victoryPoints) {
        T leader = null;
        double topVP = 0.0;
        boolean tiedForLead = false;
        for (T player : players) {
            double vp = victoryPoints.applyAsDouble(player);
            if (vp < GAME_WIN_THRESHOLD) {
                continue;
            }
            if (leader == null || vp > topVP) {
                leader = player;
                topVP = vp;
                tiedForLead = false;
            } else if (vp == topVP) {
                tiedForLead = true;
            }
        }
        return (leader != null && !tiedForLead) ? Optional.of(leader) : Optional.empty();
    }

    /**
     * Convenience overload for callers that already have a name &rarr; victory-points mapping.
     *
     * @return the winning player's name, or empty if nobody reaches the threshold or the lead is tied
     */
    public static Optional<String> gameWinner(Map<String, ? extends Number> victoryPointsByPlayer) {
        return gameWinner(victoryPointsByPlayer.entrySet(), e -> e.getValue().doubleValue())
                .map(Map.Entry::getKey);
    }

    /**
     * The total victory points awarded in a game can never exceed the number of players: a game of
     * N players is decided by N&nbsp;&minus;&nbsp;1 ousts plus a single game-win bonus, for N VP at
     * most. A recorded total above the player count means the victory points were mis-entered and
     * the result should not be trusted (e.g. persisted to history).
     *
     * @param playerCount          number of players that were in the game
     * @param totalVictoryPoints   sum of every player's final victory-point total
     */
    public static boolean isPlausibleVictoryPointTotal(int playerCount, double totalVictoryPoints) {
        return totalVictoryPoints >= 0 && totalVictoryPoints <= playerCount + EPSILON;
    }
}
