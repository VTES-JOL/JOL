-- V7: Player activity, game activity, and global chat

CREATE TABLE jol_player_activity (
    player_id VARCHAR(36) NOT NULL,
    last_seen TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_jol_player_activity PRIMARY KEY (player_id),
    CONSTRAINT fk_jol_player_activity_player FOREIGN KEY (player_id) REFERENCES jol_player (player_id)
);

CREATE TABLE jol_game_activity (
    game_id           VARCHAR(36)  NOT NULL,
    last_updated      TIMESTAMP WITH TIME ZONE NOT NULL,
    player_timestamps TEXT NOT NULL DEFAULT '{}',
    player_pings      TEXT NOT NULL DEFAULT '{}',
    CONSTRAINT pk_jol_game_activity PRIMARY KEY (game_id),
    CONSTRAINT fk_jol_game_activity_game FOREIGN KEY (game_id) REFERENCES jol_game (game_id)
);

CREATE TABLE jol_global_chat (
    id        BIGSERIAL   NOT NULL,
    player_id VARCHAR(36) NOT NULL,
    message   TEXT        NOT NULL,
    posted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_jol_global_chat PRIMARY KEY (id)
);
