# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build the runnable Quarkus jar (target/quarkus-app/quarkus-run.jar)
./mvnw clean package

# Run locally — Quarkus dev mode, app served at /jol. Needs Docker: with no
# JDBC URL configured under %dev, Quarkus Dev Services boots a throwaway
# Postgres container and Flyway loads db/migration + db/devseed (Player1..5,
# password "password"; Player1 is admin). Quinoa (see quarkus.quinoa.* in
# application.properties) starts the frontend's own `npm run dev` as a
# subprocess and proxies to it automatically.
ENABLE_CAPTCHA=false ./mvnw quarkus:dev

# Run a dev instance against a real / production-shaped Postgres instead
# (does NOT migrate or clean it — schema must already be at head):
JOL_DB_URL=jdbc:postgresql://host/db JOL_DB_USER=… JOL_DB_PASSWORD=… \
  ./mvnw quarkus:dev -Dquarkus.profile=prodlike

# Run all tests (excludes "Builder" group by default). Pure unit tests need
# nothing. The JPA tier (net.deckserver.jpa.repository.*) uses a
# Testcontainers Postgres running the real Flyway migrations — needs Docker;
# it self-skips (not fails) when Docker is unavailable. See TESTING.md.
./mvnw test

# Run a single test class
./mvnw test -Dtest=PlayerRepositoryTest

# migrate-to-db.sh / load-test-fixtures.sh are for seeding a real Postgres
# from a production JSON snapshot only — not used by tests or `quarkus:dev`.

# Frontend: type-check + build, lint, unit/component tests (from src/main/webui/)
npm run build
npm run lint
npm run test

# Frontend: end-to-end tests (Playwright) — starts one quarkus:dev process
# via its webServer config; see src/main/webui/README.md's Testing section
npm run test:e2e
```

See `TESTING.md` for the test structure. In short: pure-logic tests (`*Test`, no DB) run anywhere; the JPA tier (`net.deckserver.jpa.repository.*`, `@ExtendWith(PostgresJpaExtension.class)`) runs against a Testcontainers Postgres built by the real Flyway migrations plus the `src/main/resources/db/testseed` fixture. New `@Entity` classes must be added to `src/test/resources/META-INF/persistence.xml`'s single `jol-test-pu` unit (plain Jakarta Persistence bootstrap has no entity scanning; the main persistence unit still auto-discovers). The service-level test suite that booted H2 via the old `JolServiceExtension`/`JolFixtureLoader` was removed pending a rework — see TESTING.md's "Deferred" list.

`quarkus:dev` serves HTTPS on `https://localhost:8443` (self-signed cert — see `application.properties`'s `%dev.quarkus.http.ssl-port` block for how to regenerate `src/main/resources/dev-keystore.p12` if needed), since `AuthService`'s cookies are unconditionally `Secure`. Production runs plain HTTP behind Traefik's own TLS termination (see `docker-compose.yml`) — the dev-only HTTPS config doesn't apply there.

The `Builder` tag is excluded from the default test run — these are `CardDatabaseBuilder` tests that regenerate the static card **image / HTML tooltip** assets under `static/` (served from `static.deckserver.net` in prod). Card *data* is no longer a build artifact: `net.deckserver.game.cards.CardRegistry` parses the VEKN CSVs (`csv/core/vtescrypt.csv` / `vteslib.csv`, dir overridable via `jol.card.dir`) directly at boot into an immutable in-memory index, hot-reloadable via `POST /jol/api/admin/cards/reload`. The old `CardService` + `CardSummary` + CloudFront `cards.json` path is gone.

## Key Environment Variables

