package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.game.model.JolGame;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;

import java.util.List;

/**
 * What a rollback to {@code toTurn} would change, for the admin confirm modal:
 * how many turns get discarded, whether the active player changes, and each
 * player's pool / victory points / ousted status before vs. after.
 *
 * <p>Loads the turn snapshot ({@link GameService#loadSnapshot}) the rollback
 * itself would restore and diffs it against current in-memory state — one
 * on-demand read, nothing persisted. Board/card state is deliberately not
 * diffed; pool/VP/ousted is the signal for "does this land where I meant".
 */
@Getter
public class RollbackPreviewBean {

    private final String fromTurn;
    private final String toTurn;
    private final int turnsDiscarded;
    private final boolean snapshotAvailable;
    private final String activePlayerBefore;
    private final String activePlayerAfter;
    private final List<PlayerDiff> players;

    public RollbackPreviewBean(String gameName, String gameId, String targetLabel, String turnCode) {
        JolGame current = GameService.getGameByName(gameName);
        this.fromTurn = current.getTurnLabel();
        this.toTurn = targetLabel;
        this.activePlayerBefore = current.getActivePlayer();

        List<String> turnLabels = ChatService.getTurns(gameId);
        int idx = turnLabels.indexOf(targetLabel);
        this.turnsDiscarded = idx >= 0 ? Math.max(0, turnLabels.size() - 1 - idx) : 0;

        JolGame snapshot;
        try {
            snapshot = GameService.loadSnapshot(gameId, turnCode);
        } catch (RuntimeException e) {
            snapshot = null;
        }
        this.snapshotAvailable = snapshot != null;
        if (snapshot == null) {
            this.activePlayerAfter = null;
            this.players = List.of();
            return;
        }
        this.activePlayerAfter = snapshot.getActivePlayer();
        JolGame after = snapshot;
        List<String> inSnapshot = after.getPlayers();
        this.players = current.getPlayers().stream()
                .map(p -> {
                    // A player seated after the target turn won't be in that snapshot;
                    // show their current values unchanged rather than NPE on getPool().
                    boolean present = inSnapshot.contains(p);
                    return new PlayerDiff(
                            p,
                            current.getPool(p), present ? after.getPool(p) : current.getPool(p),
                            current.getVictoryPoints(p), present ? after.getVictoryPoints(p) : current.getVictoryPoints(p),
                            current.isOusted(p), present ? after.isOusted(p) : current.isOusted(p));
                })
                .toList();
    }

    public record PlayerDiff(
            String name,
            int poolBefore, int poolAfter,
            double vpBefore, double vpAfter,
            boolean oustedBefore, boolean oustedAfter) {}
}
