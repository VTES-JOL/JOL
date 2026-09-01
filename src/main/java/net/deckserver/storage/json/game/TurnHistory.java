package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * Appends a chat line to the current turn, creating the turn bucket if it
     * doesn't exist yet, and returns the {@link TurnData} it landed in (so
     * callers can read its position / chat index for row-per-message persistence).
     */
    public TurnData addChat(ChatData chatData) {
        String currentLabel = getCurrentTurnLabel();
        TurnData turn = turnMap.computeIfAbsent(currentLabel, k -> new TurnData(currentPlayer, currentTurn));
        turn.addChat(chatData);
        return turn;
    }

    /** 0-based ordinal of the current turn within this history, or 0 if it isn't present. */
    @JsonIgnore
    public int getCurrentTurnIndex() {
        int i = 0;
        String currentLabel = getCurrentTurnLabel();
        for (String label : turnMap.keySet()) {
            if (label.equals(currentLabel)) {
                return i;
            }
            i++;
        }
        return Math.max(0, turnMap.size() - 1);
    }

    public Collection<TurnData> getTurns() {
        return turnMap.values();
    }
}
