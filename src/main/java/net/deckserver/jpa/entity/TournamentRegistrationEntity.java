package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.storage.json.system.TournamentRegistration;

import java.util.Objects;

@Entity
@Table(name = "jol_tournament_registration")
public class TournamentRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private String tournamentId;

    @Column(name = "player_id", nullable = false, length = 36)
    private String playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id", insertable = false, updatable = false)
    private PlayerEntity player;

    @Column(name = "vekn")
    private String vekn;

    @Column(name = "deck_id")
    private String deckId;

    // deliberate copy of jol_deck_content: a snapshot of the deck as registered,
    // immune to later edits or deletion of the player's deck
    @Column(name = "deck_content", columnDefinition = "TEXT")
    private String deckContent;

    public TournamentRegistrationEntity() {}

    public static TournamentRegistrationEntity from(String tournamentId, String playerId, TournamentRegistration reg) {
        TournamentRegistrationEntity entity = new TournamentRegistrationEntity();
        entity.tournamentId = tournamentId;
        entity.playerId = playerId;
        entity.vekn = reg.getVekn();
        entity.deckId = reg.getDeck();
        entity.deckContent = reg.getDeckContent();
        return entity;
    }

    public TournamentRegistration toRegistration() {
        String name = player != null ? player.getPlayerName() : playerId;
        TournamentRegistration reg = new TournamentRegistration(name, vekn);
        reg.setDeck(deckId);
        reg.setDeckContent(deckContent);
        return reg;
    }

    public String getDeckContent() { return deckContent; }
    public void setDeckContent(String deckContent) { this.deckContent = deckContent; }

    public Long getId() { return id; }
    public String getTournamentId() { return tournamentId; }
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return player != null ? player.getPlayerName() : null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TournamentRegistrationEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
