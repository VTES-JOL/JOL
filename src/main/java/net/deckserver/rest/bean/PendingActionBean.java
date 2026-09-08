package net.deckserver.rest.bean;

import lombok.Builder;
import lombok.Getter;
import net.deckserver.storage.json.game.PendingActionData;

import java.util.List;

/**
 * The one open action / response window on a table, for the game screen's HUD
 * banner (rules R1 / D9). A straight projection of {@link PendingActionData} —
 * not viewer-scoped: the whole table sees the same banner, the client decides
 * per-viewer whether to show Respond/Pass (viewer in {@code awaiting}), Resolve
 * (viewer is {@code actor}), or an informational line.
 */
@Getter
@Builder
public class PendingActionBean {
    private final String id;
    private final String actor;
    private final String actingCardId;
    /** BLEED / HUNT / RUSH / POLITICAL / RESCUE / DIABLERISE / LEAVE_TORPOR / GO_ANARCH / ACTION_CARD / OTHER. */
    private final String type;
    /** Short human label for the type ("bleed", "hunt", …). */
    private final String label;
    private final String targetPlayer;
    private final String targetCardId;
    /** Bleed amount / vote count — 0 when unspecified. */
    private final int amount;
    private final String note;
    private final String declaredAt;
    private final List<String> awaiting;
    private final List<String> passed;

    public static PendingActionBean of(PendingActionData pa) {
        return PendingActionBean.builder()
                .id(pa.getId())
                .actor(pa.getActor())
                .actingCardId(pa.getActingCardId())
                .type(pa.getType().name())
                .label(pa.label())
                .targetPlayer(pa.getTargetPlayer())
                .targetCardId(pa.getTargetCardId())
                .amount(pa.getAmount())
                .note(pa.getNote())
                .declaredAt(pa.getDeclaredAt())
                .awaiting(pa.getAwaiting())
                .passed(pa.getPassed())
                .build();
    }
}
