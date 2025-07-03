package net.deckserver.storage.json.system;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import net.deckserver.game.validators.*;

@Getter
public enum GameFormat {
    STANDARD("Standard", StandardDeckValidator.class, 5),
    DUEL("Duel", DuelDeckValidator.class, 2),
    V5("V5", V5DeckValidator.class, 5),
    JYHAD("Jyhad", JyhadDeckValidator.class, 5);

    @JsonValue
    private final String label;
    private final Class<? extends DeckValidator> deckValidator;
    private final int playerCount;

    GameFormat(String label, Class<? extends DeckValidator> deckValidator, int playerCount) {
        this.label = label;
        this.deckValidator = deckValidator;
        this.playerCount = playerCount;
    }

    @JsonCreator
    public static GameFormat from(String format) {
        for (GameFormat gameFormat : GameFormat.values()) {
            if (gameFormat.label.equalsIgnoreCase(format)) {
                return gameFormat;
            }
        }
        throw new IllegalArgumentException(format);
    }
}
