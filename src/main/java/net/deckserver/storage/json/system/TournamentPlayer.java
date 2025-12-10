package net.deckserver.storage.json.system;

import lombok.Data;

@Data
public class TournamentPlayer {
    private String name;
    private float vp = 0.0f;
    private boolean gw;
}
