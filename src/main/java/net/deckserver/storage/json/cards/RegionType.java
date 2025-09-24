package net.deckserver.storage.json.cards;

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

    public final static EnumSet<RegionType> OWNER_VISIBLE_REGIONS = EnumSet.of(READY, UNCONTROLLED, ASH_HEAP, HAND, TORPOR, REMOVED_FROM_GAME, RESEARCH);
    public final static EnumSet<RegionType> OTHER_VISIBLE_REGIONS = EnumSet.of(READY, ASH_HEAP, TORPOR, REMOVED_FROM_GAME);
    public final static EnumSet<RegionType> OTHER_HIDDEN_REGIONS = EnumSet.complementOf(OTHER_VISIBLE_REGIONS);
    public final static EnumSet<RegionType> SIMPLE_REGIONS = EnumSet.of(ASH_HEAP, HAND, REMOVED_FROM_GAME, LIBRARY, RESEARCH);
    public final static EnumSet<RegionType> PLAYABLE_REGIONS = EnumSet.of(HAND, RESEARCH);
    public final static EnumSet<RegionType> IN_PLAY_REGIONS = EnumSet.of(READY, TORPOR);
    private final String xmlLabel;
    private final String description;
    private final boolean ownerVisibility;
    private final boolean otherVisibility;

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

    public boolean ownerVisibility() {
        return ownerVisibility;
    }

    public boolean otherVisibility() {
        return otherVisibility;
    }

}
