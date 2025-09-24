package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.ToString;
import net.deckserver.storage.json.cards.RegionType;

import java.util.*;

@Data
@JsonPropertyOrder({"id", "name", "playerOrder", "orderOfPlayReversed", "turn", "phase", "notes", "cards", "players", "currentPlayer", "edge"})
@ToString(of = {"id", "name"})
public class GameData {
    private String id;
    private String name;

    private List<String> playerOrder = new ArrayList<>();
    private Map<String, PlayerData> players = new HashMap<>();
    private Map<String, CardData> cards = new HashMap<>();

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData currentPlayer;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData edge;

    private boolean orderOfPlayReversed = false;
    private String turn = "1.1";
    private String phase;
    private String notes;

    private String timeoutRequestor;

    public GameData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public GameData(String id) {
        this.id = id;
    }

    public GameData() {
        this.id = UUID.randomUUID().toString();
    }

    public void addPlayer(PlayerData playerData) {
        this.players.put(playerData.getName(), playerData);
        this.playerOrder.add(playerData.getName());
    }

    public void initRegion(Collection<CardData> cardData) {
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
        return Optional.ofNullable(this.currentPlayer).map(PlayerData::getName).orElse(null);
    }

    @JsonIgnore
    public String getEdgePlayer() {
        return this.edge != null ? this.edge.getName() : null;
    }

    @JsonIgnore
    public CardData getCard(String id) {
        return this.cards.get(id);
    }

    @JsonIgnore
    public List<String> getPlayerNames() {
        return this.playerOrder;
    }

    @JsonIgnore
    public RegionData getPlayerRegion(String player, RegionType type) {
        return this.players.get(player).getRegion(type);
    }

    @JsonIgnore
    public List<CardData> getUniqueCards(CardData card) {
        List<CardData> cards = new ArrayList<>();
        if (!card.isUnique()) {
            return cards;
        }

        players.values().stream()
                .map(playerData -> playerData.getRegion(RegionType.READY))
                .flatMap(regionData -> regionData.getCards().stream())
                .filter(c -> c.getName().equals(card.getName()))
                .forEach(cards::add);

        return cards;
    }

    @JsonIgnore
    public RegionData getRegionFromCard(CardData card) {
        return card.getRegion();
    }

    public void orderPlayers(List<String> newOrder) {
        if (!new HashSet<>(this.playerOrder).containsAll(newOrder)) {
            return;
        }
        this.playerOrder = newOrder;
    }

    @JsonIgnore
    public List<PlayerData> getCurrentPlayers() {
        return this.playerOrder.stream()
                .map(this.players::get)
                .filter(playerData -> playerData.getPool() > 0)
                .toList();
    }

    public void initRegion(RegionData crypt, List<CardData> cryptCards) {
        cryptCards.forEach(card -> {
            crypt.addCard(card, false);
            cards.put(card.getId(), card);
        });
    }

    public void updatePredatorMapping() {
        List<PlayerData> currentPlayers = getCurrentPlayers();
        PlayerData current;
        PlayerData first = null;
        PlayerData predator = null;
        for (PlayerData player : currentPlayers) {
            current = player;
            if (first == null) {
                first = current;
            }
            if (predator != null) {
                current.setPredator(predator);
                predator.setPrey(current);
            }
            predator = current;
            if (player.equals(currentPlayers.getLast())) {
                current.setPrey(first);
                first.setPredator(current);
            }
        }
    }
}
