-- Dev bootstrap data — applied ONLY under the %dev profile
-- (%dev.quarkus.flyway.locations = classpath:db/migration,classpath:db/devseed).
-- Never on the classpath for %prod / %prodlike.
--
-- Gives `quarkus:dev` a usable environment out of the box: five login
-- accounts, all with the password "password". Player1 also holds the admin
-- roles so the admin UI is reachable without further setup.
--
-- password_hash is a jbcrypt hash (cost 13, matching PlayerService.gensalt)
-- of the literal string "password".

INSERT INTO player (player_id, player_name, email, password_hash, show_images, edge_color) VALUES
  ('d0000000-0000-0000-0000-000000000001', 'Player1', 'player1@example.com', '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#E53935'),
  ('d0000000-0000-0000-0000-000000000002', 'Player2', 'player2@example.com', '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#1E88E5'),
  ('d0000000-0000-0000-0000-000000000003', 'Player3', 'player3@example.com', '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#43A047'),
  ('d0000000-0000-0000-0000-000000000004', 'Player4', 'player4@example.com', '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#FDD835'),
  ('d0000000-0000-0000-0000-000000000005', 'Player5', 'player5@example.com', '$2a$13$W8jnE.g8vJWIYYTTr0vnLO5FdrldvGet9TKekJD36TmqO8F8t9ptu', TRUE, '#8E24AA')
ON CONFLICT (player_name) DO NOTHING;

INSERT INTO player_role (player_id, role) VALUES
  ('d0000000-0000-0000-0000-000000000001', 'ADMIN'),
  ('d0000000-0000-0000-0000-000000000001', 'TOURNAMENT_ADMIN')
ON CONFLICT (player_id, role) DO NOTHING;

INSERT INTO player_activity (player_id, last_seen) VALUES
  ('d0000000-0000-0000-0000-000000000001', now()),
  ('d0000000-0000-0000-0000-000000000002', now()),
  ('d0000000-0000-0000-0000-000000000003', now()),
  ('d0000000-0000-0000-0000-000000000004', now()),
  ('d0000000-0000-0000-0000-000000000005', now())
ON CONFLICT (player_id) DO NOTHING;
