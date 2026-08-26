-- V11: Site notes and web push subscriptions

CREATE TABLE jol_site_notes (
    id    SMALLINT NOT NULL DEFAULT 1,
    notes TEXT     NOT NULL DEFAULT '',
    CONSTRAINT pk_jol_site_notes PRIMARY KEY (id),
    CONSTRAINT ck_jol_site_notes_singleton CHECK (id = 1)
);

CREATE TABLE jol_subscription (
    id             BIGSERIAL    NOT NULL,
    player_id      VARCHAR(36)  NOT NULL,
    endpoint       VARCHAR(1024) NOT NULL,
    auth_key       VARCHAR(255) NOT NULL,
    p256dh_key     VARCHAR(255) NOT NULL,
    failure_count  INT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_jol_subscription PRIMARY KEY (id),
    CONSTRAINT fk_jol_subscription_player FOREIGN KEY (player_id) REFERENCES jol_player (player_id) ON DELETE CASCADE,
    CONSTRAINT uq_jol_subscription_player_endpoint UNIQUE (player_id, endpoint)
);
