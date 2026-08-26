-- V14: Seed the "SYSTEM" account and add the missing FK from global_chat to
-- player.
--
-- GlobalChatRepository.insert() looks up the poster by name and silently
-- skips the DB write if no matching player row exists. Several background
-- jobs (PublicGameBuilder, TournamentJob, GameCleanUp, TournamentService)
-- post global chat as player "SYSTEM", but no such player row has ever
-- existed -- those announcements were only ever added to the in-memory
-- chat cache and never persisted.

INSERT INTO player (player_id, player_name, password_hash, show_images, edge_color)
VALUES ('00000000-0000-0000-0000-000000000000', 'SYSTEM',
        '$2a$13$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
        TRUE, '#FFFFFF')
ON CONFLICT (player_name) DO NOTHING;

-- Drop any pre-existing global_chat rows that don't resolve to a real player
-- (only reachable via the legacy JSON import path in migrate-to-db.sh, which
-- already documents dropping chat from unknown/deleted players) so the new
-- FK below can be added.
DELETE FROM global_chat gc
WHERE NOT EXISTS (SELECT 1 FROM player p WHERE p.player_id = gc.player_id);

CREATE INDEX idx_global_chat_player_id ON global_chat (player_id);

ALTER TABLE global_chat
    ADD CONSTRAINT fk_global_chat_player
        FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE;
