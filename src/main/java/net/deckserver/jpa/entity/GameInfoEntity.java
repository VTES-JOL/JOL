package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.storage.json.system.GameInfo;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "game")
public class GameInfoEntity {

    @Id
    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "game_id", nullable = false, unique = true)
    private String gameId;

    @Column(name = "owner_id", length = 36)
    private String ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "player_id", insertable = false, updatable = false)
    private PlayerEntity owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_format", nullable = false)
    private GameFormat gameFormat;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // application-level data-format version (GameInfo.Version ordinal), NOT a JPA
    // @Version — optimistic locking lives on GameStateEntity
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "tournament_name")
    private String tournamentName;

    public GameInfoEntity() {}

    public static GameInfoEntity from(GameInfo info, String ownerId) {
        GameInfoEntity entity = new GameInfoEntity();
        entity.gameName = info.getName();
        entity.gameId = info.getId();
        entity.ownerId = ownerId;
        entity.visibility = info.getVisibility();
        entity.status = info.getStatus();
        entity.gameFormat = info.getGameFormat();
        entity.createdAt = info.getCreated();
        entity.version = info.getVersion().getVersion();
        entity.tournamentName = info.getTournamentName();
        return entity;
    }

    public GameInfo toGameInfo() {
        GameInfo info = new GameInfo();
        info.setName(gameName);
        info.setId(gameId);
        info.setOwner(owner != null ? owner.getPlayerName() : null);
        info.setVisibility(visibility);
        info.setStatus(status);
        info.setGameFormat(gameFormat);
        info.setCreated(createdAt);
        info.setVersion(versionFromInt(version));
        info.setTournamentName(tournamentName);
        return info;
    }

    private static GameInfo.Version versionFromInt(int v) {
        return Arrays.stream(GameInfo.Version.values())
                .filter(ver -> ver.getVersion() == v)
                .findFirst()
                .orElse(GameInfo.Version.INITIAL);
    }

    public void update(GameInfo info, String ownerId) {
        this.ownerId = ownerId;
        this.visibility = info.getVisibility();
        this.status = info.getStatus();
        this.gameFormat = info.getGameFormat();
        this.version = info.getVersion().getVersion();
        this.tournamentName = info.getTournamentName();
    }

    public String getGameName() { return gameName; }

    public String getGameId() { return gameId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameInfoEntity that)) return false;
        return Objects.equals(gameName, that.gameName);
    }

    @Override
    public int hashCode() { return Objects.hashCode(gameName); }
}
