-- V6: Active game state and chat history

CREATE TABLE jol_game_state (
    game_id        VARCHAR(36)  NOT NULL,
    state          TEXT         NOT NULL,
    current_player VARCHAR(255),
    turn           VARCHAR(50),
    phase          VARCHAR(50),
    player_count   INTEGER      NOT NULL DEFAULT 0,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_jol_game_state PRIMARY KEY (game_id),
    CONSTRAINT fk_jol_game_state_game FOREIGN KEY (game_id) REFERENCES jol_game (game_id)
);

CREATE TABLE jol_game_chat (
    game_id    VARCHAR(36) NOT NULL,
    history    TEXT        NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_jol_game_chat PRIMARY KEY (game_id),
    CONSTRAINT fk_jol_game_chat_state FOREIGN KEY (game_id) REFERENCES jol_game_state (game_id)
);
