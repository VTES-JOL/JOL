package net.deckserver.storage.json.game;

import lombok.Data;
import net.deckserver.game.enums.GameFormat;

import java.util.List;

@Data
public class GameSummary {
    private String id;
    private String name;
    private String turnLabel;
    private String phase;
    private PlayerSummary activePlayer;
    private PlayerSummary predator;
    private PlayerSummary prey;
    private GameFormat format;
    private List<String> players;
}
