package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.deckserver.game.jaxb.actions.Turn;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TurnHistory {
    private String currentTurn;
    private String currentPlayer;
    private Map<String, TurnData> turnMap = new LinkedHashMap<>();

    public TurnHistory(List<TurnData> turns) {
        turns.forEach(turn -> {
            turnMap.put(turn.getLabel(), turn);
        });
        this.currentTurn = turns.getLast().getTurnId();
        this.currentPlayer = turns.getLast().getPlayer();
    }

    @JsonIgnore
    public TurnData getTurn(String label) {
        return turnMap.getOrDefault(label, new TurnData());
    }

    @JsonIgnore
    public List<String> getTurnLabels() {
        return turnMap.keySet().stream().toList().reversed();
    }

    public void addTurn(TurnData turn) {
        turnMap.put(turn.getLabel(), turn);
        currentPlayer = turn.getPlayer();
        currentTurn = turn.getTurnId();
    }

    public void addTurn(String player, String turnId) {
        TurnData turn = new TurnData(player, turnId);
        turnMap.put(turn.getLabel(), turn);
        currentTurn = turnId;
        currentPlayer = player;
    }

    @JsonIgnore
    public String getCurrentTurn() {
        return currentTurn;
    }

    @JsonIgnore
    public String getCurrentTurnLabel() {
        return String.format("%s %s", currentPlayer, currentTurn);
    }

    public void addChat(ChatData chatData) {
        String currentLabel = getCurrentTurnLabel();
        turnMap.getOrDefault(currentLabel, new TurnData(currentPlayer, currentTurn)).addChat(chatData);
    }

    public Collection<TurnData> getTurns() {
        return turnMap.values();
    }
}
