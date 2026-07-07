# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build WAR
./mvnw clean package

# Start local Postgres (required), then optionally load fixture data
docker compose -f local-docker-compose.yml up -d db
./migrate-to-db.sh src/test/resources/data   # reset DB + import fixture data

# Run locally (Tomcat 9, app served at /jol)
JOL_DB_PASSWORD=jol ./mvnw tomcat9:run

# Run all tests (excludes "Builder" group by default; uses in-memory H2, no Postgres needed)
./mvnw test

# Run a single test class
./mvnw test -Dtest=DoCommandTest

# Run Cucumber BDD tests
./mvnw test -Dtest=RunCucumberTest
```

Tests require `ENABLE_TEST_MODE=true` — set via `@SetEnvironmentVariable` on the test classes, so no manual setup is needed when running via Maven. Service-level tests boot an in-memory H2 database populated from `src/test/resources/data` by `JolServiceExtension`/`JolFixtureLoader`.

The `Builder` tag is excluded from the default test run — these are `CardDatabaseBuilder` tests that regenerate static card JSON/HTML for the nginx static server.

## Key Environment Variables

| Variable | Purpose |
|---|---|
| `JOL_DB_URL` | JDBC URL (default `jdbc:postgresql://localhost:5432/jol`) |
| `JOL_DB_USER` / `JOL_DB_PASSWORD` | Database credentials (default user `jol`) |
| `JOL_DB_POOL_SIZE` | HikariCP max pool size (default 10) |
| `JOL_LOGS` | Log directory (default `target`) |
| `VAPID_KEY_FILE` | Path to VAPID private key PEM; web push disabled when unset |
| `ENABLE_TEST_MODE` | Disables scheduled persistence and JPA writes (set to `true` in tests) |
| `ENABLE_CAPTCHA` | Set to `false` for local dev |
| `JOL_RECAPTCHA_KEY` / `JOL_RECAPTCHA_SECRET` | reCAPTCHA credentials |
| `DISCORD_BOT_TOKEN` / `DISCORD_PING_CHANNEL_ID` | Discord integration |
| `TYPE` | Visual env label (`dev`, `prod`, etc.) |

## Architecture Overview

This is a **Vampire: The Eternal Struggle (VTES) online card game server** (deckserver.net), packaged as a Java WAR deployed on Tomcat 9. State is persisted in **PostgreSQL** via JPA (Hibernate 7 + Flyway + HikariCP); services hold authoritative in-memory copies and write through to the DB (single-node assumption — see `PersistedService` javadoc). `migrate-to-db.sh` (repo root) resets the DB and imports the legacy `JOL_DATA` JSON files.

### Request Flow

1. Browser calls hand-written `ds.js` (a fetch-based REST client) which posts to `/jol/api/...`
2. Jersey JAX-RS resources (`net.deckserver.rest`) handle each endpoint and delegate to **`JolAdmin`** or services
3. `JolAdmin` manages in-memory `GameModel` / `PlayerModel` maps and routes to **`JolGame`** or **`DoCommand`**
4. Services write through to PostgreSQL immediately (`PersistedService.jpaWrite`), or flush write-behind caches on a schedule (game state, activity timestamps)
5. Server-push notifications are sent over WebSocket (`/ws/updates`) via `WebSocketRegistry`

### Package Map

- **`net.deckserver`** — `JolAdmin` (singleton orchestrator); `Recaptcha`
- **`net.deckserver.dwr`**
  - `bean/` — JSON response objects returned to the frontend
  - `creators/` — populate beans for each page/view (`GameCreator`, `LobbyCreator`, etc.); `UpdateFactory` builds the full page-update response
  - `model/` — core game logic
    - `JolGame` — record holding game id + `GameData`; all game state mutation methods
    - `DoCommand` — record; parses and executes player text commands (e.g. `burn library 1`)
    - `GameModel` — in-memory per-game view; held in `JolAdmin.gmap`
    - `PlayerModel` — in-memory per-player state; held in `JolAdmin.pmap`
    - `GameView` — player-centric view with toggle/changed state flags
    - `CommandParser` — tokenises command strings
- **`net.deckserver.services`** — static service singletons
  - `PersistedService` — abstract base: `jpaWrite()` transaction helper, optional scheduled flush (interval 0 = write-through only), test-mode bypass, graceful shutdown (call `shutdown()` from `ServletContextListener`, not JVM hooks)
  - `GameService`, `PlayerService`, `DeckService`, `CardService`, `ChatService`, etc.
- **`net.deckserver.jpa`** — persistence layer
  - `JpaFactory` — HikariCP + Flyway + EntityManagerFactory lifecycle; env-driven connection config
  - `entity/` — one entity per table (`jol_player`, `jol_game`, `jol_game_state`, `jol_game_snapshot`, `jol_registration`, `jol_deck_*`, `jol_tournament*`, `jol_game_history`, chat/activity tables)
  - `repository/` — plain repositories taking an `EntityManager`; callers own the transaction (via `PersistedService.jpaWrite`)
  - Migrations live in `src/main/resources/db/migration/` (Flyway, `hbm2ddl=validate` in prod; H2 tests use `create-drop`)
