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
    private Set<String> openHand = new HashSet<>();

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData currentPlayer;

    @JsonIdentityReference(alwaysAsId = true)
    private PlayerData edge;

    private boolean orderOfPlayReversed = false;
    private String turn = "1.1";
    private Phase phase;
    private String notes;

    private String timeoutRequestor;

    /** Per-seat exit records (oust / withdrawal), in exit order. Empty on games that predate the field. */
    private List<ExitData> exits = new ArrayList<>();

    /** The one open action / response window (rules R1), or null when nothing is declared. */
    private PendingActionData pendingAction;

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
                .filter(playerData -> !playerData.isOusted() || playerData.getPool() > 0)
                .toList();
    }

    public void addPlayerToOpenHand(String playerName) {
        this.openHand.add(playerName);
    }

    public void removePlayerFromOpenHand(String playerName) {
        this.openHand.remove(playerName);
    }

    public Boolean isPlayerOpenHand(String playerName) {
        return this.openHand.contains(playerName);
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

    /**
     * Stable-sort every player's READY region so minions precede permanents
     * (masters / locations / powerbases). The list index <em>is</em> the command
     * coordinate ({@code lock ready 3}) and the client renders that number, so
     * an unsorted list can label a minion "3" while a permanent above it is "1"
     * (D19 / D35b). Relative order within each group — i.e. influence / play
     * order — is preserved. Call after a board-mutating submit and on load,
     * never mid-command (coordinates must stay stable while a submit resolves).
     */
    public void normalizeReadyOrder() {
        for (PlayerData player : players.values()) {
            player.getRegion(RegionType.READY).getCards()
                    .sort(Comparator.comparingInt(card -> card.isMinion() ? 0 : 1));
        }
    }

    @JsonIgnore
    public String getTurnLabel() {
        return String.format("%s %s", currentPlayer.getName(), turn);
    }

    /** Record (or replace) the exit record for a seat, so the ousted-seat strip can name the VP recipient. */
    public void recordExit(ExitData exit) {
        this.exits.removeIf(e -> e.getSeat().equals(exit.getSeat()));
        this.exits.add(exit);
    }

    /** Drop a seat's exit record — it came back into the game. */
    public void clearExit(String seat) {
        this.exits.removeIf(e -> e.getSeat().equals(seat));
    }

    @JsonIgnore
    public ExitData getExit(String seat) {
        return this.exits.stream().filter(e -> e.getSeat().equals(seat)).findFirst().orElse(null);
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
