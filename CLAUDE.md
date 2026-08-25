# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build WAR
./mvnw clean package

# Run locally (Tomcat 9, app served at /jol)
JOL_DATA=src/test/resources/data ./mvnw tomcat9:run

# Run the React frontend dev server alongside it (separate terminal) —
# see frontend/vite.config.ts's top comment for how this proxies to Tomcat
cd frontend && npm install && npm run dev

# Run all tests (excludes "Builder" group by default)
./mvnw test

# Run a single test class
./mvnw test -Dtest=DoCommandTest

# Run Cucumber BDD tests
./mvnw test -Dtest=RunCucumberTest

# Frontend: type-check + build, lint, unit/component tests (from frontend/)
npm run build
npm run lint
npm run test

# Frontend: end-to-end tests (Playwright) — starts both Tomcat (against a
# disposable copy of the test fixture data) and the Vite dev server itself;
# see frontend/README.md's Testing section for details
npm run test:e2e
```

Tests require `JOL_DATA` and `ENABLE_TEST_MODE=true` — these are set via `@SetEnvironmentVariable` on the test classes, so no manual setup is needed when running via Maven.

The `Builder` tag is excluded from the default test run — these are `CardDatabaseBuilder` tests that regenerate static card JSON/HTML served from `static.deckserver.net`.

## Key Environment Variables

| Variable | Purpose |
|---|---|
| `JOL_DATA` | Path to data directory (required) |
| `ENABLE_TEST_MODE` | Disables scheduled persistence (set to `true` in tests) |
| `ENABLE_CAPTCHA` | Set to `false` for local dev |
| `JOL_RECAPTCHA_KEY` / `JOL_RECAPTCHA_SECRET` | reCAPTCHA credentials |
| `DISCORD_BOT_TOKEN` / `DISCORD_PING_CHANNEL_ID` | Discord integration |
| `VAPID_PUBLIC_KEY` | Web push (VAPID) public key, fetched via `GET /jol/api/config` (`ConfigResource`) and used client-side by `frontend/src/push/pushNotifications.ts` for `pushManager.subscribe()`. Set via `.keys` (see `docker-compose.yml`'s `env_file`). The matching private key is **not** an env var — `NotificationService` reads it from `<JOL_DATA>/vapid_private.pem`, so that file must be copied into the `JOL_DATA` volume (e.g. from `notifications/vapid_private.pem`) for any environment that needs to send pushes. |
| `TYPE` | Visual env label (`dev`, `prod`, etc.) |
| `BASE_URL` | Origin card images/HTML/JSON tooltips are fetched from — exposed via `GET /jol/api/config` (`ConfigResource`), read by the React app's `getBaseUrl()` (`frontend/src/api/config.ts`). Only matters for a production build: in `npm run dev`, `getBaseUrl()` unconditionally returns a relative path instead, since Vite's own `serveCardAssets.ts` already serves the local `static/` directory directly — this env var is never consulted in dev at all. Defaults to `https://static.dev.deckserver.net`, which never resolves to anything reachable outside prod (it was only ever an `/etc/hosts` entry pointing at a now-removed local nginx container). Unset in prod (`docker-compose.yml`), where the real default is correct. |

## Architecture Overview

This is a **Vampire: The Eternal Struggle (VTES) online card game server** (deckserver.net), packaged as a Java WAR deployed on Tomcat 9. It uses **no database** — all state is persisted as JSON/XML files under `JOL_DATA`.

### Request Flow

1. Browser loads the React SPA (`frontend/`, bundled into the WAR at `/react/*`) — every top-level view (`/main`, `/lobby`, `/game/*`, etc.) is forwarded there by `MainServlet` once `AuthService` confirms a session; `/login` is served unauthenticated by `LoginServlet`
2. The SPA's `api/client.ts` (a thin fetch wrapper) calls `/jol/api/...` directly — there's no hand-written JS shim between them anymore (the old `ds.js` client and every legacy JSP it drove were deleted wholesale in the React migration)
3. Jersey JAX-RS resources (`net.deckserver.rest`) handle each endpoint and delegate to **`JolAdmin`** or services
4. `JolAdmin` manages in-memory `GameModel` / `PlayerModel` maps and routes to **`JolGame`** or **`DoCommand`**
5. Services persist state back to JSON files on a schedule (via `PersistedService`) or on demand
6. Server-push notifications are sent over WebSocket (`/ws/updates`) via `WebSocketRegistry`; the SPA treats each push as a signal to re-fetch (`ws/useGameSocket.ts`, `ws/useJolSocket.ts`), not as a payload carrier itself

### Package Map

