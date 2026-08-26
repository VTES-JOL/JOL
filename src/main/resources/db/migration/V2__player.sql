CREATE TABLE jol_player (
    player_name   VARCHAR(255) NOT NULL,
    player_id     VARCHAR(36)  NOT NULL,
    email         VARCHAR(255),
    password_hash VARCHAR(255),
    discord_id    VARCHAR(255),
    vekn_id       VARCHAR(255),
    country_code  VARCHAR(10),
    show_images   BOOLEAN      NOT NULL DEFAULT TRUE,
    edge_color    VARCHAR(20)  NOT NULL DEFAULT '#FFFFFF',
    CONSTRAINT pk_jol_player PRIMARY KEY (player_id),
    CONSTRAINT uq_jol_player_name UNIQUE (player_name)
);

CREATE TABLE jol_player_role (
    player_id VARCHAR(36)  NOT NULL,
    role      VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_jol_player_role PRIMARY KEY (player_id, role),
    CONSTRAINT fk_jol_player_role_player FOREIGN KEY (player_id) REFERENCES jol_player (player_id) ON DELETE CASCADE
);
