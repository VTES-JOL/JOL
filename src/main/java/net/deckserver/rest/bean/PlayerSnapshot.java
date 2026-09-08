package net.deckserver.rest.bean;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Structural replacement for player.jsp. */
@Getter
@Builder
public class PlayerSnapshot {
    private final String name;
    private final int pool;
    private final double victoryPoints;
    private final boolean active;
    private final boolean edge;
    private final boolean pinged;
    /** Name of this seat's predator in the current (ousted-skipped) ring, or null if none / this seat is out. */
    private final String predator;
    /** Name of this seat's prey in the current (ousted-skipped) ring, or null if none / this seat is out. */
    private final String prey;
    /** ISO-8601 UTC instant of this seat's last board-mutating action, or null if they've never acted. */
    private final String lastActionAt;
    /** How this seat left the game — "OUST" / "WITHDRAW", or null if still in (or a legacy exit with no record). */
    private final String exitKind;
    /** Player credited the VP for this seat's exit (predator-at-oust, or self for a withdrawal); null if unknown. */
    private final String exitVpRecipient;
    private final List<RegionSnapshot> regions;
}
