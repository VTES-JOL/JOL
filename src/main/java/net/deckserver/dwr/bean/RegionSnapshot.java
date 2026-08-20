package net.deckserver.dwr.bean;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Structural replacement for region.jsp. */
@Getter
@Builder
public class RegionSnapshot {
    private final String type; // RegionType name, e.g. "READY"
    private final String commandKey; // short key used in commands, e.g. "ready"/"inactive"/"ashheap"/"rfg"
    private final String label;
    private final boolean simple; // RegionType.SIMPLE_REGIONS — render CardSimple instead of the full Card
    private final boolean openHand;
    private final boolean hiddenHand;
    private final List<CardSnapshot> cards;
}
