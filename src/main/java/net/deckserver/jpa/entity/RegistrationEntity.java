package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.storage.json.system.RegistrationStatus;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "jol_registration")
public class RegistrationEntity {

    @EmbeddedId
    private RegistrationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", referencedColumnName = "game_id", insertable = false, updatable = false)
    private GameInfoEntity game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id", insertable = false, updatable = false)
    private PlayerEntity player;

    @Column(name = "deck_id")
    private String deckId;

    @Column(name = "deck_name")
    private String deckName;

    @Column(name = "valid", nullable = false)
    private boolean valid;

    @Column(name = "summary")
    private String summary;

    @Column(name = "registered_at")
    private OffsetDateTime registeredAt;

    // deliberate copy of jol_deck_content: a snapshot of the deck as registered,
    // immune to later edits or deletion of the player's deck
    @Column(name = "deck_content", columnDefinition = "TEXT")
    private String deckContent;

    public RegistrationEntity() {}

    public static RegistrationEntity from(String gameId, String playerId, RegistrationStatus status) {
        RegistrationEntity entity = new RegistrationEntity();
        entity.id = new RegistrationId(gameId, playerId);
        entity.deckId = status.getDeckId();
        entity.deckName = status.getDeckName();
        entity.valid = status.isValid();
        entity.summary = status.getSummary();
        entity.registeredAt = status.getTimestamp();
        entity.deckContent = status.getDeckContent();
        return entity;
    }

    public RegistrationStatus toRegistrationStatus() {
        RegistrationStatus status = new RegistrationStatus();
        status.setDeckId(deckId);
        status.setDeckName(deckName);
        status.setValid(valid);
        status.setSummary(summary);
        status.setTimestamp(registeredAt);
        status.setDeckContent(deckContent);
        return status;
    }

    public void update(RegistrationStatus status) {
        this.deckId = status.getDeckId();
        this.deckName = status.getDeckName();
        this.valid = status.isValid();
        this.summary = status.getSummary();
        this.registeredAt = status.getTimestamp();
        this.deckContent = status.getDeckContent();
    }

    public RegistrationId getId() { return id; }
    public String getDeckContent() { return deckContent; }
    public void setDeckContent(String deckContent) { this.deckContent = deckContent; }

    public String getGameName() { return game != null ? game.getGameName() : null; }
    public String getPlayerName() { return player != null ? player.getPlayerName() : null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
