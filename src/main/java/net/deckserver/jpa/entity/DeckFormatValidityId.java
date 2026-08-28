package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import net.deckserver.game.enums.GameFormat;

import java.io.Serializable;
import java.util.Objects;

/** Composite key for {@link DeckFormatValidityEntity}: one row per (deck, format). */
@Embeddable
public class DeckFormatValidityId implements Serializable {

    @Column(name = "deck_id", nullable = false, length = 36)
    private String deckId;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 32)
    private GameFormat format;

    public DeckFormatValidityId() {}

    public DeckFormatValidityId(String deckId, GameFormat format) {
        this.deckId = deckId;
        this.format = format;
    }

    public String getDeckId() { return deckId; }
    public GameFormat getFormat() { return format; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckFormatValidityId that)) return false;
        return Objects.equals(deckId, that.deckId) && format == that.format;
    }

    @Override
    public int hashCode() { return Objects.hash(deckId, format); }
}
