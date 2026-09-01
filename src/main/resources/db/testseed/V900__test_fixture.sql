-- Declarative fixture for the JPA test tier — applied only by
-- net.deckserver.testsupport.PostgresJpaExtension.applyTestSeed()
-- (flyway locations = classpath:db/migration,classpath:db/testseed).
-- NOT on the runtime classpath for any deployed profile.
--
-- Every name/id is prefixed "fixture-" / "f0000000-..." so it never collides
-- with the ad-hoc rows the *RepositoryTest classes insert for themselves.
-- Assertions against this data live in FixtureDataTest.
--
-- password_hash is a jbcrypt hash (cost 13) of the literal string "password".

INSERT INTO player (player_id, player_name, email, password_hash, show_images, edge_color) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'fixture-alice', 'alice@fixture.test', '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#FFFFFF'),
  ('f0000000-0000-0000-0000-000000000002', 'fixture-bob',   'bob@fixture.test',   '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#FFFFFF'),
  ('f0000000-0000-0000-0000-000000000003', 'fixture-carol', 'carol@fixture.test', '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#FFFFFF');

INSERT INTO player_role (player_id, role) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'JUDGE');

INSERT INTO deck_info (player_id, deck_name, deck_id, format) VALUES
  ('f0000000-0000-0000-0000-000000000001', 'Fixture Weenie', 'f0000000-0000-0000-0000-0000000000d1', 'TAGGED'),
  ('f0000000-0000-0000-0000-000000000001', 'Fixture Wall',   'f0000000-0000-0000-0000-0000000000d2', 'TAGGED');

INSERT INTO deck_content (deck_id, content) VALUES
  ('f0000000-0000-0000-0000-0000000000d1', '{"deck":{"name":"Fixture Weenie","crypt":{"count":0},"library":{"count":0}}}'),
  ('f0000000-0000-0000-0000-0000000000d2', '{"deck":{"name":"Fixture Wall","crypt":{"count":0},"library":{"count":0}}}');

INSERT INTO game (game_name, game_id, owner_id, visibility, status, game_format, created_at, version) VALUES
  ('fixture-game', 'f0000000-0000-0000-0000-00000000a001',
   'f0000000-0000-0000-0000-000000000001', 'PUBLIC', 'ACTIVE', 'STANDARD',
   TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', 2);

INSERT INTO game_state (game_id, state, current_player, turn, phase, player_count, updated_at) VALUES
  ('f0000000-0000-0000-0000-00000000a001', '{}', 'fixture-alice', '1', 'UNLOCK', 2,
   TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00');
