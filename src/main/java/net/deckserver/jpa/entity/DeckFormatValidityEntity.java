package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Per-format validation outcome for a deck, recomputed and upserted whenever
 * the deck's content changes (see {@code DeckValidityService}). One row per
 * (deck_id, format). Rows are cleaned up by the {@code ON DELETE CASCADE} from
 * {@code deck_info}.
 */
@Entity
@Table(name = "deck_format_validity")
public class DeckFormatValidityEntity {

    @EmbeddedId
    private DeckFormatValidityId id;

    @Column(name = "valid", nullable = false)
    private boolean valid;

    /** JSON array of error message strings; {@code "[]"} when valid. */
    @Column(name = "errors", nullable = false, columnDefinition = "TEXT")
    private String errors = "[]";

    @Column(name = "computed_at", nullable = false)
    private OffsetDateTime computedAt;

    public DeckFormatValidityEntity() {}

    public DeckFormatValidityId getId() { return id; }
    public void setId(DeckFormatValidityId id) { this.id = id; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getErrors() { return errors; }
    public void setErrors(String errors) { this.errors = errors; }

    public OffsetDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(OffsetDateTime computedAt) { this.computedAt = computedAt; }
}
