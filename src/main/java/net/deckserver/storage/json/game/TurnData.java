package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
public class TurnData {
    private String turnId;
    private String player;
    private List<ChatData> chats = new ArrayList<>();

    public TurnData(String player, String turnId) {
        this.player = player;
        this.turnId = turnId;
    }

    @JsonIgnore
    public void setTurnId(int turn, int index) {
        turnId = String.format("%d.%d", turn, index);
    }

    public void addChat(ChatData data) {
        this.chats.add(data);
    }

    @JsonIgnore
    public String getLabel() {
        return String.format("%s %s", player, turnId);
    }
}