-- V21: "call a judge" requests raised from within a game.
--
-- A seated player raises one request per game (partial unique index below
-- enforces at-most-one OPEN per game_id). Judges see the queue on a dedicated
-- page and jump into the game to rule; resolution happens in-game via the same
-- button. Rows deliberately OUTLIVE game cleanup (game_id ON DELETE SET NULL,
-- game_name / tournament_name denormalised) so the ruling history stays
-- available to other judges after the game itself is gone.

CREATE TABLE judge_request (
    id                BIGSERIAL    PRIMARY KEY,
    game_id           VARCHAR(36)  REFERENCES game (game_id) ON DELETE SET NULL,
    game_name         VARCHAR(255) NOT NULL,   -- snapshot: survives game deletion
    tournament_name   VARCHAR(255),            -- non-null => tournament game (context on the judge page)
    requested_by      VARCHAR(255) NOT NULL,
    category          VARCHAR(24)  NOT NULL,   -- INCORRECT_PLAY | CARD_RULING | OTHER
    status            VARCHAR(16)  NOT NULL,   -- OPEN | RETRACTED | RESOLVED
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    raw_details       TEXT         NOT NULL,   -- exactly what the player typed ([Card Name] markup intact)
    parsed_details    TEXT         NOT NULL,   -- ParserService.parseGameChat token form, for rendering
    resolved_by       VARCHAR(255),
    resolved_at       TIMESTAMP WITH TIME ZONE,
    resolution_raw    TEXT,
    resolution_parsed TEXT
);

-- At most one OPEN request per game at a time.
CREATE UNIQUE INDEX uq_judge_request_open_per_game
    ON judge_request (game_id)
    WHERE status = 'OPEN';

-- Judge-queue read (open, oldest first) and history read (resolved, newest first).
CREATE INDEX ix_judge_request_status_created ON judge_request (status, created_at);
CREATE INDEX ix_judge_request_category       ON judge_request (category, status);
