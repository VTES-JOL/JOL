package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.storage.json.system.RefreshTokenInfo;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id", insertable = false, updatable = false)
    private PlayerEntity player;

    @Column(name = "secret_hash", nullable = false, length = 64)
    private String secretHash;

    @Column(name = "device_label")
    private String deviceLabel;

    @Column(name = "created_at", nullable = false)
    private long createdAt;

    @Column(name = "last_used_at", nullable = false)
    private long lastUsedAt;

    @Column(name = "expires_at", nullable = false)
    private long expiresAt;

    @Column(name = "remember", nullable = false)
    private boolean remember;

    public RefreshTokenEntity() {}

    public static RefreshTokenEntity from(String playerId, RefreshTokenInfo info) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.id = info.getId();
        entity.playerId = playerId;
        entity.secretHash = info.getSecretHash();
        entity.deviceLabel = info.getDeviceLabel();
        entity.createdAt = info.getCreatedAt();
        entity.lastUsedAt = info.getLastUsedAt();
        entity.expiresAt = info.getExpiresAt();
        entity.remember = info.isRemember();
        return entity;
    }

    public void update(RefreshTokenInfo info) {
        this.secretHash = info.getSecretHash();
        this.lastUsedAt = info.getLastUsedAt();
        this.expiresAt = info.getExpiresAt();
    }

    public RefreshTokenInfo toRefreshTokenInfo() {
        RefreshTokenInfo info = new RefreshTokenInfo();
        info.setId(id);
        info.setPlayerName(player != null ? player.getPlayerName() : null);
        info.setSecretHash(secretHash);
        info.setDeviceLabel(deviceLabel);
        info.setCreatedAt(createdAt);
        info.setLastUsedAt(lastUsedAt);
        info.setExpiresAt(expiresAt);
        info.setRemember(remember);
        return info;
    }

    public String getId() { return id; }
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return player != null ? player.getPlayerName() : null; }
    public long getExpiresAt() { return expiresAt; }
}
