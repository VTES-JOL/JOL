package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jol_subscription")
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id", insertable = false, updatable = false)
    private PlayerEntity player;

    @Column(name = "endpoint", nullable = false, length = 1024)
    private String endpoint;

    @Column(name = "auth_key", nullable = false)
    private String authKey;

    @Column(name = "p256dh_key", nullable = false)
    private String p256dhKey;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    public Long getId() { return id; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return player != null ? player.getPlayerName() : null; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getAuthKey() { return authKey; }
    public void setAuthKey(String authKey) { this.authKey = authKey; }

    public String getP256dhKey() { return p256dhKey; }
    public void setP256dhKey(String p256dhKey) { this.p256dhKey = p256dhKey; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
}
