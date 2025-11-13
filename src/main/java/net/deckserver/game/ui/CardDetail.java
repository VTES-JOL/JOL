package net.deckserver.game.ui;

import lombok.Data;
import net.deckserver.game.enums.Clan;
import net.deckserver.game.enums.Path;
import net.deckserver.game.enums.RegionType;
import net.deckserver.game.enums.Sect;
import net.deckserver.storage.json.game.CardData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Data
public class CardDetail implements Serializable {
    public static final EnumSet<RegionType> FULL_ATTRIBUTE_REGIONS = EnumSet.of(RegionType.READY, RegionType.TORPOR, RegionType.UNCONTROLLED);
    public static final EnumSet<RegionType> LIMITED_ATTRIBUTE_REGIONS = EnumSet.of(RegionType.ASH_HEAP);
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
    private boolean infernal;
    private boolean playtest;
    private Path path;
    private Sect sect;
    private Clan clan;

    public CardDetail(CardData card) {
        this.id = card.getId();
        this.name = card.getName();
        this.cardId = card.getCardId();
        this.owner = card.getOwnerName();
        this.capacity = card.getCapacity();
        this.counters = card.getCounters();
        this.label = Optional.ofNullable(card.getNotes()).orElse("");
        this.votes = card.getVotes();
        this.locked = card.isLocked();
        this.contested = card.isContested();
        this.minion = card.isMinion();
        this.infernal = card.isInfernal();
        this.playtest = card.isPlaytest();
        this.path = Optional.ofNullable(card.getPath()).orElse(Path.NONE);
        this.sect = Optional.ofNullable(card.getSect()).orElse(Sect.NONE);
        this.clan = Optional.ofNullable(card.getClan()).orElse(Clan.NONE);
        this.disciplines = card.getDisciplinesSorted();
        this.cards = card.getCards().stream().map(CardData::getId).toList();
    }

    public String buildAttributes(RegionType region, String index, boolean visible) {
        String attributeString = String.format("data-coordinates='%s' ", index);
        attributeString += String.format("data-label='%s' ", label);
        attributeString += String.format("data-visible='%s' ", visible);
        if (visible) {
            attributeString += String.format("data-card-id='%s' ", cardId);
            attributeString += String.format("data-secured='%s' ", playtest);
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
