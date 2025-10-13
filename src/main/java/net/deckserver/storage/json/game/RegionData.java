package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.deckserver.game.enums.RegionType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIdentityReference
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@EqualsAndHashCode(exclude = {"player"})
@ToString(of = {"id"})
public class RegionData {
    @JsonIdentityReference(alwaysAsId = true)
    private LinkedList<CardData> cards = new LinkedList<>();

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData player;
    private RegionType type;
    private String id;

    public RegionData(RegionType type, PlayerData playerData) {
        this.type = type;
        this.player = playerData;
        this.id = playerData.getName() + "-" + type.description();
    }

    public RegionData() {
    }

    @JsonIgnore
    public String getOwner() {
        return player.getName();
    }

    public List<CardData> getCards() {
        return cards;
    }

    @JsonIgnore
    public CardData getFirstCard() {
        return cards.getFirst();
    }

    public void shuffle(int limit) {
        if (limit < 0) return;
        if (limit == 0) limit = cards.size();
        List<CardData> subList = cards.subList(0, Math.min(limit, cards.size()));
        Collections.shuffle(subList);
    }

    public void addCard(CardData card, boolean top) {
        // Remove from parent first, if it exists
        if (card.getParent() != null) {
            card.getParent().remove(card);
        }
        // Remove from region if it exists
        if (card.getRegion() != null) {
            card.getRegion().removeCard(card);
        }
        if (top) {
            cards.addFirst(card);
        } else {
            cards.add(card);
        }
        card.setParent(null);
        card.setRegion(this);
    }

    public void removeCard(CardData card) {
        cards.remove(card);
        card.setRegion(null);
    }

    @JsonIgnore
    public CardData getCard(int i) {
        return cards.get(i);
    }

    public int size() {
        int size = cards.size();
        for (CardData card: cards) {
            size += card.size();
        }
        return size;
    }
}
