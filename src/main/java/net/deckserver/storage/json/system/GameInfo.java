package net.deckserver.storage.json.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class GameInfo {
    public static final Version CURRENT_VERSION = Version.GAME_STATE;
    private String name;
    private String id;
    private String owner;
    private Visibility visibility;
    private GameStatus status;
    private GameFormat gameFormat = GameFormat.STANDARD;
    private OffsetDateTime created = OffsetDateTime.now();
    private Version version = Version.INITIAL;

    public GameInfo(String name, String id, String owner, Visibility visibility, GameStatus status, GameFormat gameFormat) {
        this.name = name;
        this.id = id;
        this.owner = owner;
        this.visibility = visibility;
        this.status = status;
        this.gameFormat = gameFormat;
        this.version = CURRENT_VERSION;
    }

    @JsonIgnore
    public boolean isPlayTest() {
        return gameFormat == GameFormat.PLAYTEST;
    }

    @Getter
    public enum Version {
        INITIAL(0),
        GAME_STATE(1);

        private final int version;

        Version(int version) {
            this.version = version;
        }

        public boolean isCurrent() {
            return this.equals(CURRENT_VERSION);
        }

        public boolean isOlderThan(Version version) {
            return this.version < version.getVersion();
        }
    }
}
