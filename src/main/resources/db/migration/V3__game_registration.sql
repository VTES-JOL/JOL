CREATE TABLE jol_game (
    game_name       VARCHAR(255) NOT NULL,
    game_id         VARCHAR(36)  NOT NULL,
    owner_id        VARCHAR(36),
    visibility      VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    game_format     VARCHAR(30)  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    version         INTEGER      NOT NULL DEFAULT 0,
    tournament_name VARCHAR(255),
    CONSTRAINT pk_jol_game PRIMARY KEY (game_name),
    CONSTRAINT uq_jol_game_id UNIQUE (game_id),
    CONSTRAINT fk_jol_game_owner FOREIGN KEY (owner_id) REFERENCES jol_player (player_id)
);

CREATE TABLE jol_registration (
    game_id     VARCHAR(36)  NOT NULL,
    player_id   VARCHAR(36)  NOT NULL,
    deck_id     VARCHAR(36),
    deck_name   VARCHAR(255),
    valid       BOOLEAN      NOT NULL DEFAULT FALSE,
    summary     TEXT,
    registered_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_jol_registration PRIMARY KEY (game_id, player_id),
    CONSTRAINT fk_jol_registration_game   FOREIGN KEY (game_id)   REFERENCES jol_game   (game_id),
    CONSTRAINT fk_jol_registration_player FOREIGN KEY (player_id) REFERENCES jol_player (player_id)
);
