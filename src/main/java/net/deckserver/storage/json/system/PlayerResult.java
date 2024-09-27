package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerResult {
    private String playerName;
    private String deckName;
    private String victoryPoints;
    private boolean gameWin;
}