- **`net.deckserver.storage.json`**
  - `system/` — top-level data objects: `GameInfo`, `PlayerInfo`, `DeckInfo`, `GameHistory`, tournament classes
  - `game/` — in-game state: `GameData`, `PlayerData`, `RegionData`, `CardData`, `TurnData`
  - `deck/` — deck structure: `Deck`, `Crypt`, `Library`, `DeckParser`
  - `cards/` — `CardSummary`, `SecuredCardLoader`
- **`net.deckserver.game`**
  - `enums/` — domain enums: `RegionType`, `CardType`, `Clan`, `Phase`, `GameStatus`, etc.
  - `ui/` — `CardDetail`: rich per-card view object used by game model
  - `validators/` — deck validation: `StandardDeckValidator`, `V5DeckValidator`, `DuelDeckValidator`, `PlayTestValidator` all extend `AbstractDeckValidator`; use `ValidatorFactory`
- **`net.deckserver.servlet`** — JSP/Servlet entry points: `LoginServlet`, `LogoutServlet`, `RegisterServlet`, `MainServlet`; JSP templates under `WEB-INF/jsps/`
  - `JspRenderer` — renders a JSP to a String (replaces DWR's `WebContextFactory.forwardToString()`)
  - `RequestContext` — thread-local holder for `HttpServletRequest`/`HttpServletResponse` used by `UpdateFactory`
  - `DwrCompatibilityServlet` — catches lingering `/jol/dwr/**` requests from pre-migration browsers and forces a hard reload
- **`net.deckserver.rest`** — Jersey JAX-RS REST API (`/jol/api/...`); fully replaces DWR
  - `BaseResource` — base class: injects `SecurityContext` + HTTP context, calls `UpdateFactory`
  - `PageResource` — `POST /navigate`, `GET /poll`, `POST /chat`
  - `LobbyResource` — `POST /lobby/games`, deck registration, invites
  - `GameActionResource` — submit commands, end turn, toggle, notes, state, history
  - `DeckResource` — deck CRUD and validation
  - `UserResource` — profile, password, preferences
  - `AdminResource` — roles, player management, CSV export
  - `TournamentResource` — full tournament lifecycle
  - `GameResource` — `GET /games` (lobby game list)
  - `PlayerResource` — `GET /me` (current player profile)
  - `NotificationResource` — `POST /subscription` (web push subscriptions)
  - `SystemResource` — `GET /stats`
  - `SecurityFilter` — rejects unauthenticated API calls with 401
- **`net.deckserver.ws`** — WebSocket push
  - `JolWebSocketEndpoint` — `@ServerEndpoint("/ws/updates")`; shares HTTP session auth; handles join/leave/ping frames
  - `WebSocketRegistry` — tracks player→session mapping; `notifyMain()` / `notifyGame(gameId)` push update signals to clients
- **`net.deckserver.jobs`** — background jobs: `GameCleanUp`, `PublicGameBuilder`, `TournamentJob`
- **`net.deckserver.push`** — Web Push notification support

### Database Layout (PostgreSQL, Flyway-managed)

```
jol_player / jol_player_role        # players + roles
jol_game                            # game metadata (name, id, owner, status, format)
jol_game_state                      # serialized GameData JSON, @Version optimistic locking
jol_game_snapshot                   # per-turn state snapshots for admin rollback
jol_game_chat                       # serialized turn history JSON
jol_registration                    # game registrations incl. deck snapshot (deck_content)
jol_deck_info / jol_deck_format / jol_deck_content   # decks; legacy decks store raw text in content
jol_tournament / jol_tournament_registration          # tournaments incl. deck snapshots
jol_player_activity / jol_game_activity               # timestamps (write-behind, 1-min flush)
jol_global_chat                     # lobby chat (trimmed to last 1000)
jol_game_history                    # past game results (pastGames.json successor)
```

Legacy file data under the old `JOL_DATA` directory is imported by `migrate-to-db.sh`.

### Frontend + API Notes

- `src/main/webapp/js/ds.js` is a hand-written fetch-based REST client. It exposes the same `DS.*` surface the old DWR code did, so JSPs and game JS didn't need to change call sites. New API methods go in `ds.js` + the matching JAX-RS resource.
- Responses from JAX-RS resources are `Map<String, Object>` built by `UpdateFactory.getUpdate(playerName)` — same bean structure as before. `RequestContext.set(req, res)` must be called before `UpdateFactory` so `JspRenderer` can render JSP fragments into the response.
- WebSocket at `/ws/updates` (Tomcat JSR-356) carries lightweight push signals — clients re-poll the REST API on receipt rather than receiving full payloads over the socket.
- Card HTML/JSON for tooltips/modals is generated by `CardDatabaseBuilder` (test-scope) and served statically from nginx at `static.deckserver.net`.

### Deployment

- Docker: `docker-compose.yml` for production (prod + test apps, each with its own `postgres:16` service; DB passwords from compose `.env`), `local-docker-compose.yml` for local Postgres/static server
- Session clustering: Redisson (Redis) Tomcat session manager (configured in `tomcat9-maven-plugin` dependencies)
- AWS CloudFront SDK included for CDN invalidation