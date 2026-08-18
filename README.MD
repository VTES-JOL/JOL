# V:TES Online (deckserver.net)

Online client and game server for Vampire: The Eternal Struggle. Java WAR deployed on Tomcat 9. No database — all state is persisted as JSON (and, for a shrinking set of legacy games, XML) files under a `JOL_DATA` directory.

# Requirements
* Java 21 (Corretto recommended — Lombok's annotation processing breaks on newer JDKs)
* Maven 3+

## Java
Install Java 21 as per your operating system.

## Maven
### Build
`./mvnw clean package` (macOS/Linux) or `mvnw.cmd clean package` (Windows)

### Run
#### Environment Variables
* `JOL_DATA` ( required ) - points to the game data directory, example data is available at `src/test/resources/data`. Can also be set via the `jol.data` system property, which takes precedence.
* `ENABLE_TEST_MODE` - disables scheduled persistence; set automatically by `@SetEnvironmentVariable` in tests, default: `false`
* `ENABLE_CAPTCHA` - disable Cloudflare Turnstile for local development if required, default: `true`
* `JOL_RECAPTCHA_KEY` / `JOL_RECAPTCHA_SECRET` - Cloudflare Turnstile site key / secret key, required when captcha is enabled (named "recaptcha" for historical reasons — this is Turnstile, not Google reCAPTCHA)
* `DISCORD_BOT_TOKEN` / `DISCORD_PING_CHANNEL_ID` / `DISCORD_API_VERSION` - required for Discord integration - bot token, channel id to post pings, and Discord API version (e.g. `10`); there is no code-level default for any of the three
* `VAPID_PUBLIC_KEY` - required for web push notifications - VAPID public key embedded client-side in `notification.jsp`. The matching private key is **not** an env var — `NotificationService` reads it from `<JOL_DATA>/vapid_private.pem`
* `BASE_URL` / `KEY_ID` / `KEY_FILE` - required for CloudFront-signed secured card image URLs - distribution base URL, key pair id, and path to the CloudFront private key file, used by `SecuredCardLoader`
* `TYPE` - visual/environment label (`dev`, `test`, `prod`, ...), default: `dev` - also selects which `css/<TYPE>.css` stylesheet is loaded

Run the `tomcat9:run` goal after setting up the environment variables you need:
`JOL_DATA=src/test/resources/data ./mvnw tomcat9:run`

The app is served at `http://localhost:8080/jol`.

### Tests
`./mvnw test` runs the full suite (Cucumber BDD scenarios included) except the `Builder`-tagged group, which regenerates the static card JSON/HTML used by the nginx static server and isn't part of normal test runs.

`./mvnw test -Dtest=DoCommandTest` runs a single test class.

### Data Structure
Everything below lives under `JOL_DATA`.
* `games.json` - hashmap of Game Name -> `GameInfo.java`
* `pastGames.json` - hashmap of Timestamp -> `GameHistory.java`
* `players.json` - hashmap of Player Name -> `PlayerInfo.java`
* `chats.json` - list of global chat entries
* `decks.json` - hashmap of Player Name -> Deck Name -> `DeckInfo.java`
* `registrations.json` - hashmap of Game Name -> Player Name -> `RegistrationStatus.java`
* `tournaments.json` - hashmap of tournament data; per-tournament detail lives under `tournaments/<uuid>/`
* `game-timestamps.json` / `player-timestamps.json` - last-activity tracking, per game and per player
* `site-notes.md` - admin-editable site banner/notes shown to players
* `subscriptions.json` - web push subscription records
* `jwt_secret.key` - HMAC signing key for login access tokens, generated on first boot and reused thereafter — losing it forces every active access token to be re-issued via its refresh token (or a fresh login if that's also gone)
* `refreshTokens.json` - "remember me" refresh tokens, one row per logged-in device, keyed by player name
* `vapid_private.pem` - private half of the web push VAPID key pair (see `VAPID_PUBLIC_KEY` above)
* `decks/` - `*.json`, modern-format decks, ULID-named
* `games/<uuid>/` - `game.json` (state) and `history.json` (chat/action history) for current games, plus one `<deckId>.json` per registered deck. A small number of not-yet-migrated legacy games still carry `game.xml`/`actions.xml` instead (see `GameDataConversion`)

Card data (crypt/library text, images) is *not* read from `JOL_DATA` at runtime — `CardService` fetches a pre-built `cards.json` from CloudFront (`static.deckserver.net`), falling back to a local `static/secured/cards.json` if that's unreachable. That file is generated from VEKN's official `vtescrypt.csv`/`vteslib.csv` by the test-scope `CardDatabaseBuilder` (the `Builder`-tagged tests).

### Storage Mechanics
No database anywhere — every durable piece of state above is a file under `JOL_DATA`, and the pattern is the same for all of them:

1. Each `*Service` singleton (`PlayerService`, `GameService`, `RefreshTokenService`, ...) extends `PersistedService`, which loads its JSON file into an in-memory `Map` exactly once, in its private constructor.
2. All reads and mutations happen against that in-memory map — there's no per-request disk I/O on the hot path; `PlayerService.get(name)` is a `HashMap` lookup, not a file read.
3. Persistence is write-behind, not synchronous: `PersistedService` schedules a `persist()` flush on a fixed interval per service (`PlayerService`/`GameService` every 5 minutes, `TournamentService` every 10, etc.) via a daemon `ScheduledExecutorService`, backed by a `Cleaner`-registered fallback and an explicit flush from `JolApplicationInitializer.contextDestroyed()` on graceful shutdown. `ENABLE_TEST_MODE=true` skips scheduled persistence entirely so tests don't fight the filesystem.
4. **Consequence:** a hard crash (as opposed to a graceful shutdown) can lose up to one persistence interval's worth of writes — there's no write-ahead log or journal. This trades durability guarantees for operational simplicity, which is a reasonable call for a game server where the worst case is replaying a turn, not losing money.

**Games are the one exception with real substructure**, since a game is too much state for a single flat map entry: `games/<uuid>/game.json` holds the live `GameData`, `history.json` holds the chat/action log, and each registered deck gets its own `<deckId>.json` alongside them. `GameService` still follows the same load-into-memory-then-persist-on-schedule pattern underneath, just keyed by directory instead of by map entry.

**Legacy XML is a shrinking remnant, not the design:** a small number of pre-migration games were never converted from the old `game.xml`/`actions.xml` (JaxB) format to JSON. `GameDataConversion` (`net.deckserver.jobs`) reads those on the fly; all new games are JSON-only, and this code path should be treated as obsolete-in-progress rather than something to build new features against.

**Cards are the other exception** — crypt/library data isn't in `JOL_DATA` at all (see above); keeping it out of the per-instance data directory means it stays identical across every deployment without needing to be synced.

### Technology Stack
* JSP / Servlets - page entry points and login/session bootstrapping
  * `MainServlet.java` - maps to `/`, `/main.jsp`, `/lobby`, `/deck`, `/admin`, `/game/*`, `/tournament`, `/tournamentAdmin`, `/profile`, `/active`, `/watch`, `/super`
  * `LoginServlet.java` - maps to `/login`
  * `LogoutServlet.java` - maps to `/logout`
  * `RegisterServlet.java` - maps to `/register`
  * JSP templates - located under `/WEB-INF/jsps`, grouped by function (`lobby`, `game`, `decks`, `admin`, `tournament`, `tournament-admin`, `profile`, `watch`, `help`, `main`); each has its own `layout.jsp` loaded first, then subsequent fragments
  * `/WEB-INF/main.jsp` holds the references to the functional layout sections
* Jersey - JAX-RS implementation for the REST API under `/jol/api/...`, which fully replaces the DWR-based client the app used to run on
  * `JolApplication` configures the application path and package scanning (`net.deckserver.rest`)
  * Resources: `PageResource` (navigate/poll/chat), `LobbyResource`, `DeckResource`, `GameActionResource`, `GameResource`, `UserResource`, `PlayerResource`, `AdminResource`, `TournamentResource`, `NotificationResource`, `AuthResource`, `SystemResource`
  * `SecurityFilter` rejects unauthenticated calls with 401 and populates the JAX-RS `SecurityContext` (`Principal` / `isUserInRole`)
  * `src/main/webapp/js/ds.js` is the hand-written fetch-based REST client consumed by the JSPs and game JS — see [Frontend Architecture](#frontend-architecture) below for how it loads and updates pages
* WebSocket (JSR-356) - lightweight server push over `/ws/updates`
  * `JolWebSocketEndpoint` authenticates the handshake off the login cookie and handles join/leave/ping frames
  * `WebSocketRegistry` tracks player -> socket(s) and game -> sockets mappings; clients re-poll the REST API on receipt rather than getting full payloads over the socket
* Authentication - cookie-based, no server-side session store; see [Authentication](#authentication) below for the full request flow
* Jackson - JSON serialization library, used throughout for reading/writing all data files listed above
* JaxB - legacy XML serialization, retained only to read pre-migration `game.xml`/`actions.xml` files (`net.deckserver.game.jaxb`, `GameDataConversion`); all new games are stored as JSON
* jBCrypt - password hashing for `PlayerInfo.hash`
* Caffeine - in-memory caching, e.g. active-user summaries and hot `JolGame` lookups
* jQuery - DOM manipulation and event wiring in `ds.js` and the page JS
* Nginx - static file server hosted at `static.deckserver.net`
  * `CardDatabaseBuilder.java` (test-scope) parses the VEKN-supplied crypt/library CSVs and generates the static JSON/HTML consumed by tooltips and modals, plus card images
* Docker - runs the application server on deckserver.net, fronted by Traefik for TLS termination and routing
  * `docker-compose.yml` - production config (`prod`, `test`, and `static` nginx containers), each with its own persistent named volume for `JOL_DATA`
  * `local-docker-compose.yml` - local config for the static data and/or application server

### Frontend Architecture
There is no client-side router or virtual DOM — the browser loads one real page (`main.jsp`, which contains every panel: lobby, game, decks, admin, etc., toggled by CSS), and after that everything is driven by polling/pushed JSON that gets stitched into the existing DOM by jQuery. The whole cycle lives in `src/main/webapp/js/ds.js`.

1. **Initial load** — on `$(document).ready`, `ds.js` reads the current path (`/jol/game/<id>`, etc.) to figure out which view to start on, then calls `DS.init(target)`. This posts to `POST /jol/api/navigate` with `{target, init: true}` and, on response, calls `init(data)`, which applies the saved dark-mode preference, hands the payload to `processData`, and opens the WebSocket (`initWebSocket()`).

2. **`processData` is the entire dispatch mechanism.** Every response from `/navigate`, `/poll`, or any action call (`DS.submitForm`, `DS.createGame`, ...) is a flat JSON object whose *keys are function names*:
   ```js
   const processDataHandlers = {
       checkVersion, navigate, loadGame,
       callbackMain, callbackLobby, callbackShowDecks, callbackAdmin,
       callbackProfile, callbackTournament, callbackTournamentAdmin,
       callbackAllGames, showStatus, setPreferences, setEdgeColorPref,
   };
   function processData(a) {
       for (const key in a) { processDataHandlers[key]?.(a[key]); }
   }
   ```
   `processData` loops over the object's keys and calls the matching handler with that key's value — **the server response shape *is* the client dispatch contract**. Key names are produced server-side by `ViewCreator.getFunction()` (e.g. `GameCreator.getFunction()` returns `"loadGame"`, `MainCreator` returns `"callbackMain"`) and consumed client-side by a same-named function in `ds.js`. Adding a new panel means adding both a `ViewCreator` on the server and a matching function here — nothing enforces the pairing beyond the string matching, so a rename on one side silently breaks the other.

3. **Navigating.** Clicking a nav link calls `DS.navigate(target)` → `POST /navigate` with just `{target}` (no `init`). `PageResource.navigate` sets the player's current view (`PlayerModel.setView`/`enterGame`) server-side, then returns a fresh `UpdateFactory.getUpdate()` payload — same shape as the initial load, so it flows through `processData` identically. Browser back/forward (`popstate`) re-issues the same navigate call rather than doing any client-side history diffing.

4. **Staying current: WebSocket-first, polling fallback.** `/ws/updates` pushes lightweight signals only — `{"type":"game","id":...}` or `{"type":"main"}` — never the actual payload. On receipt, the client re-fetches: `DS.getState(game, false)` if it's a game update for the game currently open, or `DS.doPoll()` (→ `GET /poll`) otherwise. If the socket drops, `ds.js` falls back to a 5-second `setTimeout` polling loop (`errorhandler`) until it reconnects, and a `visibilitychange` listener forces a reconnect plus one catch-up round-trip whenever a backgrounded tab becomes visible again (sockets can die silently while backgrounded, with no `onclose` firing until the OS actually tears the connection down).

5. **Rendering a game specifically.** `loadGame(data)` (the `GameBean` payload — see [Game State Model](#game-state-model-playermodel--gamemodel--gameview--gamebean) below) doesn't re-render the whole board on every update. `data.state` and `data.hand` are pre-rendered HTML fragments (JSP-rendered server-side by `GameView`), swapped into the DOM wholesale only when the server says something actually changed. Chat lines (`data.turn`) get appended incrementally rather than replacing the log.

6. **Errors and auth expiry.** Any 401 that survives `ds.js`'s own silent-refresh retry (see [Authentication](#authentication) below) redirects straight to `/jol/login` (`errorhandler`). Any other failure shows a "reconnecting" banner and retries via polling.

**Mental model for a new dev:** the frontend has no state store of its own beyond a handful of module-level `let`s (`game`, `player`, `currentPage`, cached lobby/profile data for re-rendering). The server is the single source of truth; the client's job is "ask for an update, dump whatever comes back into the DOM via the function named by that key." This is simple to reason about and has no build step, but every UI change touches both a Java `ViewCreator`/bean and the matching jQuery handler by hand.

### Authentication
Cookie-based, with no server-side session store — replaces an earlier `HttpSession`-based design that didn't survive app restarts or support "remember me."

**Two cookies, two lifetimes:**
* **`jol_at`** (access token) — a signed JWT, 15 minute expiry, `HttpOnly`+`Secure`. This is the *only* thing any request actually trusts: `SecurityFilter` (REST), `MainServlet` (JSP pages), and the WebSocket handshake (`JolWebSocketEndpoint.Configurator`) all validate it directly by signature, with no lookup required. That's what makes auth restart-proof — the signing key (`AuthService`, persisted to `<JOL_DATA>/jwt_secret.key` on first boot) is the only piece of state that has to survive, not a live session table.
* **`jol_rt`** (refresh token) — an opaque, long-lived, per-device token, `HttpOnly`+`Secure`, tracked server-side by `RefreshTokenService` (`<JOL_DATA>/refreshTokens.json`). Used only to silently mint a new `jol_at` once it expires — the user never sees a re-login prompt as long as `jol_rt` is still valid.

**Token shape and rotation.** A `jol_rt` cookie value is `{id}.{secret}` — the `id` gives O(1) lookup of the stored row, the `secret` is what's hashed (`SHA-256`) and compared. On every successful use, the secret half is rotated (new random secret, same `id`, new hash stored) and the token's expiry slides forward — up to a 90-day absolute cap from original creation, regardless of how often it's refreshed. If a presented secret doesn't match the currently-stored hash for its `id`, that means an *already-rotated-away* token was replayed (a strong signal of token theft), so the row is revoked outright rather than silently rejected.

**Multi-device.** Each login (or "remember me" checkbox) creates its own `RefreshTokenService` row, tagged with a truncated `User-Agent` as a device label. Logging out revokes only that row (`AuthService.clearAuth`); other devices are untouched. `UserResource` exposes `/user/devices` (list), `/user/devices/{id}` (revoke one), and `/user/logout-all` (revoke everything) for the profile page.

**Request flow.** `AuthService.authenticate(req, resp)` is the single entry point every auth check goes through:
1. Is `jol_at` present and does it verify? → done, return the username, no I/O.
2. Otherwise, is `jol_rt` present and does it validate against `RefreshTokenService`? → mint a new `jol_at`, rotate `jol_rt`, set both cookies on the response, return the username. This is what makes an expired access token invisible to the user on a normal page load or REST call.
3. Otherwise → unauthenticated (401 for REST, redirect-to-login for JSP pages, handshake rejection for WebSocket).

Because step 2 needs to write response cookies, it only happens where a response is available (REST filter, JSP servlets). The WebSocket handshake only does step 1 — if `jol_at` has already expired by the time a socket connects, the connection is rejected outright rather than silently refreshed; in practice this rarely bites because a fresh page load always mints a new `jol_at` first.

**Client side (`ds.js`).** `apiCall` retries exactly once on a 401: it calls `POST /jol/api/auth/refresh` (`AuthResource`, deliberately exempted from `SecurityFilter`'s auth check since its whole purpose is to run *after* auth has expired), and if that succeeds, replays the original request. This is what lets a long-idle tab silently recover without the user noticing, as long as `jol_rt` hasn't also expired.

**Notable non-goal:** logging out doesn't retroactively invalidate an already-issued `jol_at` — it only stops that device from getting a new one. A stolen/leaked access token remains valid for up to its remaining 15-minute window even after logout. This is the standard access/refresh-token trade-off, not an oversight.

**Password storage** is unrelated to the above and unchanged: `PlayerService.authenticate` checks a jBCrypt hash (`PlayerInfo.hash`, cost factor 13) — no tokens or cookies involved in verifying the password itself, only in what happens after it succeeds.

### Game State Model: PlayerModel / GameModel / GameView / GameBean
This is the piece most likely to confuse a new developer, because there are four similarly-named classes involved and the caching/dirty-tracking is easy to miss on a first read.

* **`PlayerModel`** (`net.deckserver.dwr.model.PlayerModel`) is the *player's* piece of session-equivalent state — one instance per logged-in player, held in `JolAdmin.pmap` (`Map<String, PlayerModel>`, created lazily by `JolAdmin.getPlayerModel`). It knows almost nothing about game internals: just which `view` the player is currently on (`"main"`, `"game"`, `"lobby"`, ...) and, if `view == "game"`, which `game` (by name) they're looking at (`enterGame`/`getCurrentGame`). It also carries a few other player-scoped UI bits — the currently-loaded deck for the deck editor, the global chat read-position.

* **`GameModel`** (`net.deckserver.dwr.model.GameModel`) is the *game's* piece of state — one instance per active game, held in `JolAdmin.gmap`, wrapping the actual `JolGame`/`GameData`. It owns a `Map<String player, GameView> views` — **one `GameView` per player currently seated at or watching that game**, created lazily in `getView(player)`. All game mutation (`submit`, `endTurn`, notes updates, ...) happens here, and after each mutation `GameModel` walks its `views` map and flips dirty flags on every `GameView` (`doReload`/`reloadNotes`) so every connected player's next fetch knows what to re-render.

* **`GameView`** (`net.deckserver.dwr.model.GameView`) is the *per-player, per-game rendering cache and dirty-tracker* — this is the piece that ties `PlayerModel` to `GameModel`. It's not stored on `PlayerModel` itself; `PlayerModel` only remembers the game's *name*. Every fetch resolves it fresh: `GameCreator.createData(player)` reads `player.getCurrentGame()`, looks up the `GameModel` via `JolAdmin.getGameModel(name)`, and calls `game.getView(player.getPlayerName())` — which returns the *same* cached `GameView` instance across requests (keyed by player name in `GameModel.views`), or creates one on first access. `GameView` tracks five independent dirty flags (`stateChanged`, `phaseChanged`, `turnChanged`, `globalNotesChanged`, `privateNotesChanged`) plus per-region collapse/expand UI state (`collapsed`) and a local buffer of unsent chat lines (`chats`).

* **`GameBean`** (`net.deckserver.dwr.bean.GameBean`) is the flat, immutable JSON DTO actually sent to the browser — built fresh on every call by `GameView.create()`. This is where the dirty flags pay off: `create()` only does the expensive work (rendering `hand.jsp`/`state.jsp` to a string via `JspRenderer`, which requires a `RequestContext`) for fields whose flag is currently set — e.g. `hand` stays `null` unless `stateChanged` is true, so an update that only added a chat line doesn't re-render the whole board HTML. After building the bean, `clearAccess()` resets all the flags to `false` so the *next* call is a no-op unless something changes them again in between.

**Full round trip for one game action**, tying it back to Frontend Architecture above:
1. Client: `DS.submitForm(game, phase, command, chat, ping)` → `POST /jol/api/game/{name}/submit`.
2. `GameActionResource` → `GameModel.submit(player, ...)` runs the command (`DoCommand`), mutates `JolGame`, then calls `doReload(...)` to mark every seated player's `GameView` dirty.
3. The response to the *acting* player is built synchronously via the normal `update()` → `UpdateFactory.getUpdate()` → `GameCreator` → `GameView.create()` path, same as any other request, riding back on the HTTP response for that call.
4. Other players watching the same game don't get this HTTP response — they get a WebSocket `{"type":"game","id":...}` ping, which (per Frontend Architecture) triggers *their* client to call `DS.getState(game, false)`, which re-enters the exact same `GameCreator`/`GameView.create()` path from their own request, reading whichever dirty flags `doReload` set for *their* `GameView` instance.

So: one game mutation, one `JolGame` update, but N independently dirty-tracked `GameView`s (one per connected viewer) and N separately-triggered fetches to collect them — `GameModel` is the fan-out point, `GameView` is what makes each fan-out target's re-render minimal.