- **`net.deckserver`** — `JolAdmin` (singleton orchestrator); `Recaptcha`
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
  - `validators/` — deck validation: `StandardDeckValidator`, `V5DeckValidator`, `DuelDeckValidator`, `PlayTestValidator` all extend `AbstractDeckValidator`; use `ValidatorFactory`
  - `model/` — core game logic
    - `JolGame` — record holding game id + `GameData`; all game state mutation methods
    - `DoCommand` — record; parses and executes player text commands (e.g. `burn library 1`)
    - `GameModel` — in-memory per-game view; held in `JolAdmin.gmap`
    - `GameView` — per-player region collapse/expand state (client now owns most of what this used to track — see `PlayerBoard.tsx`'s comment on `ALWAYS_COLLAPSED`)
    - `CommandParser` — tokenises command strings
- **`net.deckserver.servlet`** — no JSPs left (every one was deleted along with `ds.js`/`card-modal.js` once its React equivalent shipped — see Frontend + API Notes below)
  - `MainServlet` — `@WebServlet` on every top-level view path (`/`, `/main`, `/lobby`, `/game/*`, etc.); gates auth via `AuthService`, then always forwards to `/react/index.html`
  - `LoginServlet` — serves `/login` unauthenticated, forwarding to the same `/react/index.html` (the SPA renders the login page itself based on route)
  - `DwrCompatibilityServlet` — catches stray `/jol/dwr/**` calls from browser tabs still running the pre-migration client and forces a hard reload
  - `JolApplicationInitializer` — Jersey/Jakarta servlet bootstrap
- **`net.deckserver.rest`** — Jersey JAX-RS REST API (`/jol/api/...`); the SPA's sole backend interface
  - `bean/` — JSON response objects returned to the frontend (`GameSnapshot`, `NavBean`, `DeckEdit`, etc.), built directly by `net.deckserver.rest` resources and factories (e.g. `GameSnapshotFactory`) — the old `creators/` package and `UpdateFactory`/`JspRenderer`/`RequestContext` (which built one shared page-update response and rendered JSP fragments into it) were removed once every view had its own dedicated REST endpoint
  - `BaseResource` — base class: injects `SecurityContext` + HTTP context
  - `PageResource` — `GET /nav` (polled by `frontend/src/nav/NavContext.tsx` for the authenticated shell's nav state), `POST /chat`
  - `LobbyResource` — `/lobby/player/games` CRUD (create/start/close), deck registration, invites
  - `GameActionResource` / `GameStateResource` — the former holds what's still shared with the old DWR-era surface (deck/players/turns/history/notes); the latter is the SPA-only `/game/{id}/view` + `view/submit` + `view/end-turn` used by `GamePage.tsx`
  - `DeckResource` / `DeckPageResource` — deck CRUD, validation, and the deck-editor page's combined view
  - `AuthResource` — login/register/logout (replaces the old JSP form POSTs)
  - `ProfileResource`, `AdminResource`, `AdminPageResource`, `TournamentResource`, `MainResource`, `WatchResource`, `StatisticsResource`, `ConfigResource`, `NotificationResource` — one resource per SPA page/concern, each returning that page's own bean directly (see `frontend/src/api/types.ts` for the mirrored shapes)
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

The entire client is now `frontend/` — a Vite/TypeScript/React SPA. Every legacy JSP, and the hand-written `ds.js`/`card-modal.js` REST client/tooltip JS that drove them, were deleted outright once each view had a React equivalent; there's no incremental JSP-vs-React branching left anywhere in the request path (see `MainServlet`/`LoginServlet` above). See `frontend/README.md` for the frontend project's own structure, scripts, and testing setup — this section only covers how it fits into the wider app.

- New API methods go in `frontend/src/api/client.ts` (or a page-specific API module, e.g. `pages/login/authApi.ts`) + the matching JAX-RS resource. Add the response shape to `frontend/src/api/types.ts`, kept as hand-written mirrors of the Java beans — update both sides together when a bean's shape changes.
- JAX-RS resources return the bean directly (e.g. `GameSnapshot`, `LobbyPageBean`) — no shared envelope; each SPA page fetches only what it needs from its own dedicated endpoint(s).
- WebSocket at `/ws/updates` (Tomcat JSR-356) carries lightweight push signals — the SPA re-fetches the relevant REST endpoint on receipt (`frontend/src/ws/useGameSocket.ts`, `useJolSocket.ts`) rather than receiving full payloads over the socket. Because a state-saving action also notifies the actor's own session, any state derived only from a full-snapshot refresh (not the mutating request's own response) can be overwritten by that self-triggered refetch within the same round trip — see `CommandForm.tsx`'s `status` field for a concrete case (server-side command-validation errors are kept in local component state instead of read off the snapshot, for exactly this reason).
- Card HTML/JSON for tooltips/modals is generated by `CardDatabaseBuilder` (test-scope) and served statically from `static.deckserver.net` (CloudFront/nginx in prod). Local dev serves the same `static/` directory directly via `frontend/serveCardAssets.ts` (Vite) — no local nginx/Docker layer needed.
- The built SPA lands at `target/react-dist` (via `frontend-maven-plugin`) and is copied into the WAR at `/react/*` by `maven-war-plugin` — see `web.xml`'s `/react/*` static mapping and its comment for why that mapping has to exist explicitly (without it, requests for the SPA's own hashed asset files recurse into `MainServlet` and stack-overflow).

### Deployment

- Docker: `docker-compose.yml` runs `prod`, `test`, and `static` (nginx, serving prebuilt card assets from a named volume) containers in production, fronted by Traefik for TLS termination and routing
- Session clustering: Redisson (Redis) Tomcat session manager (configured in `tomcat9-maven-plugin` dependencies)
- AWS CloudFront SDK included for CDN invalidation