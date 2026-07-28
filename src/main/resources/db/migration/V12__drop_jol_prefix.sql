-- V12: Drop the redundant "jol_" prefix from table names — the whole schema
-- already lives in its own database, so the prefix added nothing.

ALTER TABLE jol_player                  RENAME TO player;
ALTER TABLE jol_player_role             RENAME TO player_role;
ALTER TABLE jol_game                    RENAME TO game;
ALTER TABLE jol_registration            RENAME TO registration;
ALTER TABLE jol_deck_info               RENAME TO deck_info;
ALTER TABLE jol_deck_format             RENAME TO deck_format;
ALTER TABLE jol_deck_content            RENAME TO deck_content;
ALTER TABLE jol_tournament              RENAME TO tournament;
ALTER TABLE jol_tournament_registration RENAME TO tournament_registration;
ALTER TABLE jol_game_state              RENAME TO game_state;
ALTER TABLE jol_game_chat               RENAME TO game_chat;
ALTER TABLE jol_player_activity         RENAME TO player_activity;
ALTER TABLE jol_game_activity           RENAME TO game_activity;
ALTER TABLE jol_global_chat             RENAME TO global_chat;
ALTER TABLE jol_game_history            RENAME TO game_history;
ALTER TABLE jol_game_snapshot           RENAME TO game_snapshot;
ALTER TABLE jol_site_notes              RENAME TO site_notes;
ALTER TABLE jol_subscription            RENAME TO subscription;
