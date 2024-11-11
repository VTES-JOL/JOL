package net.deckserver.game.storage.state;

import java.util.EnumSet;

public enum RegionType {
    READY("ready region", "Ready region", true, true),
    UNCONTROLLED("inactive region", "Uncontrolled region", true, false),
    ASH_HEAP("ashheap", "Ash heap", true, true),
    HAND("hand", "Hand", true, false),
    LIBRARY("library", "Library", false, false),
    CRYPT("crypt", "Crypt", false, false),
    TORPOR("torpor", "Torpor", true, true),
    REMOVED_FROM_GAME("rfg", "Removed from Game", true, true),
    RESEARCH("research", "Research Area", true, false);

    private final String xmlLabel;
    private final String description;
    private final boolean ownerVisibility;
    private final boolean otherVisibility;

    public final static EnumSet<RegionType> OWNER_VISIBLE_REGIONS = EnumSet.of(READY, UNCONTROLLED, ASH_HEAP, HAND, TORPOR, REMOVED_FROM_GAME, RESEARCH);
    public final static EnumSet<RegionType> OTHER_VISIBLE_REGIONS = EnumSet.of(READY, ASH_HEAP, TORPOR, REMOVED_FROM_GAME);

    RegionType(String xmlLabel, String description, boolean ownerVisibility, boolean otherVisibility) {
        this.xmlLabel = xmlLabel;
        this.description = description;
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

    public String xmlLabel() {
        return xmlLabel;
    }

    public String description() {
        return description;
    }

    public boolean ownerVisibility() {
        return ownerVisibility;
    }

    public boolean otherVisibility() {
        return otherVisibility;
    }

}
