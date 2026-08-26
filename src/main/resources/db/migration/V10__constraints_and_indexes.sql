-- V10: Add missing FK indexes, cascade deletes, and column constraints

-- Indexes on foreign key columns missing from previous migrations
CREATE INDEX idx_registration_game_id       ON jol_registration(game_id);
CREATE INDEX idx_registration_player_id     ON jol_registration(player_id);
CREATE INDEX idx_tournament_reg_player_id   ON jol_tournament_registration(player_id);
CREATE INDEX idx_game_owner_id              ON jol_game(owner_id);

-- Add ON DELETE CASCADE to game_state → game and game_chat → game_state
-- so that removing a game automatically cleans up its state and chat
ALTER TABLE jol_game_state
    DROP CONSTRAINT fk_jol_game_state_game,
    ADD  CONSTRAINT fk_jol_game_state_game
        FOREIGN KEY (game_id) REFERENCES jol_game (game_id) ON DELETE CASCADE;

ALTER TABLE jol_game_chat
    DROP CONSTRAINT fk_jol_game_chat_state,
    ADD  CONSTRAINT fk_jol_game_chat_state
        FOREIGN KEY (game_id) REFERENCES jol_game_state (game_id) ON DELETE CASCADE;

-- Ensure every player row has a password hash.
-- Rows that somehow have no hash get an impossible-to-match sentinel
-- (syntactically valid BCrypt, will never verify against any real password).
UPDATE jol_player
    SET password_hash = '$2a$13$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
    WHERE password_hash IS NULL;
ALTER TABLE jol_player ALTER COLUMN password_hash SET NOT NULL;

-- Drop erroneous unique constraint on recorded_at in jol_game_history.
-- Two games can finish at the same microsecond; the BIGSERIAL pk already
-- provides uniqueness.
ALTER TABLE jol_game_history DROP CONSTRAINT uq_jol_game_history_recorded_at;
