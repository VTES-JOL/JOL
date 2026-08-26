-- V8: Past game history (previously pastGames.json)

CREATE TABLE jol_game_history (
    id          BIGSERIAL    NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    game_name   VARCHAR(255) NOT NULL,
    started     VARCHAR(64),
    ended       VARCHAR(64),
    results     TEXT         NOT NULL DEFAULT '[]',
    CONSTRAINT pk_jol_game_history PRIMARY KEY (id),
    CONSTRAINT uq_jol_game_history_recorded_at UNIQUE (recorded_at)
);
