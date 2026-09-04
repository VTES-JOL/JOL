-- V22: retire the SUPER_USER and PLAYTESTER player roles and the PLAYTEST game
-- format.
--
-- The enum constants PlayerRole.SUPER_USER / PlayerRole.PLAYTESTER and
-- GameFormat.PLAYTEST were removed in code. Both role and game/tournament
-- format columns are @Enumerated(EnumType.STRING), so any surviving row would
-- fail to hydrate ("No enum constant ...") on the next boot. Scrub them:
--
--   * player_role  — drop the two retired role grants outright.
--   * game / tournament — fold PLAYTEST format rows back to STANDARD (the
--     PlayTestValidator was just StandardDeckValidator with playtest cards
--     allowed; an in-progress game's state is not re-validated anyway).
--   * deck_format  — free-text tag table; remove the now-meaningless tag.

DELETE FROM player_role WHERE role IN ('SUPER_USER', 'PLAYTESTER');

UPDATE game       SET game_format = 'STANDARD' WHERE game_format = 'PLAYTEST';
UPDATE tournament SET deck_format = 'STANDARD' WHERE deck_format = 'PLAYTEST';

DELETE FROM deck_format WHERE format_tag = 'PLAYTEST';
