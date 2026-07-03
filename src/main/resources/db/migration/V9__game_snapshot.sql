-- V9: Per-turn game state snapshots for rollback (previously games/<gameId>/game-<turn>.json)

CREATE TABLE jol_game_snapshot (
    game_id    VARCHAR(36) NOT NULL,
    turn       VARCHAR(32) NOT NULL,
    state      TEXT        NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_jol_game_snapshot PRIMARY KEY (game_id, turn),
    CONSTRAINT fk_jol_game_snapshot_game FOREIGN KEY (game_id) REFERENCES jol_game (game_id) ON DELETE CASCADE
);
