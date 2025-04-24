package net.deckserver.storage.json.game;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class TurnData {
    @Setter(AccessLevel.NONE)
    private String turnId;
    private String player;

    private List<ChatData> chats = new ArrayList<>();

    public void setTurn(int turn, int phase) {
        turnId = String.format("%d.%d", turn, phase);
    }

    public void addChat(ChatData data) {
        this.chats.add(data);
    }
}