# V:TES Online (deckserver.net)

Online client and game server for Vampire: The Eternal Struggle. Built on **Quarkus**, packaged as a runnable jar. State is persisted in **PostgreSQL** via JPA (Hibernate + Flyway + Agroal, all Quarkus-managed) — see [Storage Mechanics](#storage-mechanics) below. (An older JSON-file-per-directory storage model predates this; [Migrating JSON Data to PostgreSQL](#migrating-json-data-to-postgresql) covers moving an existing `JOL_DATA` directory over.)

# Requirements
* **Java 21** (Corretto recommended — Lombok's annotation processing breaks on newer JDKs; do **not** build with JDK 25)
* **Maven 3+** — or just use the bundled `./mvnw` wrapper
* **Node 22.x** (CI pins `22.17.0`; see `.nvmrc`) — Quinoa builds the frontend with whatever `node` is on your `PATH`, and Vite 8 / rolldown-vite requires Node ≥ 20.19 / 22.12. An older Node fails the frontend build with an opaque Vite error.
* **Docker** — for the local PostgreSQL container (`local-docker-compose.yml`)
* **`psql`, `jq`, `python3`** on your `PATH` — used by `migrate-to-db.sh` / `load-test-fixtures.sh` to load fixture data. On macOS: `brew install libpq jq` then `brew link --force libpq` to put `psql` on your `PATH` (`python3` ships with the Xcode command-line tools).

## Java
Install Java 21 as per your operating system.

## Node
Install Node 22.x (`nvm install` picks up the pinned version from `.nvmrc`). Only needed on your `PATH` — there's no separate frontend install step for normal use; `quarkus:dev` drives the frontend build itself via Quinoa.

## Maven
### Build
`./mvnw clean package` (macOS/Linux) or `mvnw.cmd clean package` (Windows)

### Run
#### Environment Variables
None of the below are required for a local `quarkus:dev` run against the fixture database — the two flags in the quickstart command (`JOL_DB_PASSWORD=jol ENABLE_CAPTCHA=false`) are enough. For docker-compose / production-style runs, copy `.env.example` → `.env` and `.keys.example` → `.keys` (both git-ignored) and fill them in; each optional integration (captcha, Discord, web push, signed card URLs) is simply inactive when its variables are unset.

* `JOL_DB_URL` - JDBC URL, default `jdbc:postgresql://localhost:5432/jol`
* `JOL_DB_USER` / `JOL_DB_PASSWORD` - database credentials, default user `jol`
* `JOL_DB_POOL_SIZE` - Agroal (Quarkus's connection pool) max pool size, default `10`
* `ENABLE_TEST_MODE` - skips the startup hook (`JpaStartup`) that calls `JpaFactory.initialize()` (tests initialize their own H2 instance instead — see [Tests](#tests) below) and makes every service's write-through methods a no-op that only updates in-memory state, so tests never touch a real database; set automatically by `@SetEnvironmentVariable` in tests, default: `false`
* `ENABLE_CAPTCHA` - disable Cloudflare Turnstile for local development if required, default: `true`
* `JOL_RECAPTCHA_KEY` / `JOL_RECAPTCHA_SECRET` - Cloudflare Turnstile site key / secret key, required when captcha is enabled (named "recaptcha" for historical reasons — this is Turnstile, not Google reCAPTCHA)
* `DISCORD_BOT_TOKEN` / `DISCORD_PING_CHANNEL_ID` / `DISCORD_API_VERSION` - required for Discord integration - bot token, channel id to post pings, and Discord API version (e.g. `10`); there is no code-level default for any of the three
* `VAPID_PUBLIC_KEY` - required for web push notifications - VAPID public key embedded client-side in `src/main/webui/src/push/pushNotifications.ts` for `pushManager.subscribe()`
* `VAPID_KEY_FILE` - path to the VAPID private key PEM file; `NotificationService` reads it at startup, and web push is silently disabled (not a startup failure) when unset or unreadable
* `JWT_SECRET_FILE` - path to the JWT signing key file (base64, HMAC); `AuthService` generates and writes one on first run if the file doesn't exist yet, falling back to `jwt_secret.key` in the working directory when unset (fine for local dev, but set explicitly anywhere the working directory isn't stable/persistent)
* `BASE_URL` / `KEY_ID` / `KEY_FILE` - required for CloudFront-signed secured card image URLs - distribution base URL, key pair id, and path to the CloudFront private key file, used by `SecuredCardLoader`
* `TYPE` - visual/environment label (`dev`, `test`, `prod`, ...), default: `dev`

Generate the dev-only HTTPS keystore (a one-time step — the file is git-ignored, and `quarkus:dev` binds port 8443 over TLS and will not start without it):
```bash
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -validity 3650 \
  -keystore src/main/resources/dev-keystore.p12 -storetype PKCS12 -storepass changeit \
  -dname "CN=localhost" -ext "SAN=dns:localhost,ip:127.0.0.1"
```
Re-run it only if you delete the keystore or it expires. (This command is also kept in `application.properties`'s `%dev.quarkus.http.ssl-port` comment block.)

Then start a local Postgres, load fixture data, and run `quarkus:dev` (see [Migrating JSON Data to PostgreSQL](#migrating-json-data-to-postgresql) below for the two commands referenced here):
```bash
docker compose -f local-docker-compose.yml up -d db
./load-test-fixtures.sh                    # resets the local db to Player1-5 fixture data
JOL_DB_PASSWORD=jol ENABLE_CAPTCHA=false ./mvnw quarkus:dev
```

The app is served at `https://localhost:8443/jol` (self-signed dev cert — see `application.properties`'s `%dev.quarkus.http.ssl-port` block; required because login cookies are `Secure`). Your browser will warn about the self-signed cert on first visit — accept it to proceed. Production runs plain HTTP behind Traefik's own TLS termination instead.

**Logging in:** the fixture set has accounts `Player1`–`Player8`, all with password `password`. `Player1` carries `JUDGE` / `PLAYTESTER` / `SUPER_USER` roles, and the fixtures include a "Test Game" that `Player1`–`Player5` are already registered in.

#### Frontend (React)
The entire client is `src/main/webui/` (Quinoa's own default location for a Quarkus project's frontend) — a Vite/TypeScript/React SPA; there's no legacy JSP/jQuery page left anywhere in the request path. See [Frontend Architecture](#frontend-architecture) below for how it fits into the wider app, and `src/main/webui/README.md` for the frontend project's own structure and testing setup. `quarkus:dev` above starts it automatically (via the Quinoa extension) — no separate frontend command needed. Running `npm run dev` standalone from `src/main/webui/` is only useful for iterating on the frontend build config itself.

### Tests
`./mvnw test` runs the full suite (Cucumber BDD scenarios included) except the `Builder`-tagged group, which regenerates the static card JSON/HTML served from `static.deckserver.net` and isn't part of normal test runs. Service-level tests boot an in-memory H2 database populated from `src/test/resources/data` by `JolServiceExtension`/`JolFixtureLoader` (see `src/test/resources/META-INF/persistence.xml`) — register any new JPA entity in both persistence units there, not just the main one, or JPA-backed tests fail with "Unknown entity type".

`./mvnw test -Dtest=DoCommandTest` runs a single test class.

### Database Schema
Tables live in one Postgres database, versioned by Flyway migrations under `src/main/resources/db/migration/` (`V1__baseline.sql` onward — add a new `V<n>__description.sql` file for schema changes, never edit a merged one).
* `player` / `player_role` - accounts, hashed passwords (jBCrypt), and roles
* `player_activity` / `game_activity` - last-activity tracking, per player and per game
* `game` - one row per game (name, status, format, owner, tournament link, ...)
* `game_state` - the live `GameData` for each game, as a JSON blob column
* `game_snapshot` - one row per turn, used for rollback
* `game_chat` - the chat/action history for a game, as a JSON blob column
* `game_history` - past-game VP/GW results
* `registration` - who's registered which deck into which game, including a frozen `deck_content` snapshot of the deck as registered (independent of later edits to the player's own deck)
* `deck_info` / `deck_format` / `deck_content` - a player's decks: metadata, format tags, and the deck's own content
* `tournament` / `tournament_registration` - tournament definitions and player registrations, same frozen-deck-snapshot pattern as `registration`
* `global_chat` - site-wide chat
* `site_notes` - admin-editable site banner/notes shown to players (singleton row)
* `subscription` - web push subscription records
* `refresh_token` - "remember me" refresh tokens, one row per logged-in device (see [Authentication](#authentication) below)

Most large free-form structures (game state, deck content, tournament rounds/rules) are stored as a JSON/text blob in a single column rather than fully normalized — each JPA entity's `toXxx()`/`from()` methods handle the (de)serialization against a matching `net.deckserver.storage.json.*` class, which is now the domain/API model rather than a file format: it's still what services hold in memory and what REST beans wrap, just no longer what gets serialized to disk.

Card data (crypt/library text, images) is *not* in this database at all — `CardService` fetches a pre-built `cards.json` from CloudFront (`static.deckserver.net`), falling back to a local `static/secured/cards.json` if that's unreachable. That file is generated from VEKN's official `vtescrypt.csv`/`vteslib.csv` by the test-scope `CardDatabaseBuilder` (the `Builder`-tagged tests). Keeping it out of the database means it stays identical across every deployment without needing to be synced.

### Storage Mechanics
Each `*Service` singleton (`PlayerService`, `GameService`, `TournamentService`, ...) extends `PersistedService`, which provides the `jpaRead`/`jpaWrite`/`jpaWriteThenMutate`/`jpaWriteWithRollback`/`jpaWriteAlways` helpers every service's persistence goes through — but not all in the same way:

1. **Write-through with an in-memory cache** (most services): the constructor loads its table into an in-memory `Map` once; reads are plain map lookups (`PlayerService.get(name)` is a `HashMap` lookup, not a query), and every mutation writes to Postgres *and* the map together via `jpaWriteThenMutate` (persist first, then publish the in-memory change) or `jpaWriteWithRollback` (mutate optimistically, roll the map back if the write fails).
2. **Stateless, no cache** (`DeckService`, `HistoryService`): every read is a live query — there's no cache to keep warm because a deck or history record is read far less often relative to how large its content is, so a full-table cache isn't worth holding.
3. **Batched write-behind** (`PlayerActivityService`, `PlayerGameActivityService` only): these two are hit on essentially every request (a nav poll, a game view), so a synchronous DB write per call would put every poll on the write path. They keep the in-memory-map design and flush on a fixed interval instead (see each class's own comment for why), same pattern write-through services used to follow entirely before the migration.
4. `ENABLE_TEST_MODE=true` skips the `JpaStartup`/`JpaFactory.initialize()` hook and makes every write helper a no-op that only updates in-memory state, so tests run against an in-memory H2 instance (see [Tests](#tests) above) without ever touching Postgres for the assertion itself.

**Single-node assumption:** the in-memory copy each write-through service holds is treated as authoritative, with the database as a write-through copy of it — a second app node would diverge from the first immediately, so scaling out would mean moving reads to the database first. Graceful shutdown (`PersistedService`'s `@Observes ShutdownEvent` — CDI-managed, not a JVM shutdown hook or the old `ServletContextListener`) flushes every batched service and lets Quarkus close the JPA `EntityManagerFactory`/Agroal pool itself.

### Migrating JSON Data to PostgreSQL

`migrate-to-db.sh` (repo root) is a one-shot script that resets a target Postgres database and imports an existing `JOL_DATA` JSON directory into it — used both for local development against fixture data and for a real cutover of production/test data.

**Prerequisites:** `psql`, `jq`, `python3` on whatever machine runs the script (see below for running it inside a container instead, if the target host doesn't have them installed directly).

**Usage:**
```bash
./migrate-to-db.sh [--player-data-only] [DATA_DIR]
```
* `DATA_DIR` defaults to `~/data`.
* Connection is via the standard `PGHOST`/`PGPORT`/`PGDATABASE`/`PGUSER`/`PGPASSWORD` env vars (defaults: `localhost:5432/jol`, user `jol`, password `jol`).
* The script **drops and recreates** the target database, then re-applies every Flyway migration under `src/main/resources/db/migration/` before importing data — safe to re-run, but never point it at a database you want to keep.

**`--player-data-only`** restricts the import to tables owned by a single player — `player`/`player_role`, `player_activity`, `deck_info`/`deck_format`/`deck_content`, and `refresh_token`. Everything else (games, registrations, tournaments, global chat, past-game history) is skipped entirely. This is meant for a first test migration against a real data directory without dragging along live/in-progress game state — the summary printed at the end shows `0 rows` for every skipped table so you can confirm what did and didn't come across.

**Local development:**
```bash
docker compose -f local-docker-compose.yml up -d db   # starts a local Postgres, no Traefik/nginx
./load-test-fixtures.sh                                # shorthand for: ./migrate-to-db.sh src/test/resources/data
```

**Migrating a real (e.g. production or test) data directory**, when the target Postgres container doesn't have `psql`/`jq`/`python3` available on its host directly: run the script inside a throwaway container instead, with the real `JOL_DATA` volume mounted read-only and attached to the same Docker network as the Postgres container, so it can resolve it by service name:
```bash
docker run -d --name jol-migrate-tmp --network web -v <jol-data-volume>:/data:ro postgres:16 sleep infinity
docker cp migrate-to-db.sh jol-migrate-tmp:/migrate-to-db.sh
docker exec jol-migrate-tmp mkdir -p /src/main/resources/db/migration   # docker cp won't create parent dirs
docker cp src/main/resources/db/migration/. jol-migrate-tmp:/src/main/resources/db/migration/
docker exec jol-migrate-tmp bash -c "apt-get update -qq && apt-get install -y -qq jq python3"
docker exec -e PGHOST=<postgres-service-name> -e PGPASSWORD=<password> \
  jol-migrate-tmp bash /migrate-to-db.sh --player-data-only /data
docker rm -f jol-migrate-tmp
```
(`postgres:16` already bundles `psql`; the script's relative `MIGRATIONS` path means the copied files must land at `/src/main/resources/db/migration` alongside the script at `/migrate-to-db.sh`, matching the repo's own layout.) Stop whatever's currently reading/writing that data directory first, so the import is a consistent snapshot rather than racing live writes.

### Technology Stack
* JPA (Hibernate 7) / Flyway / Agroal / PostgreSQL - persistence layer, all Quarkus-managed, see [Storage Mechanics](#storage-mechanics) above
  * `net.deckserver.jpa.entity` - one `@Entity` per table, auto-discovered (no `persistence.xml`); `net.deckserver.jpa.repository` - one repository per entity/aggregate holding the actual JPQL/`EntityManager` calls, so services never touch `EntityManager` directly
  * `JpaFactory` - thin facade resolving the `EntityManagerFactory` Quarkus's own `quarkus-hibernate-orm` extension already built (via CDI) - `initialize()` for the real app, `initializeWithEmf()` for tests
* Quarkus REST (`jakarta.ws.rs`) - the REST API under `/jol/api/...`, the SPA's sole backend interface
  * `JolApplication` sets the `/api` application path (a plain `jakarta.ws.rs.core.Application` subclass - Quarkus REST auto-discovers `@Path` resources itself, no package-scanning config needed)
  * Resources: one per SPA page/concern, each returning that page's own bean directly with no shared envelope - `PageResource` (nav/global chat), `LobbyResource`, `DeckResource`/`DeckPageResource`, `GameActionResource`/`GameStateResource`, `AdminResource`/`AdminPageResource`, `TournamentResource`, `ProfileResource`, `PlayerResource`, `StatisticsResource`, `WatchResource`, `NotificationResource`, `ConfigResource`, `AuthResource`, `MainResource`
  * `SecurityFilter` - both a `ContainerRequestFilter` (rejects unauthenticated calls with 401, populates the JAX-RS `SecurityContext`) and a `ContainerResponseFilter` (attaches any cookies a silent refresh-token rotation needs to set - there's no live mutable response during the request-filter phase to write one onto directly)
  * `LegacyClientResource` returns 401 to the two calls a pre-React browser tab left open across a deploy still makes on its own, bouncing it to `/login`
* WebSocket (JSR-356, Quarkus/Undertow) - lightweight server push over `/ws/updates`, signals only - see [Frontend Architecture](#frontend-architecture) below
  * `JolWebSocketEndpoint` authenticates the handshake off the login cookie (via a `ServerEndpointConfig.Configurator`, not an `HttpSession` - there isn't one) and handles join/leave/ping frames
  * `WebSocketRegistry` tracks player -> socket(s) and game -> sockets mappings; clients re-fetch the relevant REST endpoint on receipt rather than getting full payloads over the socket
* Authentication - cookie-based, no server-side session store; see [Authentication](#authentication) below for the full request flow
* Jackson - JSON serialization throughout: REST request/response bodies, and the JSON/text blob columns described in [Database Schema](#database-schema) above
* jBCrypt - password hashing for `PlayerInfo.hash`
* Caffeine - in-memory caching, e.g. active-user summaries and hot `JolGame` lookups
* Vite / React / TypeScript - the entire client, see [Frontend Architecture](#frontend-architecture) below
  * `CardDatabaseBuilder.java` (test-scope) parses the VEKN-supplied crypt/library CSVs and generates the static JSON/HTML consumed by tooltips and modals, plus card images, served from `static.deckserver.net` (nginx) in prod and by `src/main/webui/serveCardAssets.ts` (Vite) in local dev
* Docker - runs the application server on deckserver.net, fronted by Traefik for TLS termination and routing. **Stale as of the Quarkus migration** - `docker-compose.yml`/`Dockerfile`s still describe building/running a Tomcat WAR, which this project hasn't produced since early in the migration; updating deployment for the Quarkus jar is outstanding work
  * `docker-compose.yml` - production config: `prod` (+ its own `prod-db` Postgres), `test`, and `static` (nginx, serving prebuilt card assets) containers
  * `local-docker-compose.yml` - dev-only, just a local Postgres `db` service

### Frontend Architecture
The entire client is `src/main/webui/` — a real Vite/TypeScript/React SPA with a client-side router (`react-router-dom`) and no JSPs left anywhere in the request path, served directly by Quarkus via the Quinoa extension. See `src/main/webui/README.md` for the frontend project's own structure, scripts, and testing setup; this section covers how it fits into the wider app.

1. **Routing.** `App.tsx` defines the routes directly with `react-router-dom` (`MainPage`/`LoginPage` load eagerly; every other page — `LobbyPage`, `GamePage`, `DeckPage`, `TournamentPage`, `AdminPage`, ... — is a separate lazily-loaded chunk, fetched on first navigation there). Server-side, Quinoa's own SPA routing (`quarkus.quinoa.enable-spa-routing=true` in `application.properties`) serves `index.html` for every one of those top-level paths — no Java class or rewrite-rule list deciding routes, and no auth check happens server-side at that layer at all.
2. **Auth gating is client-side.** `AuthGate` (in `App.tsx`) only renders the authenticated app shell once a `GET /jol/api/nav` call (`useNav`, backed by `SecurityFilter`) has actually confirmed a session — it doesn't trust the server to have gated the page already. A logged-out visitor sees `LoginPage`, which is deliberately mounted outside `AuthGate` so it can render before any session exists.
3. **Data fetching is TanStack Query, not a hand-rolled cache.** Every page's `src/main/webui/src/api/client.ts` call (a thin `fetch` wrapper — see [Authentication](#authentication) below for its 401 handling) is wrapped in a `useQuery`/`useMutation` hook; TanStack Query owns request de-duplication, caching, and refetch-on-focus. There's no server-response dispatch table and no shared page envelope — each REST resource returns exactly the bean the page that calls it needs (see `src/main/webui/src/api/types.ts` for the hand-maintained mirrors of those Java bean shapes; update both sides together when one changes).
4. **Staying current: WebSocket push invalidates a query key, nothing more.** `/ws/updates` (JSR-356) pushes lightweight envelopes — `{"type":"invalidate","key":[...]}` — never the actual payload. `WebSocketRegistry.notifyInvalidate(key)` is what any mutating endpoint calls after a save; client-side, `ws/useQueryInvalidation.ts` (mounted once, near the app root) bridges that straight into `queryClient.invalidateQueries({queryKey: key})` — every page's `useQuery` for that same key just refetches on its own, no per-page socket handling needed. A game page additionally calls `useGameSocket(gameId)` to join/leave that game's server-side "room" (`WebSocketRegistry.joinGame`/`leaveGame`) so its tab is one of the sessions a game-scoped `notifyGame`/`notifyInvalidate(["game", gameId])` call reaches — the hook itself does nothing with the push, since the same generic invalidation bridge already handles it. If the socket drops, `socket.ts` reconnects and every currently-joined room is rejoined (`onOpen` handlers), and a `visibilitychange` listener forces a reconnect on a backgrounded tab coming back to the front (sockets can die silently while backgrounded, with no `onclose` firing until the OS actually tears the connection down).
5. **Self-triggered refetch race.** Because a state-saving action's own response already carries the fresh state, but that same action's server-side `notifyInvalidate` call *also* pings the actor's own session, a query refetch triggered by that self-ping can race the mutation's own response handling within the same round trip. State that must reflect only the mutating call's own response (not a subsequent full-snapshot refetch) is kept in local component state instead of read off the query result for exactly this reason — see `CommandForm.tsx`'s `status` field for a concrete case (server-side command-validation errors).
6. **Errors and auth expiry.** `api/client.ts`'s `request()` redirects straight to `/jol/login` on any 401 — no client-side retry, because there's nothing to retry: `AuthService.authenticate` (see [Authentication](#authentication) below) already tries a silent refresh-token-backed reissue *within that same request*, so a 401 only ever reaches the client once the refresh token itself is no longer valid either. Any other failure surfaces via `useConnectivity`'s "reconnecting" banner (`ReconnectingOverlay`).

**Mental model for a new dev:** there's no bespoke client-side cache or dispatch table to learn — TanStack Query is the state store, keyed the same way on both ends (`['game', gameId]`, etc.), and the WebSocket's only job is telling the query cache "this key is stale, go refetch it." Adding a new piece of server-pushed state means calling `notifyInvalidate` with the right key server-side and having *some* component already `useQuery`-ing that key client-side — no new dispatch wiring required on either side.

### Authentication
Cookie-based, with no server-side session store — replaces an earlier `HttpSession`-based design that didn't survive app restarts or support "remember me."

**Two cookies, two lifetimes:**
* **`jol_at`** (access token) — a signed JWT, 15 minute expiry, `HttpOnly`+`Secure`. This is the *only* thing any request actually trusts: `SecurityFilter` (REST) and the WebSocket handshake (`JolWebSocketEndpoint.Configurator`) both validate it directly by signature, with no lookup required. That's what makes auth restart-proof — the signing key (`AuthService`, read from the `JWT_SECRET_FILE` env var, generated and written there on first boot if missing) is the only piece of state that has to survive, not a live session table.
* **`jol_rt`** (refresh token) — an opaque, long-lived, per-device token, `HttpOnly`+`Secure`, tracked server-side by `RefreshTokenService` (the `refresh_token` table — see [Database Schema](#database-schema) above). Used only to silently mint a new `jol_at` once it expires — the user never sees a re-login prompt as long as `jol_rt` is still valid.

**Token shape and rotation.** A `jol_rt` cookie value is `{id}.{secret}` — the `id` gives O(1) lookup of the stored row, the `secret` is what's hashed (`SHA-256`) and compared. On every successful use, the secret half is rotated (new random secret, same `id`, new hash stored) and the token's expiry slides forward — up to a 90-day absolute cap from original creation, regardless of how often it's refreshed. If a presented secret doesn't match the currently-stored hash for its `id`, that means an *already-rotated-away* token was replayed (a strong signal of token theft), so the row is revoked outright rather than silently rejected.

**Multi-device.** Each login (or "remember me" checkbox) creates its own `refresh_token` row, tagged with a truncated `User-Agent` as a device label. Logging out revokes only that row (`AuthService.clearAuth`); other devices are untouched. `RefreshTokenService` already has the methods for a device-management UI (`list`, `revoke(playerName, id)`, `revokeAll(playerName)`) but nothing in `net.deckserver.rest` currently calls them — there's no `/devices` endpoint wired up yet, so this is service-layer capability without a REST surface so far.

**Request flow.** `AuthService.authenticate(HttpHeaders)` is the single entry point every auth check goes through, returning an `AuthResult` (username, plus any cookies the caller needs to attach — there's no live mutable response at this layer to write onto directly under Quarkus REST, unlike the old Jersey/Servlet design):
1. Is `jol_at` present and does it verify? → done, return the username, no I/O.
2. Otherwise, is `jol_rt` present and does it validate against `RefreshTokenService`? → mint a new `jol_at`, rotate `jol_rt`, return the username plus both new cookies for the caller to attach. This is what makes an expired access token invisible to the user on a normal page load or REST call — the client never sees a 401 for this case at all, since the very request that would have failed instead succeeds with fresh cookies attached.
3. Otherwise → unauthenticated (401 for REST, handshake rejection for WebSocket).

Because step 2 needs to attach response cookies, it only happens where a response exists to attach them to (`SecurityFilter`'s response-filter phase, or `AuthResource`'s own handlers). The WebSocket handshake only does step 1 — if `jol_at` has already expired by the time a socket connects, the connection is rejected outright rather than silently refreshed; in practice this rarely bites because a fresh page load always mints a new `jol_at` first.

**Client side** (`src/main/webui/src/api/client.ts`). There's no client-side retry-on-401 logic, because there's nothing to retry: per the request flow above, a 401 only ever reaches the browser once the refresh token itself has *also* failed, at which point `client.ts` just redirects to `/jol/login` — see [Frontend Architecture](#frontend-architecture) above. `POST /jol/api/auth/refresh` (`AuthResource`, exempted from `SecurityFilter`'s auth check) runs the same `authenticate()` flow standalone for a forced refresh — currently unused from `src/main/webui/src`, left over from an earlier client that called it explicitly on a 401 before every other endpoint's own request started transparently refreshing via step 2 above.

**Notable non-goal:** logging out doesn't retroactively invalidate an already-issued `jol_at` — it only stops that device from getting a new one. A stolen/leaked access token remains valid for up to its remaining 15-minute window even after logout. This is the standard access/refresh-token trade-off, not an oversight.

**Password storage** is unrelated to the above and unchanged: `PlayerService.authenticate` checks a jBCrypt hash (`PlayerInfo.hash`, cost factor 13) — no tokens or cookies involved in verifying the password itself, only in what happens after it succeeds.

### Game State Model: JolGame / GameModel / GameView / GameSnapshot
There used to be a fifth class here (`PlayerModel`, one instance per logged-in player tracking "which view/game is this player currently looking at") — it's gone, because that's now just React Router state plus whatever the current page's own `useQuery` calls ask for; nothing server-side needs to remember a player's "current view" between requests any more.

* **`JolGame`** (`net.deckserver.game.model.JolGame`) is the actual game state — a record wrapping `GameData`, with all the game-rule mutation methods (`submit` a command runs through `DoCommand`, `newTurn`, region/card manipulation, ...). Loaded from and saved back to Postgres by `GameService` (write-through per save, see [Storage Mechanics](#storage-mechanics) above) — this class itself has no persistence awareness.

* **`GameModel`** (`net.deckserver.game.model.GameModel`) is the per-active-game orchestrator — one instance per game name, held in `JolAdmin.gmap` (`Map<String, GameModel>`, created lazily by `JolAdmin.getGameModel`). It's what `GameStateResource` actually calls: `submit(player, phase, command, chat, ping, clientId)` authorizes the caller (player-in-game, or a judge), runs the command against the current `JolGame` (fetched fresh from `GameService` each call — no caching at this layer), and persists via `JolAdmin.saveGameState`, which write-throughs to Postgres and calls `WebSocketRegistry.notifyGame(gameId, excludeClientId)`.

* **`GameView`** (`net.deckserver.game.model.GameView`) is now just a small per-viewer region-collapse cache (`toggleCollapsed`/`isCollapsed`), seeded with the same "collapse a torpored/ousted player's non-essential regions" defaults the client (`PlayerBoard.tsx`'s `ALWAYS_COLLAPSED`) now computes for itself instead. `GameModel` still holds a `Map<String, GameView> views` and `getView(player)`/`resetView(player)` methods to manage it (the latter still called on player-replace, for cache hygiene), but nothing currently calls `getView` at all — the client owns this UI state entirely now, and this whole path is vestigial server-side state left over from before it did. It used to also carry dirty-flag tracking and a JSP-rendered HTML fragment cache for the old client — both were removed once nothing but that removed rendering path read them.

* **`GameSnapshot`** (`net.deckserver.rest.bean.GameSnapshot`), built by `GameSnapshotFactory.build(gameModel, viewer, status)`, is the flat JSON DTO actually sent to the browser — from scratch, on every call, directly off the current `JolGame`. There's no dirty-flag tracking or per-field skip logic any more: the factory is stateless and safe to call from any number of concurrent requests, and the "only send what changed" optimization the old dirty-flag system existed for is now just "the client only refetches when a WebSocket push tells it its query key is stale" (see [Frontend Architecture](#frontend-architecture) above) — the expense that optimization used to guard against (rendering JSP fragments) doesn't exist on this path at all any more.

**Full round trip for one game action:**
1. Client: `GamePage` calls `POST /jol/api/game/{id}/view/submit` (via `api/client.ts`) with the phase/command/chat/ping to apply.
2. `GameStateResource.submit` resolves the `GameModel` for that game id and calls `game.submit(player, ..., clientId())` — `clientId()` is this tab's `X-Client-Id` header, threaded through so the acting tab can be excluded from its own push (see next step).
3. `GameModel.submit` authorizes the caller, runs the command against `JolGame`, and (if anything changed) calls `JolAdmin.saveGameState(game, silent, excludeClientId)` → `GameService.saveGame` (write-through to Postgres) → `WebSocketRegistry.notifyGame(gameId, excludeClientId)`.
4. The acting tab's own response already carries the fresh `GameSnapshot` (built synchronously by `GameSnapshotFactory` and returned from the same call) — it doesn't need the WebSocket push at all, which is why step 3 excludes it.
5. Every *other* tab watching this game (joined via `useGameSocket`, see [Frontend Architecture](#frontend-architecture) above) receives the `{"type":"invalidate","key":["game", gameId]}` push and refetches `GET /jol/api/game/{id}/view` on its own — same `GameSnapshotFactory` path, just entered from a different request, for that viewer.

So: one game mutation, one `JolGame` update in Postgres, one broadcast — but no per-viewer server-side state to keep in sync any more; each viewer's next fetch just rebuilds its own `GameSnapshot` fresh, which is safe precisely because that build is stateless.
