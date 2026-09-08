package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * The one open "action declared — response window" on a table (rules R1 / D9).
 *
 * <p>Deliberately shallow: it models <em>identity</em> (who declared what, at
 * whom, for how much) plus a <em>pass ledger</em> (who still owes a response,
 * who has explicitly passed <em>this</em> window). The rules impulse
 * fine-structure — reset-on-play, sub-windows, "only when needed" — stays a
 * chat convention. A table is single-threaded, so at most one of these is ever
 * present on {@code GameData}; a {@code null} field means no window is open
 * (there is no stored RESOLVED/WITHDRAWN record — resolve clears it).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
public class PendingActionData {

    public enum Type {BLEED, HUNT, RUSH, POLITICAL, RESCUE, DIABLERISE, LEAVE_TORPOR, GO_ANARCH, ACTION_CARD, OTHER}

    /** Correlates the declare / pass / resolve log lines; makes pass & resolve idempotent. */
    private String id;
    /** Player who declared. */
    private String actor;
    /** Acting minion's CardData.id — null for a rare card-in-play political with no minion. */
    private String actingCardId;
    private Type type;
    /** Declared directed target (a bleed defaults to the actor's prey); null for undirected. */
    private String targetPlayer;
    /** When a card is the target (rush / rescue / diablerise). */
    private String targetCardId;
    /** Bleed amount / vote count — informational only, 0 when unspecified. */
    private int amount;
    /** Free text the actor typed ("Govern the Unaligned at sup"). */
    private String note;
    /** ISO-8601 instant the window opened. */
    private String declaredAt;
    /** Players whose response is still outstanding. Empty ⇒ the actor can resolve. */
    private List<String> awaiting = new ArrayList<>();
    /** Players who explicitly passed THIS window. */
    private List<String> passed = new ArrayList<>();

    public PendingActionData(String id, String actor, String actingCardId, Type type,
                             String targetPlayer, String targetCardId, int amount, String note, String declaredAt) {
        this.id = id;
        this.actor = actor;
        this.actingCardId = actingCardId;
        this.type = type;
        this.targetPlayer = targetPlayer;
        this.targetCardId = targetCardId;
        this.amount = amount;
        this.note = note;
        this.declaredAt = declaredAt;
    }

    /** "bleed vs Stolas for 2" / "hunt" — used in log lines. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String describe() {
        StringBuilder sb = new StringBuilder(label());
        if (targetPlayer != null) sb.append(" vs ").append(targetPlayer);
        if (amount > 1) sb.append(" for ").append(amount);
        return sb.toString();
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String label() {
        return switch (type) {
            case BLEED -> "bleed";
            case HUNT -> "hunt";
            case RUSH -> "rush";
            case POLITICAL -> "political action";
            case RESCUE -> "rescue";
            case DIABLERISE -> "diablerie";
            case LEAVE_TORPOR -> "leave-torpor action";
            case GO_ANARCH -> "go-anarch action";
            case ACTION_CARD -> "action";
            case OTHER -> "action";
        };
    }
}
