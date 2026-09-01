-- V18: Row-per-message game chat, replacing the game_chat.history JSON blob.
--
-- Each net.deckserver.storage.json.game.ChatData becomes one row. Turn grouping
-- (net.deckserver.storage.json.game.TurnData) is denormalised onto every row
-- rather than kept in a separate game_turn table -- a turn only carries
-- (player, turnId), so a join table would add churn for no query benefit.
--
-- Ordering within a game is (turn_seq, chat_seq) with the BIGSERIAL id as final
-- tiebreaker; both seq columns are written explicitly by ChatService so order is
-- stable regardless of id assignment.
--
-- game_chat is left in place for one release as a rollback safety net; nothing
-- reads it after this migration. A later V19 drops it.

CREATE TABLE game_chat_message (
    id            BIGSERIAL    PRIMARY KEY,
    game_id       VARCHAR(36)  NOT NULL REFERENCES game (game_id) ON DELETE CASCADE,
    turn_seq      INTEGER      NOT NULL,   -- 0-based ordinal of the turn within the game
    chat_seq      INTEGER      NOT NULL,   -- 0-based ordinal of the message within its turn
    turn_id       VARCHAR(50)  NOT NULL,   -- e.g. "3.1"
    turn_player   VARCHAR(255) NOT NULL,   -- active player that turn
    turn_label    VARCHAR(320) NOT NULL,   -- "<turn_player> <turn_id>" -- the /history?turn= key
    posted_at     TIMESTAMP WITH TIME ZONE NOT NULL, -- real insert time; migration uses game_chat.updated_at
    display_ts    VARCHAR(64),             -- ChatData.timestamp legacy display string ("3-Feb 14:05")
    source        VARCHAR(320),            -- ChatData.source (player / "SYSTEM" / "Judge - X")
    message       TEXT         NOT NULL,
    command       TEXT,                    -- existing synthetic canonical command (ChatData.command)
    invocation    TEXT,                    -- raw text the player submitted for the command that produced this line
    invocation_by VARCHAR(255)             -- who issued that command
);

CREATE INDEX ix_game_chat_message_turn  ON game_chat_message (game_id, turn_seq, chat_seq, id);
CREATE INDEX ix_game_chat_message_label ON game_chat_message (game_id, turn_label, id);

-- Self-backfill from the existing blob so an already-migrated database needs no
-- external script run. Fresh imports (migrate-to-db.sh) run this against an empty
-- game_chat and load game_chat_message directly instead.
INSERT INTO game_chat_message
    (game_id, turn_seq, chat_seq, turn_id, turn_player, turn_label,
     posted_at, display_ts, source, message, command)
SELECT
    gc.game_id,
    (t.ord - 1)::int,
    (c.ord - 1)::int,
    t.turn ->> 'turnId',
    t.turn ->> 'player',
    (t.turn ->> 'player') || ' ' || (t.turn ->> 'turnId'),
    gc.updated_at,
    c.chat ->> 'timestamp',
    c.chat ->> 'source',
    COALESCE(c.chat ->> 'message', ''),
    c.chat ->> 'command'
FROM game_chat gc
CROSS JOIN LATERAL jsonb_array_elements(gc.history::jsonb)
     WITH ORDINALITY AS t(turn, ord)
CROSS JOIN LATERAL jsonb_array_elements(COALESCE(t.turn -> 'chats', '[]'::jsonb))
     WITH ORDINALITY AS c(chat, ord)
WHERE gc.history IS NOT NULL
  AND gc.history <> ''
  AND (t.turn ->> 'turnId') IS NOT NULL
  AND (t.turn ->> 'player') IS NOT NULL;
