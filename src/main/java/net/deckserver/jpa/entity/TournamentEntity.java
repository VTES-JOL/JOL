package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.TournamentFormat;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "jol_tournament")
public class TournamentEntity {

    @Id
    @Column(name = "tournament_id", nullable = false)
    private String tournamentId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "registration_start")
    private OffsetDateTime registrationStart;

    @Column(name = "registration_end")
    private OffsetDateTime registrationEnd;

    @Column(name = "play_starts")
    private OffsetDateTime playStarts;

    @Column(name = "play_ends")
    private OffsetDateTime playEnds;

    @Enumerated(EnumType.STRING)
    @Column(name = "format")
    private TournamentFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "deck_format")
    private GameFormat deckFormat;

    @Column(name = "number_of_rounds", nullable = false)
    private int numberOfRounds;

    @Column(name = "final_enabled", nullable = false)
    private boolean finalEnabled;

    @Column(name = "requires_id", nullable = false)
    private boolean requiresId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    @Column(name = "special_rules", columnDefinition = "TEXT")
    private String specialRules;

    @Column(name = "rounds", columnDefinition = "TEXT")
    private String rounds;

    @Column(name = "finals", columnDefinition = "TEXT")
    private String finals;

    public TournamentEntity() {}

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public OffsetDateTime getRegistrationStart() { return registrationStart; }
    public void setRegistrationStart(OffsetDateTime v) { this.registrationStart = v; }
    public OffsetDateTime getRegistrationEnd() { return registrationEnd; }
    public void setRegistrationEnd(OffsetDateTime v) { this.registrationEnd = v; }
    public OffsetDateTime getPlayStarts() { return playStarts; }
    public void setPlayStarts(OffsetDateTime v) { this.playStarts = v; }
    public OffsetDateTime getPlayEnds() { return playEnds; }
    public void setPlayEnds(OffsetDateTime v) { this.playEnds = v; }
    public TournamentFormat getFormat() { return format; }
    public void setFormat(TournamentFormat format) { this.format = format; }
    public GameFormat getDeckFormat() { return deckFormat; }
    public void setDeckFormat(GameFormat deckFormat) { this.deckFormat = deckFormat; }
    public int getNumberOfRounds() { return numberOfRounds; }
    public void setNumberOfRounds(int v) { this.numberOfRounds = v; }
    public boolean isFinalEnabled() { return finalEnabled; }
    public void setFinalEnabled(boolean v) { this.finalEnabled = v; }
    public boolean isRequiresId() { return requiresId; }
    public void setRequiresId(boolean v) { this.requiresId = v; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public String getSpecialRules() { return specialRules; }
    public void setSpecialRules(String specialRules) { this.specialRules = specialRules; }
    public String getRounds() { return rounds; }
    public void setRounds(String rounds) { this.rounds = rounds; }
    public String getFinals() { return finals; }
    public void setFinals(String finals) { this.finals = finals; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TournamentEntity that)) return false;
        return Objects.equals(tournamentId, that.tournamentId);
    }

    @Override
    public int hashCode() { return Objects.hashCode(tournamentId); }
}
