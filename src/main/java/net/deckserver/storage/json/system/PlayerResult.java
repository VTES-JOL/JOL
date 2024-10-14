package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "playerName")
public class PlayerResult {
    private String playerName;
    private String deckName;
    private String victoryPoints;
    private boolean gameWin;
}
