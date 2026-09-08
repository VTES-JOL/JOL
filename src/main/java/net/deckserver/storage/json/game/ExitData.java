package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A persisted record of one seat leaving the game — an oust or a withdrawal.
 *
 * <p>The ousted-seat strip on the game screen wants to show <em>who was credited
 * the VP</em> for that exit. That is not derivable from the live snapshot: by the
 * time anything renders, {@code updatePredatorMapping()} has already re-linked the
 * ring past the departed seat, so "current predator of that seat" is wrong by
 * construction, and there is nothing left to tell an oust from a withdrawal.
 * So the recipient is captured here at the moment of the exit:
 * <ul>
 *   <li><b>OUST</b> — the predator of the ousted seat <em>at the moment of the
 *       oust</em> (the position, regardless of who dealt the final pool loss).</li>
 *   <li><b>WITHDRAW</b> — the withdrawing player themselves (rulebook: a
 *       withdrawal credits the withdrawer 1 VP; their predator gets nothing).</li>
 * </ul>
 * A restored seat (Ankara Citadel etc.) drops its record — see
 * {@code JolGame.changePool}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
public class ExitData {

    public enum Kind {OUST, WITHDRAW}

    /** The seat that left the game. */
    private String seat;
    /** Player credited the VP for this exit — predator-at-oust, or the withdrawer themselves. May be null (no predator). */
    private String vpRecipient;
    /** Nominal VP awarded for the exit (1.0). */
    private double amount;
    /** Turn id ("{turn}.{index}") at the moment of the exit. */
    private String turn;
    private Kind kind;

    public ExitData(String seat, String vpRecipient, double amount, String turn, Kind kind) {
        this.seat = seat;
        this.vpRecipient = vpRecipient;
        this.amount = amount;
        this.turn = turn;
        this.kind = kind;
    }
}
