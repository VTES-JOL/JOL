package net.deckserver.game.enums;

import java.util.EnumSet;
import java.util.Objects;

public enum RegionType {
    READY("ready region", "Ready region", "ready region", true, true),
    UNCONTROLLED("inactive region", "Uncontrolled region", "uncontrolled region", true, false),
    ASH_HEAP("ashheap", "Ash heap", "ash heap", true, true),
    HAND("hand", "Hand", "hand", true, false),
    LIBRARY("library", "Library", "library", false, false),
    CRYPT("crypt", "Crypt", "crypt", false, false),
    TORPOR("torpor", "Torpor", "torpor", true, true),
    REMOVED_FROM_GAME("rfg", "Removed from Game", "the removed-from-game pile", true, true),
    RESEARCH("research", "Research Area", "research area", true, false);

    public final static EnumSet<RegionType> OWNER_VISIBLE_REGIONS = EnumSet.of(READY, UNCONTROLLED, ASH_HEAP, HAND, TORPOR, REMOVED_FROM_GAME, RESEARCH);
    public final static EnumSet<RegionType> OTHER_VISIBLE_REGIONS = EnumSet.of(READY, ASH_HEAP, TORPOR, REMOVED_FROM_GAME);
    public final static EnumSet<RegionType> OTHER_HIDDEN_REGIONS = EnumSet.complementOf(OTHER_VISIBLE_REGIONS);
    public final static EnumSet<RegionType> SIMPLE_REGIONS = EnumSet.of(ASH_HEAP, HAND, REMOVED_FROM_GAME, LIBRARY, RESEARCH);
    public final static EnumSet<RegionType> PLAYABLE_REGIONS = EnumSet.of(HAND, RESEARCH);
    public final static EnumSet<RegionType> IN_PLAY_REGIONS = EnumSet.of(READY, TORPOR);
    private final String xmlLabel;
    private final String description;
    private final String logLabel;
    private final boolean ownerVisibility;
    private final boolean otherVisibility;

    RegionType(String xmlLabel, String description, String logLabel, boolean ownerVisibility, boolean otherVisibility) {
        this.xmlLabel = xmlLabel;
        this.description = description;
        this.logLabel = logLabel;
        this.ownerVisibility = ownerVisibility;
        this.otherVisibility = otherVisibility;
    }

    public static RegionType of(String xmlLabel) {
        for (RegionType regionType : RegionType.values()) {
            if (regionType.xmlLabel.equals(xmlLabel)) {
                return regionType;
            }
        }
        return null;
    }

    public static RegionType startsWith(String text) {
        text = text.toLowerCase();
        for (RegionType regionType : RegionType.values()) {
            if (regionType.xmlLabel.toLowerCase().startsWith(text) || regionType.description.toLowerCase().startsWith(text)) {
                return regionType;
            }
        }
        return null;
    }

    public String xmlLabel() {
        return xmlLabel;
    }

    public String description() {
        return description;
    }

    /**
     * Lower-case prose name for use inside a game-log sentence
     * (e.g. "burns X from their <b>ash heap</b>."). Distinct from
     * {@link #xmlLabel()} (the wire / command token) and {@link #description()}
     * (title-case UI heading).
     */
    public String logLabel() {
        return logLabel;
    }

    public boolean ownerVisibility() {
        return ownerVisibility;
    }

    public boolean otherVisibility() {
        return otherVisibility;
    }

    public boolean isVisible(String owner, String viewer) {
        return Objects.equals(owner, viewer) ? ownerVisibility : otherVisibility;
    }


}
