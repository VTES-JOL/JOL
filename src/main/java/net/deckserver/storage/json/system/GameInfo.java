package net.deckserver.storage.json.system;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    public static final Version CURRENT_VERSION = Version.DATA_FIX;
    private String name;
    private String id;
    private String owner;
    private Visibility visibility;
    private GameStatus status;
    private GameFormat gameFormat = GameFormat.STANDARD;
    @JsonAlias("created")
    private OffsetDateTime updated = OffsetDateTime.now();
    private Version version = Version.INITIAL;
    private String tournamentName;

    public GameInfo(String name, String id, String owner, Visibility visibility, GameStatus status, GameFormat gameFormat) {
        this.name = name;
        this.id = id;
        this.owner = owner;
        this.visibility = visibility;
        this.status = status;
        this.gameFormat = gameFormat;
        this.version = CURRENT_VERSION;
    }

    @Getter
    public enum Version {
        INITIAL(0),
        GAME_STATE(1),
        DATA_FIX(2);

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
