package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.deckserver.game.storage.cards.CardType;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIdentityReference
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class CardData {

    @JsonIdentityReference(alwaysAsId = true)
    @Setter(AccessLevel.NONE)
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
    private String clan;

    @Setter(AccessLevel.NONE)
    private final Set<String> disciplines = new HashSet<>();

    public void add(CardData card) {
        this.cards.add(card);
    }

    public void addDiscipline(String discipline) {
        this.disciplines.add(discipline);
    }

    @JsonIgnore
    public List<String> getDisciplinesSorted() {
        return this.disciplines.stream().sorted().collect(Collectors.toList());
    }
}
