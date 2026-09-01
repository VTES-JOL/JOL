-- V19: failed player command attempts (mistypes / invalid commands).
--
-- These are NOT chat: a command that fails to parse or validate produces no
-- chat line today, and shouldn't -- it would clutter the log for every player.
-- But a judge investigating a misplay wants to see that an attempt was made.
-- So they live in their own table, are never loaded into TurnHistory, and are
-- only served to a judge who is not seated in the game (surfaced under the game
-- chat "Commands" toggle).

CREATE TABLE game_command_error (
    id          BIGSERIAL    PRIMARY KEY,
    game_id     VARCHAR(36)  NOT NULL REFERENCES game (game_id) ON DELETE CASCADE,
    turn_label  VARCHAR(320) NOT NULL,   -- "<player> <turn_id>" of the turn it was attempted in
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    display_ts  VARCHAR(64),             -- "d-MMM HH:mm" style, matching ChatData.timestamp
    player      VARCHAR(255) NOT NULL,   -- who typed it
    raw_command TEXT         NOT NULL,   -- exactly what they submitted
    error_text  TEXT                     -- the CommandException message, if any
);

CREATE INDEX ix_game_command_error_game ON game_command_error (game_id, id);
CREATE INDEX ix_game_command_error_turn ON game_command_error (game_id, turn_label, id);
