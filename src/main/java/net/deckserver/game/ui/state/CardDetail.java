package net.deckserver.game.ui.state;

import lombok.Data;
import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.storage.state.RegionType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Data
public class CardDetail implements Serializable {
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
    private boolean merged;
    private boolean infernal;
    private String path;
    private String sect;
    private String clan;

    public static final EnumSet<RegionType> FULL_ATTRIBUTE_REGIONS = EnumSet.of(RegionType.READY, RegionType.TORPOR, RegionType.UNCONTROLLED);
    public static final EnumSet<RegionType> LIMITED_ATTRIBUTE_REGIONS = EnumSet.of(RegionType.ASH_HEAP);

    public CardDetail(Card card) {
        this.id = card.getId();
        this.name = card.getName();
        this.cardId = card.getCardId();
        this.owner = card.getOwner();
    }

    public CardDetail(String id, String name, String cardId, String owner) {
        this.id = id;
        this.name = name;
        this.cardId = cardId;
        this.owner = owner;
    }

    public String buildAttributes(RegionType region, String index, boolean visible) {
        String attributeString = String.format("data-coordinates='%s' ", index);
        attributeString += String.format("data-label='%s' ", label);
        attributeString += String.format("data-visible='%s' ", visible);
        if (visible) {
            attributeString += String.format("data-card-id='%s' ", cardId);
            if (FULL_ATTRIBUTE_REGIONS.contains(region)) {
                attributeString += String.format("data-minion='%s' ", minion);
                attributeString += String.format("data-clan='%s' ", clan);
                attributeString += String.format("data-sect='%s' ", sect);
                attributeString += String.format("data-path='%s' ", path);
                attributeString += String.format("data-capacity='%s' ", capacity);
                attributeString += String.format("data-counters='%s' ", counters);
                attributeString += String.format("data-votes='%s' ", votes);
                attributeString += String.format("data-locked='%s' ", locked);
                attributeString += String.format("data-contested='%s' ", contested);
                attributeString += String.format("data-disciplines='%s' ", String.join(" ", disciplines));
            }
        }
        return attributeString;
    }
}
