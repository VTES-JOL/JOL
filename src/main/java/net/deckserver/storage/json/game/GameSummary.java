package net.deckserver.storage.json.game;

import lombok.Data;

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
    private List<String> players;
    private boolean flagged;
}