| Variable | Purpose |
|---|---|
| `JOL_DB_URL` | JDBC URL. Consulted under `%prod` / `%prodlike` only (default `jdbc:postgresql://localhost:5432/jol`). Under `%dev` it is unset ⇒ Dev Services; pass `-Dquarkus.datasource.jdbc.url=…` to override. |
| `JOL_DB_USER` / `JOL_DB_PASSWORD` | Database credentials (default user `jol`). Ignored when Dev Services provides the container. |
| `JOL_DB_POOL_SIZE` | Agroal (Quarkus's connection pool) max pool size (default 10) |
| `jol.card.dir` | Directory `CardRegistry` reads `vtescrypt.csv` / `vteslib.csv` (and optional `*_playtest.csv`) from at boot / on `POST /jol/api/admin/cards/reload` (default `csv/core`). MicroProfile Config property — set via `-Djol.card.dir=…` or `JOL_CARD_DIR` env. |
| `ENABLE_TEST_MODE` | Skips the startup hook (`JpaStartup`) that calls `JpaFactory.initialize()` and makes every service's write-through methods (`PersistedService.jpaWrite`/`jpaWriteThenMutate`/`jpaWriteWithRollback`) a no-op that only updates in-memory state. Still set by the surviving pure-logic tests (`AuthServiceTest`, `TokenServiceTest`, `ParserServiceTest`) via `@SetEnvironmentVariable` so they never touch a database. The JPA tier does **not** set it — those tests exercise the real repositories against Postgres. |
| `ENABLE_CAPTCHA` | Set to `false` for local dev |
| `JOL_RECAPTCHA_KEY` / `JOL_RECAPTCHA_SECRET` | reCAPTCHA credentials |
| `DISCORD_BOT_TOKEN` / `DISCORD_PING_CHANNEL_ID` | Discord integration |
| `VAPID_PUBLIC_KEY` | Web push (VAPID) public key, fetched via `GET /jol/api/config` (`ConfigResource`) and used client-side by `src/main/webui/src/push/pushNotifications.ts` for `pushManager.subscribe()`. Set via `.keys` (see `docker-compose.yml`'s `env_file`). |
| `VAPID_KEY_FILE` | Path to the VAPID private key PEM file. `NotificationService` reads it at startup; web push is silently disabled (not a startup failure) when unset or unreadable. In `docker-compose.yml` this points at `/data/vapid_private.pem` on the persisted `prod-data` volume — that file must be copied there manually (e.g. from `notifications/vapid_private.pem`) for any environment that needs to send pushes. |
| `JWT_PRIVATE_KEY_FILE` / `JWT_PUBLIC_KEY_FILE` | Paths to the RS256 access-token keypair (PEM: PKCS#8 private, X.509 public). `net.deckserver.services.TokenService` signs/verifies the `jol_at` cookie with these — it's a plain jose4j JWT so it works in service tests that boot no Quarkus runtime. Unset ⇒ falls back to the committed non-secret dev keypair `src/main/resources/jwt/jwt-dev-*.pem` (so tests and a fresh checkout need no setup). Generate a prod pair with `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem && openssl pkey -in jwt-private.pem -pubout -out jwt-public.pem` and mount both on the persisted volume (same as `vapid_private.pem`). |
| `JWT_PUBLIC_KEY_LOCATION` | Feeds `mp.jwt.verify.publickey.location` — the key Quarkus's own `quarkus-smallrye-jwt` mechanism verifies with (used to build the `SecurityIdentity` behind `@RolesAllowed`; roles come from the token's `groups` claim). Must resolve to the *same* public key as `JWT_PUBLIC_KEY_FILE`; use a `file:` URL in prod (`file:/data/jwt-public.pem`). Defaults to the classpath dev key. |
| `TYPE` | Visual env label (`dev`, `prod`, etc.) |
| `BASE_URL` | Origin card images/HTML/JSON tooltips are fetched from — exposed via `GET /jol/api/config` (`ConfigResource`), read by the React app's `getBaseUrl()` (`src/main/webui/src/api/config.ts`). Only matters for a production build: in `npm run dev`, `getBaseUrl()` unconditionally returns a relative path instead, since Vite's own `serveCardAssets.ts` already serves the local `static/` directory directly — this env var is never consulted in dev at all. Defaults to `https://static.dev.deckserver.net`, which never resolves to anything reachable outside prod (it was only ever an `/etc/hosts` entry pointing at a now-removed local nginx container). Unset in prod (`docker-compose.yml`), where the real default is correct. |

## Architecture Overview

This is a **Vampire: The Eternal Struggle (VTES) online card game server** (deckserver.net), built on **Quarkus** and packaged as a runnable jar (`target/quarkus-app/quarkus-run.jar`) — migrated off a Tomcat 9 WAR/Jersey stack, a migration that surfaced and fixed several non-obvious bugs (a CDI proxy field-access bug, a WebSocket identity-confusion bug — both documented at the relevant classes below) worth knowing before assuming this description is exhaustive. State is persisted in **PostgreSQL** via JPA (Hibernate 7 + Flyway + Agroal, all Quarkus-managed): each service holds an authoritative in-memory copy and writes through to the DB on mutation (single-node assumption — see `PersistedService`'s javadoc). `migrate-to-db.sh` (repo root) resets the DB and imports the legacy `JOL_DATA` JSON files — useful for seeding a fresh Postgres from an old JSON snapshot, but the running app itself never reads those files.

### Request Flow

1. Browser loads the React SPA (`src/main/webui/`, served directly by Quarkus via the Quinoa extension — see `quarkus.quinoa.*` in `application.properties`) — Quinoa's SPA routing serves `index.html` for every client-owned route (`/main`, `/lobby`, `/game/*`, etc.); the SPA itself decides client-side whether to show the login page or the authenticated app (`AuthGate` in `src/main/webui/src/App.tsx`), backed by the auth-protected `GET /nav` call (see `CookieJwtAuthMechanism` + `quarkus.http.auth.permission.*`)
2. The SPA's `api/client.ts` (a thin fetch wrapper) calls `/jol/api/...` directly — there's no hand-written JS shim between them
3. Quarkus REST resources (`net.deckserver.rest`, `jakarta.ws.rs`) handle each endpoint and delegate to **`JolAdmin`** or services
4. `JolAdmin` manages in-memory `GameModel` / `PlayerModel` maps and routes to **`JolGame`** or **`DoCommand`**
5. Services write through to PostgreSQL immediately (`PersistedService.jpaWrite`/`jpaWriteThenMutate`/`jpaWriteWithRollback`), or batch-flush on a schedule for high-frequency, low-value writes (`PlayerActivityService`/`PlayerGameActivityService` — see their class comments for why those two specifically stay batched rather than write-through)
6. Server-push notifications are sent over WebSocket (`/ws/updates`, Quarkus/Undertow JSR-356) via `WebSocketRegistry`; the SPA treats each push as a signal to re-fetch (`ws/useGameSocket.ts`, `ws/useQueryInvalidation.ts`), not as a payload carrier itself

### Package Map

- **`net.deckserver`** — `JolAdmin` (singleton orchestrator); `Recaptcha`
- **`net.deckserver.services`** — CDI `@Singleton` beans (`@Startup`-eagerly-created — see below for why `@Singleton` specifically, not the more obvious `@ApplicationScoped`), each still exposing a static-method API so callers are unaffected by that
  - `PersistedService` — abstract base: `@PostConstruct`/`@Observes ShutdownEvent` lifecycle (not a `ServletContextListener` — there's no servlet container anymore), test-mode bypass, and the `jpaRead`/`jpaWrite`/`jpaWriteThenMutate`/`jpaWriteWithRollback`/`jpaWriteAlways` helpers every service's mutations go through. Its `resolve(Class, Supplier)` is what every subclass's `instance()` accessor calls — deliberately `@Singleton`, not `@ApplicationScoped`: the latter is a CDI *normal scope*, meaning `Instance.get()` returns a client proxy that only intercepts method calls, not field reads, which silently broke every `instance().someField` access in this codebase (a real, reproduced bug, not a theoretical concern)
  - `GameService`, `PlayerService`, `DeckService`, `ChatService`, etc. — most hold an authoritative in-memory copy backed by write-through JPA; a few (`DeckService`, `HistoryService`) have no cache at all and read straight from JPA on every call. Card reference data is **not** here — it's `net.deckserver.game.cards.CardRegistry` (CSV-backed, no DB)
  - `CardSearchService` / `DeckImportService` — deck-editor autocomplete, batch detail lookup and KRCG/JOL paste import, all projected from `CardRegistry`
  - `ParserService` — chat/log rendering. `parseGlobalChat`/`parseGameChat` rewrite the markup a player types into plain-text tokens the React client resolves to components: `[Card Name]`→`[card:<id>:<name>]` (+`:adv`) via `CardRegistry`, `[pot]`→`[disc:pot]`, `(D)`→`[d]`, `{x}`→`[style:x]`; emoji shortcodes expand to Unicode and the HTML entities `sanitizeText` adds are decoded back (output is plain text, not HTML). `parseSymbols` still emits `<span>` icon HTML — its only caller is `CardDatabaseBuilder` (static tooltip assets), not chat. Client side: `src/main/webui/src/utils/parseMessageTokens.ts` + `components/MessageContent.tsx`. `migrate-to-db.sh` §13b rewrites the legacy stored HTML (`<a class='card-name'>`, `<span class='icon …'>`) to the same tokens on import.
- **`net.deckserver.jpa`** — the JPA layer the services above delegate to
  - `JpaFactory` — thin facade: `initialize()` resolves the `EntityManagerFactory` Quarkus's own `quarkus-hibernate-orm` extension already built (via Arc/CDI, not a hand-rolled Hikari+Flyway bootstrap), `initializeWithEmf()` for tests (see `JolServiceExtension`, which builds its own H2 `EntityManagerFactory` entirely independent of Quarkus's runtime)
  - `JpaStartup` — `@Observes @Priority(1) StartupEvent`; calls `JpaFactory.initialize()` before any service's `@PostConstruct`-triggered `load()` can run (has to be a low explicit priority — Quarkus's `@Startup` beans fire at priority 2500 by default, and without an explicit lower one here their relative order is undefined)
  - `entity/` — one `@Entity` per table (`PlayerEntity`, `GameInfoEntity`, `GameStateEntity`, `RegistrationEntity`, `DeckInfoEntity`/`DeckContentEntity`, `TournamentEntity`/`TournamentRegistrationEntity`, `GameChatEntity`, `GlobalChatEntity`, `GameHistoryEntity`, `GameSnapshotEntity`, `PlayerActivityEntity`/`GameActivityEntity`, `SiteNotesEntity`, `SubscriptionEntity`, `RefreshTokenEntity`) — auto-discovered by Quarkus's Hibernate ORM extension, no `persistence.xml`/explicit `<class>` list for the main persistence unit (the *test* one under `src/test/resources/META-INF/persistence.xml` still needs new entities registered manually, per its own note above)
  - `repository/` — one per entity/aggregate, holding the actual JPQL/`EntityManager` calls; services never touch `EntityManager` directly except via these
- **`net.deckserver.storage.json`** — these classes are the domain/API model now, not a file format — every one is still what services hold in memory and what REST beans wrap, just no longer what gets serialized to disk
  - `system/` — `GameInfo`, `PlayerInfo`, `DeckInfo`, `GameHistory`, `RefreshTokenInfo`, tournament classes
  - `game/` — in-game state: `GameData`, `PlayerData`, `RegionData`, `CardData`, `TurnData`
  - `deck/` — deck structure: `Deck` / `Crypt` / `Library` (the in-memory model everything works with), `DeckParser` (text → `Deck`), `DeckNormalizer` (any historical `deck_content` string → `Deck`), `KrcgV5Mapper` (`Deck` ⇄ **KRCG v5 JSON**, the single stored form in `deck_content` / `registration.deck_content` / `tournament_registration.deck_content`). `DeckService.migrateStorageToV5()` converts every row on boot; a `LEGACY` deck whose raw text doesn't parse cleanly keeps its verbatim text (editor shows a raw-text mode) until the owner fixes the unresolved card names
  - `cards/` — `SecuredCardLoader` only (CloudFront signed-cookie minting for the `/secured/*` playtest image assets, used by `AuthResource` on login). Card *data* lives in `net.deckserver.game.cards` now.
- **`net.deckserver.game`**
  - `cards/` — `CardRegistry` (the single card-database façade — boot-time CSV parse into an immutable `Index`, `bootstrap()`/`reload()`/`load(Path)`/`status()`, id + name resolution, `resolveExact`/`resolveNormalized`/`resolveFuzzy`), the canonical sealed `Card` → `CryptCard` / `LibraryCard` records, `PlayMode`/`PlayTarget` (play-card-modal data), `CardRef` (chat card-links), `RegistryStatus`. `importer/` holds `PlayModeParser` (derives library play modes from card text — ported from the old test-scope `LibraryImporter`) and `CryptMetadata` (intrinsic sect / votes / unique / infernal). `CardRegistryStartup` forces the load at `@Priority(1)` `StartupEvent`.
  - `enums/` — domain enums: `RegionType`, `CardType`, `Clan`, `Phase`, `GameStatus`, etc.
  - `validators/` — deck validation: `StandardDeckValidator`, `V5DeckValidator`, `DuelDeckValidator`, `PlayTestValidator` all extend `AbstractDeckValidator`; use `ValidatorFactory`
  - `model/` — core game logic
    - `JolGame` — record holding game id + `GameData`; all game state mutation methods
    - `DoCommand` — record; parses and executes player text commands (e.g. `burn library 1`)
    - `GameModel` — in-memory per-game view; held in `JolAdmin.gmap`
    - `GameView` — per-player region collapse/expand state (client now owns most of what this used to track — see `PlayerBoard.tsx`'s comment on `ALWAYS_COLLAPSED`)
    - `CommandParser` — tokenises command strings
- **`net.deckserver.rest`** — Quarkus REST (`jakarta.ws.rs`) REST API (`/jol/api/...`); the SPA's sole backend interface
  - `bean/` — JSON response objects returned to the frontend (`GameSnapshot`, `NavBean`, `DeckEdit`, etc.), built directly by `net.deckserver.rest` resources and factories (e.g. `GameSnapshotFactory`)
  - `BaseResource` — base class: injects `SecurityContext` + `HttpHeaders` (not a Servlet `HttpServletRequest` — there's no servlet container)
  - `PageResource` — `GET /nav` (read via `src/main/webui/src/auth/useNav.ts` — a react-query-cache-backed hook, no context — for the authenticated shell's nav state), `POST /chat`
  - `LobbyResource` — `/lobby/player/games` CRUD (create/start/close), deck registration, invites
  - `GameActionResource` / `GameStateResource` — the former holds what's still shared with the old DWR-era surface (deck/players/turns/history/notes); the latter is the SPA-only `/game/{id}/view` + `view/submit` + `view/end-turn` used by `GamePage.tsx`
  - `DeckResource` / `DeckPageResource` — deck CRUD, validation, and the deck-editor page's combined view
  - `AuthResource` — login/register/logout; issues/clears cookies via `AuthService`, returning `List<NewCookie>` rather than mutating a live response object (there isn't one at this layer under Quarkus REST)
  - `ProfileResource`, `AdminResource`, `AdminPageResource`, `TournamentResource`, `MainResource`, `WatchResource`, `StatisticsResource`, `ConfigResource`, `NotificationResource` — one resource per SPA page/concern, each returning that page's own bean directly (see `src/main/webui/src/api/types.ts` for the mirrored shapes)
  - `CookieJwtAuthMechanism` (`net.deckserver.rest`) — the app's single Quarkus `HttpAuthenticationMechanism`. Turns the `jol_at`/`jol_rt` cookie pair into a `SecurityIdentity` whose roles are the access token's `groups` claim (see `TokenService`), so `@RolesAllowed` and the `quarkus.http.auth.permission.*` policies in `application.properties` work against it. The decision logic is still `AuthService.authenticate(Optional,Optional)` (shared with the WebSocket handshake); the mechanism only adapts it to the Vert.x SPI, runs it on a worker thread (it can hit the DB via `RefreshTokenService`), does the silent refresh-token rotation, and writes `Set-Cookie` straight onto the Vert.x response — plus the stale-subject guard (valid token, subject gone → clear cookies, stay anonymous → clean 401). Replaced `SecurityFilter` (deleted): its blanket "401 unless authenticated" is now the `authenticated` permission policy on `/jol/api/*`, with `permit` for `/jol/api/auth/*` + `/jol/api/config`. Auth is lazy (`quarkus.http.auth.proactive=false`). Role checks: `AdminResource`/`AdminPageResource` (class-level `@RolesAllowed("ADMIN")`) and `TournamentResource` (method-level `@RolesAllowed("TOURNAMENT_ADMIN")` on its admin ops); `LobbyResource` still calls `sc.isUserInRole(...)` inline (composite "owner-or-admin" logic an annotation can't express) — same `SecurityIdentity` underneath. Note these role names are flat, no hierarchy — `ADMIN` does **not** imply `TOURNAMENT_ADMIN`. A role change takes effect on the player's **next request**: `PlayerService.setRole` bumps a per-player `minTokenIssuedAt` (in-memory), and `AuthService.authenticate` treats any access token issued before that as stale — forcing a silent refresh-token rotation that re-reads live roles (session stays alive; no re-login).
- **`net.deckserver.ws`** — WebSocket push
  - `JolWebSocketEndpoint` — `@ServerEndpoint("/ws/updates")`; auth is cookie-based (`jol_at`, parsed straight off the raw `Cookie` handshake header by a `ServerEndpointConfig.Configurator` — there's no HTTP session to share); handles join/leave/ping frames. That `Configurator`'s `getUserProperties()` map is shared across *every* handshake for the endpoint, not fresh per connection — it must always be written (username on success, removed on failure) or a later unauthenticated handshake silently inherits a previous successful one's identity (a deterministic bug, reproduced with zero concurrency — not a race)
  - `WebSocketRegistry` — tracks player→session mapping; `notifyMain()` / `notifyGame(gameId)` push update signals to clients
- **`net.deckserver.jobs`** — background jobs: `GameCleanUp`, `PublicGameBuilder`, `TournamentJob`, `RegistrationReconciliation`. **Currently not scheduled by anything** — the old `ServletContextListener` that scheduled them via a raw `ScheduledExecutorService` was deleted as part of the Quarkus migration (nothing else replaced that responsibility yet); migrating them to Quarkus's `@Scheduled` is outstanding work, not a regression introduced silently
- **`net.deckserver.push`** — Web Push notification support

### Database Schema

Tables live in one Postgres database, versioned by Flyway migrations under `src/main/resources/db/migration/` (`V1__baseline.sql` onward — add a new `V<n>__description.sql` file for schema changes, never edit a merged one). Roughly: `player`/`player_role`/`player_activity`, `game`/`game_state`/`game_snapshot`/`game_chat`/`game_activity`/`game_history`, `deck_info`/`deck_content`, `registration`, `tournament`/`tournament_registration`, `global_chat`, `site_notes`, `subscription`, `refresh_token`. Most large free-form structures (game state, deck content, tournament rounds/rules) are stored as a JSON/text blob in a single column rather than fully normalized — the JPA entity's `toXxx()`/`from()` methods handle the (de)serialization against the matching `net.deckserver.storage.json.*` class.

`csv/core/` (VEKN's `vtescrypt.csv`/`vteslib.csv`) and the generated `static/` tooltip assets are unrelated to this schema — see `net.deckserver.game.cards.CardRegistry` (runtime card data) / `CardDatabaseBuilder` (image + HTML asset generation), both untouched by the JPA migration.

### Frontend + API Notes

The entire client is `src/main/webui/` — a Vite/TypeScript/React SPA, served by Quarkus via the Quinoa extension. Lives at Quinoa's own default `ui-dir` (no override in `application.properties` needed) — moved there from a repo-root `frontend/` directory partway through the Quarkus migration, once Quinoa itself was already proven working against the old location (the move touched `serveCardAssets.ts`'s `static/` path depth and `playwright.config.ts`). See `src/main/webui/README.md` for the frontend project's own structure, scripts, and testing setup — this section only covers how it fits into the wider app.

- New API methods go in `src/main/webui/src/api/client.ts` (or a page-specific API module, e.g. `pages/login/authApi.ts`) + the matching Quarkus REST resource. Add the response shape to `src/main/webui/src/api/types.ts`, kept as hand-written mirrors of the Java beans — update both sides together when a bean's shape changes.
- REST resources return the bean directly (e.g. `GameSnapshot`, `LobbyPageBean`) — no shared envelope; each SPA page fetches only what it needs from its own dedicated endpoint(s).
- WebSocket at `/ws/updates` carries lightweight push signals — the SPA re-fetches the relevant REST endpoint on receipt (`src/main/webui/src/ws/useGameSocket.ts`, `ws/useQueryInvalidation.ts`) rather than receiving full payloads over the socket. `stores/socket.ts` (the WS client singleton) connects same-origin (`wss://${window.location.host}/jol/ws/updates`) directly to Quarkus — this bypasses Quinoa's dev-mode Vite proxying entirely (Vite only proxies its own module graph/HMR), so it behaves identically in dev and prod. Because a state-saving action also notifies the actor's own session, any state derived only from a full-snapshot refresh (not the mutating request's own response) can be overwritten by that self-triggered refetch within the same round trip — see `CommandForm.tsx`'s `status` field for a concrete case (server-side command-validation errors are kept in local component state instead of read off the snapshot, for exactly this reason).
- Card **images and HTML tooltip fragments** for tooltips/modals are generated by `CardDatabaseBuilder` (test-scope) and served statically from `static.deckserver.net` (CloudFront/nginx in prod). Local dev serves the same `static/` directory directly via `src/main/webui/serveCardAssets.ts` (Vite) — no local nginx/Docker layer needed. Card **data** for the play-card modal (`modes` / `multiMode` / `doNotReplace` / `preamble` / `cost`) now rides on `CardSnapshot` itself — `GameSnapshotFactory` enriches only the cards in the viewer's own HAND / RESEARCH region — so the client no longer fetches a per-card JSON definition.
- Quinoa auto-discovers and forwards to the frontend's own `npm run dev` in dev mode, and serves its `vite build` output directly in prod — no more manual `target/react-dist` staging. `quarkus.quinoa.enable-spa-routing=true` (direct mode, not the default auto-detect — see its comment in `application.properties`) replaces the old Tomcat `RewriteValve`/`rewrite.config` rules outright.
- `sw.js` (the push-notification service worker), which `web.xml`'s default-servlet mapping used to serve from `src/main/webapp/`, now lives under `src/main/resources/META-INF/resources/` — Quarkus's own built-in static-resource serving (a plain `quarkus-vertx-http` feature, unrelated to Quinoa). The three Bootstrap/theme CSS files that used to sit alongside it were removed once the frontend finished migrating to Tailwind (see `src/main/webui/README.md`); the Tailwind entry `styles/tailwind.css` is bundled by Vite like the rest of the app CSS.

### Deployment

**Stale as of the Quarkus migration** — `docker-compose.yml`/`Dockerfile`s still describe building and running a Tomcat WAR, which this project hasn't produced since early in the migration. Updating deployment for the Quarkus jar is outstanding work. What's documented below describes the pre-migration setup and needs re-verifying:

- Docker: `docker-compose.yml` runs `prod` (+ its own `prod-db` Postgres), `test`, and `static` (nginx, serving prebuilt card assets from a named volume) containers in production, fronted by Traefik for TLS termination and routing. `local-docker-compose.yml` is dev-only and much smaller — just a local Postgres `db` service
- Session clustering: Redisson (Redis) Tomcat session manager — moot now (auth is stateless JWT-in-cookie, not `HttpSession`-based, and there's no Tomcat), but the docker-compose/Dockerfile wiring for it hasn't been removed yet
- AWS CloudFront SDK included for CDN invalidation