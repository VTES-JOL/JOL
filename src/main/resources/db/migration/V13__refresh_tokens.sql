-- V13: Refresh tokens ("remember me" / session persistence), migrated off refreshTokens.json

CREATE TABLE refresh_token (
    id            VARCHAR(36)  NOT NULL,
    player_id     VARCHAR(36)  NOT NULL,
    secret_hash   VARCHAR(64)  NOT NULL,
    device_label  VARCHAR(255),
    created_at    BIGINT       NOT NULL,
    last_used_at  BIGINT       NOT NULL,
    expires_at    BIGINT       NOT NULL,
    remember      BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_player FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_player_id ON refresh_token (player_id);
