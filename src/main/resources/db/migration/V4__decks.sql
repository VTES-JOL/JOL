CREATE TABLE jol_deck_info (
    player_id VARCHAR(36)  NOT NULL,
    deck_name VARCHAR(255) NOT NULL,
    deck_id   VARCHAR(36)  NOT NULL,
    format    VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_jol_deck_info PRIMARY KEY (player_id, deck_name),
    CONSTRAINT uq_jol_deck_info_id UNIQUE (deck_id),
    CONSTRAINT fk_jol_deck_info_player FOREIGN KEY (player_id) REFERENCES jol_player (player_id)
);

CREATE TABLE jol_deck_format (
    deck_id    VARCHAR(36)  NOT NULL,
    format_tag VARCHAR(100) NOT NULL,
    CONSTRAINT pk_jol_deck_format PRIMARY KEY (deck_id, format_tag),
    CONSTRAINT fk_jol_deck_format_deck FOREIGN KEY (deck_id) REFERENCES jol_deck_info (deck_id) ON DELETE CASCADE
);

CREATE TABLE jol_deck_content (
    deck_id VARCHAR(36) NOT NULL,
    content TEXT        NOT NULL,
    CONSTRAINT pk_jol_deck_content PRIMARY KEY (deck_id),
    CONSTRAINT fk_jol_deck_content_deck FOREIGN KEY (deck_id) REFERENCES jol_deck_info (deck_id) ON DELETE CASCADE
);
