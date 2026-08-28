-- V16: Per-format deck validation results, recomputed and upserted on every
-- deck save (see DeckValidityService). Serves the deck editor's format chips
-- without re-validating on read. Rows are cleaned up by the cascade from
-- deck_info; existing decks get rows lazily on their next save.

CREATE TABLE deck_format_validity (
    deck_id     VARCHAR(36) NOT NULL,
    format      VARCHAR(32) NOT NULL,
    valid       BOOLEAN     NOT NULL,
    errors      TEXT        NOT NULL DEFAULT '[]',   -- JSON array of message strings
    computed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_deck_format_validity PRIMARY KEY (deck_id, format),
    CONSTRAINT fk_deck_format_validity_deck FOREIGN KEY (deck_id) REFERENCES deck_info (deck_id) ON DELETE CASCADE
);

CREATE INDEX idx_deck_format_validity_format ON deck_format_validity (format, valid);
