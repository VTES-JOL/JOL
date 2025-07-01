package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class GameInfo {
    private String name;
    private String id;
    private String owner;
    private Visibility visibility;
    private GameStatus status;
    private GameFormat gameFormat = GameFormat.STANDARD;
    private OffsetDateTime created = OffsetDateTime.now();

    public GameInfo(String name, String id, String owner, Visibility visibility, GameStatus status, GameFormat gameFormat) {
        this.name = name;
        this.id = id;
        this.owner = owner;
        this.visibility = visibility;
        this.status = status;
        this.gameFormat = gameFormat;
    }
}
