-- V15: Add ON DELETE CASCADE to registration -> game and game_activity -> game,
-- matching the cascade already used for game_state/game_chat/game_snapshot.
--
-- Without this, GameService.remove() only works because every call site
-- happens to delete registration/game_activity rows itself first
-- (RegistrationService.clearRegistrations, gameActivityRepository.delete) --
-- a convention enforced by nothing but code review. A future caller (or a
-- manual admin deletion) that skips that ordering hits an FK violation
-- instead of a clean cascade.

ALTER TABLE registration
    DROP CONSTRAINT fk_jol_registration_game,
    ADD  CONSTRAINT fk_jol_registration_game
        FOREIGN KEY (game_id) REFERENCES game (game_id) ON DELETE CASCADE;

ALTER TABLE game_activity
    DROP CONSTRAINT fk_jol_game_activity_game,
    ADD  CONSTRAINT fk_jol_game_activity_game
        FOREIGN KEY (game_id) REFERENCES game (game_id) ON DELETE CASCADE;
