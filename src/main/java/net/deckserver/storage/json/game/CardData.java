package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.deckserver.storage.json.cards.CardType;
import net.deckserver.storage.json.cards.Clan;
import net.deckserver.storage.json.cards.Path;
import net.deckserver.storage.json.cards.Sect;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
@EqualsAndHashCode(exclude = {"parent", "region", "owner", "controller"})
@ToString(of = {"id", "cardId", "name"})
public class CardData {

    @JsonIdentityReference(alwaysAsId = true)
    private final LinkedList<CardData> cards = new LinkedList<>();

    @JsonIdentityReference(alwaysAsId = true)
    private CardData parent;

    @JsonIdentityReference(alwaysAsId = true)
    private RegionData region;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData owner;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData controller;

    private String id;
    private String cardId;
    private String name;
    private boolean locked;
    private boolean contested;
    private CardType type;
    private int capacity;
    private int counters;
    private String votes;
    private String notes;
    private String title;
    private boolean advanced;
    private Clan clan;
    private Sect sect;
    private Path path;
    private boolean minion;
    private boolean playtest;
    private boolean infernal;
    private boolean unique;

    private List<String> disciplines = new ArrayList<>();

    public CardData() {
        this.id = UUID.randomUUID().toString();
    }

    public CardData(String cardId, PlayerData owner) {
        this.cardId = cardId;
        this.owner = owner;
        this.id = UUID.randomUUID().toString();
    }

    public void add(CardData card, boolean top) {
        if (card.getParent() != null) {
            // if card has a parent, remove it from that parent first
            card.getParent().remove(card);
        } else if (card.getRegion() != null) {
            // Card has no parent, remove it from the region if it exists
            card.getRegion().removeCard(card);
        }
        if (top) {
            cards.addFirst(card);
        } else {
            cards.add(card);
        }
        card.setParent(this);
        card.setRegion(this.region);
    }

    public void remove(CardData card) {
        card.setParent(null);
        card.setRegion(null);
        this.cards.remove(card);
    }

    public void addDiscipline(String discipline) {
        this.disciplines.add(discipline);
    }

    @JsonIgnore
    public List<String> getDisciplinesSorted() {
        return this.disciplines.stream().sorted().collect(Collectors.toList());
    }

    @JsonIgnore
    public String getOwnerName() {
        return this.owner.getName();
    }
}
