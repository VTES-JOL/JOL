CREATE TABLE jol_tournament (
    tournament_id       VARCHAR(36)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    registration_start  TIMESTAMP WITH TIME ZONE,
    registration_end    TIMESTAMP WITH TIME ZONE,
    play_starts         TIMESTAMP WITH TIME ZONE,
    play_ends           TIMESTAMP WITH TIME ZONE,
    format              VARCHAR(50),
    deck_format         VARCHAR(30),
    number_of_rounds    INTEGER      NOT NULL DEFAULT 0,
    final_enabled       BOOLEAN      NOT NULL DEFAULT FALSE,
    requires_id         BOOLEAN      NOT NULL DEFAULT FALSE,
    status              VARCHAR(20)  NOT NULL DEFAULT 'EDIT',
    rules               TEXT,
    special_rules       TEXT,
    rounds              TEXT,
    finals              TEXT,
    CONSTRAINT pk_jol_tournament PRIMARY KEY (tournament_id),
    CONSTRAINT uq_jol_tournament_name UNIQUE (name)
);

CREATE TABLE jol_tournament_registration (
    id              BIGSERIAL    NOT NULL,
    tournament_id   VARCHAR(36)  NOT NULL,
    player_id       VARCHAR(36)  NOT NULL,
    vekn            VARCHAR(50),
    deck_id         VARCHAR(36),
    deck_content    TEXT,
    CONSTRAINT pk_jol_tournament_registration PRIMARY KEY (id),
    CONSTRAINT uq_jol_tournament_player UNIQUE (tournament_id, player_id),
    CONSTRAINT fk_jol_tournament_reg_tournament FOREIGN KEY (tournament_id) REFERENCES jol_tournament (tournament_id) ON DELETE CASCADE
);
