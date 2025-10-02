package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TurnHistory {
    private List<TurnData> turns = new ArrayList<>();

    @JsonIgnore
    private Map<String, TurnData> turnMap = new HashMap<>();

    public TurnHistory(List<TurnData> turns) {
        this.turns = turns;
        this.turnMap = turns.stream().collect(Collectors.toMap(TurnData::getLabel, turn -> turn));
    }

    @JsonIgnore
    public TurnData getTurn(String label) {
        return turnMap.getOrDefault(label, new TurnData());
    }

    @JsonIgnore
    public List<String> getTurnLabels() {
        return turns.stream().map(TurnData::getLabel).collect(Collectors.toList()).reversed();
    }

    public void addTurn(TurnData turn) {
        turns.add(turn);
        turnMap.put(turn.getLabel(), turn);
    }

    public void addTurn(String player, String turnId) {
        TurnData turn = new TurnData(player, turnId);
        turns.add(turn);
        turnMap.put(turn.getLabel(), turn);
    }

    @JsonIgnore
    public String getCurrentTurn() {
        return turns.getLast().getTurnId();
    }

    @JsonIgnore
    public String getCurrentTurnLabel() {
        return turns.getLast().getLabel();
    }

    public void addChat(ChatData chatData) {
        turns.getLast().addChat(chatData);
    }
}
