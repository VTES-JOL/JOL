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
    private final List<RegionSnapshot> regions;
}
