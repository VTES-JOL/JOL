package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.*;

@Data
public class GameData {
    private String id;
    private String name;

    @Setter(AccessLevel.NONE)
    private List<String> playerOrder = new ArrayList<>();
    private Map<String, PlayerData> players = new HashMap<>();
    private Map<String, CardData> cards = new HashMap<>();

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData currentPlayer;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData nextPlayer;

    @JsonIdentityReference
    private PlayerData edge;

    private boolean orderOfPlayReversed = false;
    private String turn;
    private String phase;
    private String notes;

    public void addPlayer(PlayerData playerData) {
        this.players.put(playerData.getName(), playerData);
        this.playerOrder.add(playerData.getName());
    }

    public void addCards(Collection<CardData> cardData) {
        cardData.forEach(card -> this.cards.put(card.getId(), card));
    }

    @JsonIgnore
    public PlayerData getPlayer(String playerName) {
        return this.players.get(playerName);
    }

    @JsonIgnore
    public Collection<PlayerData> getPlayerData() {
        return this.players.values();
    }

    @JsonIgnore
    public String getCurrentPlayerName() {
        return this.currentPlayer.getName();
    }

    @JsonIgnore
    public String getEdgePlayer() {
        return this.edge != null ? this.edge.getName() : null;
    }

    @JsonIgnore
    public CardData getCard(String id) {
        return this.cards.get(id);
    }
}
