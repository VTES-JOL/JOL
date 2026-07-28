package net.deckserver.jpa.entity;

import jakarta.persistence.*;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.storage.json.system.DeckInfo;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "deck_info")
public class DeckInfoEntity {

    @EmbeddedId
    private DeckInfoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id", insertable = false, updatable = false)
    private PlayerEntity player;

    @Column(name = "deck_id", nullable = false, unique = true)
    private String deckId;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private DeckFormat format;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "deck_format",
            joinColumns = @JoinColumn(name = "deck_id", referencedColumnName = "deck_id"))
    @Column(name = "format_tag")
    private Set<String> gameFormats = new HashSet<>();

    public DeckInfoEntity() {}

    public static DeckInfoEntity from(String playerId, String deckName, DeckInfo info) {
        DeckInfoEntity entity = new DeckInfoEntity();
        entity.id = new DeckInfoId(playerId, deckName);
        entity.deckId = info.getDeckId();
        entity.format = info.getFormat();
        entity.gameFormats = new HashSet<>(info.getGameFormats());
        return entity;
    }

    public DeckInfo toDeckInfo() {
        DeckInfo info = new DeckInfo();
        info.setDeckId(deckId);
        info.setDeckName(id.getDeckName());
        info.setFormat(format);
        info.setGameFormats(new HashSet<>(gameFormats));
        return info;
    }

    public void update(DeckInfo info) {
        this.format = info.getFormat();
        this.gameFormats = new HashSet<>(info.getGameFormats());
    }

    public DeckInfoId getId() { return id; }
    public String getDeckId() { return deckId; }
    public String getPlayerName() { return player != null ? player.getPlayerName() : null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckInfoEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
