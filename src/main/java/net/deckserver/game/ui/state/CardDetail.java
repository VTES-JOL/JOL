package net.deckserver.game.ui.state;

import lombok.Data;
import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.storage.state.RegionType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Data
public class CardDetail {
    private String id;
    private String name;
    private String cardId;
    private List<String> cards = new ArrayList<>();
    private List<String> disciplines = new ArrayList<>();
    private int capacity;
    private int counters;
    private String label;
    private String votes;
    private String owner;
    private boolean locked;
    private boolean contested;
    private boolean minion;

    public static final EnumSet<RegionType> FULL_ATTRIBUTE_REGIONS = EnumSet.of(RegionType.READY, RegionType.TORPOR, RegionType.UNCONTROLLED);
    public static final EnumSet<RegionType> LIMITED_ATTRIBUTE_REGIONS = EnumSet.of(RegionType.ASH_HEAP);

    public CardDetail(Card card) {
        this.id = card.getId();
        this.name = card.getName();
        this.cardId = card.getCardId();
        this.owner = card.getOwner();
    }

    public String buildAttributes(RegionType region, String index, boolean visible) {
        String attributeString = String.format("data-coordinates='%s' ", index);
        attributeString += String.format("data-label='%s' ", label);
        if (visible) {
            attributeString += String.format("data-card-id='%s' ", cardId);
            attributeString += String.format("data-minion='%s' ", minion);
            if (FULL_ATTRIBUTE_REGIONS.contains(region)) {
                attributeString += String.format("data-capacity='%s' ", capacity);
                attributeString += String.format("data-counters='%s' ", counters);
                attributeString += String.format("data-votes='%s' ", votes);
                attributeString += String.format("data-locked='%s' ", locked);
                attributeString += String.format("data-contested='%s' ", contested);
            }
        }
        return attributeString;
    }
}
