# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build WAR
./mvnw clean package

# Run locally (Tomcat 9, app served at /jol)
JOL_DATA=src/test/resources/data ./mvnw tomcat9:run

# Run all tests (excludes "Builder" group by default)
./mvnw test

# Run a single test class
./mvnw test -Dtest=DoCommandTest

# Run Cucumber BDD tests
./mvnw test -Dtest=RunCucumberTest
```

Tests require `JOL_DATA` and `ENABLE_TEST_MODE=true` — these are set via `@SetEnvironmentVariable` on the test classes, so no manual setup is needed when running via Maven.

The `Builder` tag is excluded from the default test run — these are `CardDatabaseBuilder` tests that regenerate static card JSON/HTML for the nginx static server.

## Key Environment Variables

| Variable | Purpose |
|---|---|
| `JOL_DATA` | Path to data directory (required) |
| `ENABLE_TEST_MODE` | Disables scheduled persistence (set to `true` in tests) |
| `ENABLE_CAPTCHA` | Set to `false` for local dev |
| `JOL_RECAPTCHA_KEY` / `JOL_RECAPTCHA_SECRET` | reCAPTCHA credentials |
| `DISCORD_BOT_TOKEN` / `DISCORD_PING_CHANNEL_ID` | Discord integration |
| `TYPE` | Visual env label (`dev`, `prod`, etc.) |

## Architecture Overview

This is a **Vampire: The Eternal Struggle (VTES) online card game server** (deckserver.net), packaged as a Java WAR deployed on Tomcat 9. It uses **no database** — all state is persisted as JSON/XML files under `JOL_DATA`.

### Request Flow

1. Browser calls hand-written `ds.js` (a fetch-based REST client) which posts to `/jol/api/...`
2. Jersey JAX-RS resources (`net.deckserver.rest`) handle each endpoint and delegate to **`JolAdmin`** or services
3. `JolAdmin` manages in-memory `GameModel` / `PlayerModel` maps and routes to **`JolGame`** or **`DoCommand`**
4. Services persist state back to JSON files on a schedule (via `PersistedService`) or on demand
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
    - `ModelLoader` — converts between UI objects and XML/JSON data objects
    - `CommandParser` — tokenises command strings
- **`net.deckserver.services`** — static service singletons
  - `PersistedService` — abstract base: scheduled JSON persistence, test-mode bypass, graceful shutdown (call `shutdown()` from `ServletContextListener`, not JVM hooks)
  - `DataPaths` — resolves `JOL_DATA` env var; use `DataPaths.path(...)` to build file paths
  - `GameService`, `PlayerService`, `DeckService`, `CardService`, `ChatService`, etc.
- **`net.deckserver.storage.json`**
  - `system/` — top-level data files: `GameInfo`, `PlayerInfo`, `DeckInfo`, `GameHistory`, tournament classes
  - `game/` — in-game state: `GameData`, `PlayerData`, `RegionData`, `CardData`, `TurnData`
  - `deck/` — deck structure: `Deck`, `Crypt`, `Library`, `DeckParser`
  - `cards/` — `CardSummary`, `SecuredCardLoader`
- **`net.deckserver.game`**
  - `enums/` — domain enums: `RegionType`, `CardType`, `Clan`, `Phase`, `GameStatus`, etc.
  - `jaxb/` — legacy XML serialization for `game.xml` (state) and `actions.xml` (chat history) via JaxB; `XmlFileUtils` wraps load/save
  - `validators/` — deck validation: `StandardDeckValidator`, `V5DeckValidator`, `DuelDeckValidator`, `PlayTestValidator` all extend `AbstractDeckValidator`; use `ValidatorFactory`
- **`net.deckserver.servlet`** — JSP/Servlet entry points: `LoginServlet`, `LogoutServlet`, `RegisterServlet`, `MainServlet`; JSP templates under `WEB-INF/jsps/`
  - `JspRenderer` — renders a JSP to a String (replaces DWR's `WebContextFactory.forwardToString()`)
  - `RequestContext` — thread-local holder for `HttpServletRequest`/`HttpServletResponse` used by `UpdateFactory`
- **`net.deckserver.rest`** — Jersey JAX-RS REST API (`/jol/api/...`); fully replaces DWR
  - `BaseResource` — base class: injects `SecurityContext` + HTTP context, calls `UpdateFactory`
  - `PageResource` — `POST /navigate`, `GET /poll`, `POST /chat`
  - `LobbyResource` — `POST /lobby/games`, deck registration, invites
  - `GameActionResource` — submit commands, end turn, toggle, notes, state, history
  - `DeckResource` — deck CRUD and validation
  - `UserResource` — profile, password, preferences
  - `AdminResource` — roles, player management, CSV export
  - `TournamentResource` — full tournament lifecycle
  - `SecurityFilter` — rejects unauthenticated API calls with 401
- **`net.deckserver.ws`** — WebSocket push
  - `JolWebSocketEndpoint` — `@ServerEndpoint("/ws/updates")`; shares HTTP session auth; handles join/leave/ping frames
  - `WebSocketRegistry` — tracks player→session mapping; `notifyMain()` / `notifyGame(gameId)` push update signals to clients
- **`net.deckserver.jobs`** — background jobs: `GameCleanUp`, `PublicGameBuilder`, `TournamentJob`, `GameDataConversion`
- **`net.deckserver.push`** — Web Push notification support

### Data File Layout (under `JOL_DATA`)

```
games.json          # Map<name, GameInfo>
players.json        # Map<name, PlayerInfo>
decks.json          # Map<playerName, Map<deckName, DeckInfo>>
registrations.json  # Map<gameName, Map<playerName, RegistrationStatus>>
pastGames.json      # Map<timestamp, GameHistory>
tournament.json     # TournamentData
chats.json          # List<ChatEntry>
timestamps.json     # Timestamps
decks/              # *.json  — deck files (ULID-named)
games/<uuid>/       # game.json, game.xml, actions.xml, <deckId>.json
cards/              # vtescrypt.csv, vteslib.csv  (VEKN official)
```

### Frontend + API Notes

- `src/main/webapp/js/ds.js` is a hand-written fetch-based REST client. It exposes the same `DS.*` surface the old DWR code did, so JSPs and game JS didn't need to change call sites. New API methods go in `ds.js` + the matching JAX-RS resource.
- Responses from JAX-RS resources are `Map<String, Object>` built by `UpdateFactory.getUpdate(playerName)` — same bean structure as before. `RequestContext.set(req, res)` must be called before `UpdateFactory` so `JspRenderer` can render JSP fragments into the response.
- WebSocket at `/ws/updates` (Tomcat JSR-356) carries lightweight push signals — clients re-poll the REST API on receipt rather than receiving full payloads over the socket.
- Card HTML/JSON for tooltips/modals is generated by `CardDatabaseBuilder` (test-scope) and served statically from nginx at `static.deckserver.net`.

### Deployment

- Docker: `docker-compose.yml` for production, `local-docker-compose.yml` for local static/app server
- Session clustering: Redisson (Redis) Tomcat session manager (configured in `tomcat9-maven-plugin` dependencies)
- AWS CloudFront SDK included for CDN invalidation