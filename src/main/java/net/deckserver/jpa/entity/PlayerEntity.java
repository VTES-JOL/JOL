package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.game.enums.PlayerRole;
import net.deckserver.storage.json.system.PlayerInfo;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "jol_player")
public class PlayerEntity {

    @Id
    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    @Column(name = "player_name", nullable = false, unique = true)
    private String playerName;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "discord_id")
    private String discordId;

    @Column(name = "vekn_id")
    private String veknId;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "show_images", nullable = false)
    private boolean showImages = true;

    @Column(name = "edge_color", nullable = false)
    private String edgeColor = "#FFFFFF";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "jol_player_role",
            joinColumns = @JoinColumn(name = "player_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<PlayerRole> roles = new HashSet<>();

    public PlayerEntity() {}

    public static PlayerEntity from(PlayerInfo info) {
        PlayerEntity entity = new PlayerEntity();
        entity.playerId = info.getId();
        entity.playerName = info.getName();
        entity.email = info.getEmail();
        entity.passwordHash = info.getHash();
        entity.discordId = info.getDiscordId();
        entity.veknId = info.getVeknId();
        entity.countryCode = info.getCountryCode();
        entity.showImages = info.isShowImages();
        entity.edgeColor = info.getEdgeColor();
        entity.roles = new HashSet<>(info.getRoles());
        return entity;
    }

    public PlayerInfo toPlayerInfo() {
        PlayerInfo info = new PlayerInfo(playerName, playerId, email, passwordHash);
        info.setDiscordId(discordId);
        info.setVeknId(veknId);
        info.setCountryCode(countryCode);
        info.setShowImages(showImages);
        info.setEdgeColor(edgeColor);
        info.setRoles(new HashSet<>(roles));
        return info;
    }

    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerEntity that)) return false;
        return Objects.equals(playerId, that.playerId);
    }

    @Override
    public int hashCode() { return Objects.hashCode(playerId); }
}
