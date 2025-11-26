package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.deckserver.game.enums.Phase;
import net.deckserver.game.enums.RegionType;

import java.util.*;

@Data
@JsonPropertyOrder({"id", "name", "playerOrder", "orderOfPlayReversed", "turn", "phase", "notes", "cards", "players", "currentPlayer", "edge"})
@ToString(of = {"id", "name"})
@NoArgsConstructor
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
    private Phase phase;
    private String notes;

    private String timeoutRequestor;

    public GameData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public GameData(String id) {
        this.id = id;
    }

    public void addPlayer(PlayerData playerData) {
        this.players.put(playerData.getName(), playerData);
        this.playerOrder.add(playerData.getName());
    }

    @JsonIgnore
    public PlayerData getPlayer(String playerName) {
        return this.players.get(playerName);
    }

    @JsonIgnore
    public String getCurrentPlayerName() {
        return Optional.ofNullable(this.currentPlayer).map(PlayerData::getName).orElse(null);
    }

    @JsonIgnore
    public String getEdgePlayer() {
        return this.edge != null ? this.edge.getName() : "no one";
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
                .filter(playerData -> !playerData.isOusted())
                .map(playerData -> playerData.getRegion(RegionType.READY))
                .flatMap(regionData -> regionData.getCards().stream())
                .filter(c -> c.getName().equals(card.getName()))
                .forEach(cards::add);

        return cards;
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
                .filter(playerData -> !playerData.isOusted())
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

    @JsonIgnore
    public String getTurnLabel() {
        return String.format("%s %s", currentPlayer.getName(), turn);
    }

    public void replacePlayer(String oldPlayer, String newPlayer) {
        PlayerData playerData = players.get(oldPlayer);
        playerData.setName(newPlayer);
        players.remove(oldPlayer);
        players.put(newPlayer, playerData);
        int index = playerOrder.indexOf(oldPlayer);
        if (index != -1) {
            playerOrder.set(index, newPlayer);
        }
    }

}
