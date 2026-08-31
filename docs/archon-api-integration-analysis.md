# Archon API Integration Analysis

> Notes on integrating JOL's tournament lifecycle with the Archon API
> (<https://api.archon.krcg.org/docs>). Source: full OpenAPI spec
> (`https://api.archon.krcg.org/openapi.json`) cross-referenced against JOL's
> tournament code (`TournamentDefinition`, `TournamentResource`,
> `TournamentService`, `RegistrationReconciliation`).
>
> Status: exploratory. Nothing here is built.

## How Archon's API is shaped

Two APIs, two hosts:

| | Public API (`api.archon.krcg.org`) | Member API (`archon.krcg.org`) |
|---|---|---|
| Auth | app token, `client_credentials`, 1 h, no refresh | OAuth **authorization-code + PKCE (S256)**, `event:run` scope, **consent per tournament**, 30-day rotating refresh token |
| Scope | read-only VTES corpus | **exactly one already-existing tournament**, read + write |

The entire write surface is **one endpoint**: `POST /api/tournaments/{uid}/action`
with a `type` discriminator. ~45 action types cover the whole lifecycle:
`OpenRegistration`, `CloseRegistration`, `CheckInAll`, `StartRound` (with or
without explicit `seating`), `SetScore`, `FinishRound`, `StartFinals`,
`SetToss`/`RandomToss`, `FinishFinals`, `FinishTournament`, plus
`AlterSeating`/`SeatPlayer`/`SwapSeats`, `Override`, `DropOut`, `UpsertDeck`, etc.
Every call returns the whole updated `Tournament` (write is also read). Plus
`POST /bulk-register` and `GET /stream` (SSE live feed).

## Integration points, by lifecycle stage

### Stage 0 — Tournament creation → hard gap

There is **no create-tournament endpoint** anywhere in the API, and the OAuth
consent URL *requires* `&tournament=<uid>`, so the tournament must exist in
Archon before JOL can even obtain a token for it.

- **Change:** the TO still creates the tournament shell in Archon's web UI. JOL
  stores the returned `uid` + `event_code` on `TournamentDefinition` (new fields
  `archonUid`, `archonEventCode`). Everything downstream keys off that uid.
- **New subsystem required:** an OAuth client (needs a VEKN account with the DEV
  role, granted by an IC), a registered callback endpoint in JOL, and an
  **encrypted per-tournament token store** (refresh token rotates on every use —
  replay kills the lineage, so storage must be transactional). A scheduled
  refresh before the 1-hour access-token expiry. This is the biggest new piece of
  infrastructure.

### Stage 1 — Registration + VEKN validation → good fit, with an identity-mapping change

JOL already captures VEKN ID at join (`TournamentRegistration.vekn`) and
validates it.

- **Public API, no OAuth:** validate each VEKN ID at registration via
  `GET /v1/users/{vekn_id}` — confirms the number is real and yields country.
  Drop-in improvement to JOL's current validation, deployable on its own.
- **Push roster:** `POST /bulk-register` with
  `rows: [{vekn_id, email, name, paid}]` and `default_paid`. Purpose-built for
  exactly JOL's case — **unmatched rows (players with no Archon account) are
  allowed** as long as they carry `email` + `name`.
- **Model mismatch — the important one:** JOL identifies a player by **JOL
  username** (`TournamentRegistration.player`), with `vekn` as a loose string.
  Archon identifies everything — `CheckIn`, `SeatPlayer`, `SetScore`,
  `standings`, `winner` — by `user_uid` (UUID v7). `ActionRegister` even
  *requires* `user_uid` and reads the VEKN off the account (never from you),
  which is why `bulk-register` (by `vekn_id`) is the only workable path.
  - **Change:** after `bulk-register`, read the tournament back and build a
    three-way map **JOL username ↔ VEKN ID ↔ Archon `user_uid`**; persist
    `archonUserUid` on `TournamentRegistration`. Players who stay unmatched (no
    Archon account) never get a stable uid — they can't be targeted by seat/score
    actions individually, so JOL needs a fallback (encourage account creation, or
    accept those tables can't round-trip scores automatically).

### Stage 2 — Check-in → minor gap

JOL has no check-in concept: registered + deck chosen = ready. Archon *requires*
`CloseRegistration` (Registration→Waiting) then `CheckIn`/`CheckInAll` before
`StartRound` is accepted.

- **Change:** JOL calls `CloseRegistration` + `CheckInAll` right before seating.
  Optional: a real check-in screen if JOL wants to track no-shows and `CheckOut`
  them instead of hand-editing seating.

### Stage 3 — Seating → the step to automate; fits well

Today: admin types the roster into Archon, Archon computes seating, admin copies
it back / CSV-imports into JOL (`importRoundsFromCsv`, `saveTables`,
`createTournamentTables`).

Two directions, both supported:

- **A — Archon seats, JOL imports (recommended for prelims):** `ActionStartRound`
  with `seating` **omitted** → Archon runs its standard seating algorithm → the
  response's `rounds[i]` is an array of `Table`, each with seat-ordered
  `seating: [{player_uid}]`. JOL maps `user_uid`→username, populates its
  `Table<round,table,players>`, then `createTournamentTables` spins up the JOL
  games. **This deletes the manual copy/CSV step entirely.**
- **B — JOL seats, pushes to Archon:** `ActionStartRound {seating:[[uid…]…]}` or
  `ActionAlterSeating {round, seating}`. Exact analogue of JOL's existing
  `saveTables` / `recreateTable` — keep as the **manual-override escape hatch**.

Mismatches:

- `ActionStartRound` also *starts play* (state → Playing) — there is no "compute
  seating without starting a round". So "JOL creates tables" becomes semantically
  "Archon starts the round"; the two systems' round-state now have to stay in
  step.
- Archon seat **order** is meaningful (predatory seating; `SwapSeats`,
  `SeatPlayer{seat}`). JOL stores a `List<TournamentPlayer>` per table — order
  exists but JOL may not treat it as significant. Confirm JOL's list order =
  intended seat order before pushing.
- Archon's engine wants 4–5 per table. JOL's own table-builder must not produce
  3- or 6-seat tables or `AlterSeating` will be rejected.
- `recreateTable` (destructive single-table rebuild) maps to `AlterSeating` for
  just that round — but Archon won't let you reseat a round that's already
  `FinishRound`-ed, whereas JOL's recreate has no such guard.

### Stage 4 — Round play & scoring → JOL is the play engine Archon expects

Archon has no game engine — `GET /api/tournaments/{uid}/decks` is explicitly
described as "the delegated read an online-play platform makes once a round
starts." JOL *is* that platform. Clean division of labour.

- JOL's `closeTableGame` already snapshots per-player VP and picks the GW winner.
  **Add:** `ActionSetScore {round, table, scores:[{gw,vp,tp}…]}` per seat, then
  `ActionFinishRound {round}` once every table is scored (Archon enforces this).
- `Score` is `{gw:int, vp:float, tp:int}`. JOL computes `vp` per seat and one
  `gw` winner; `tp` (table points) is Archon-derived — send `vp` per seat and
  `gw` on the winner, leave `tp`.
- **Mismatch — judge overrides:** JOL has no judge/override/sanction model.
  Archon has `ActionOverride {round,table,comment}` (comment mandatory), a full
  `/sanctions/` API, and `call-judge`. Today a bad JOL result is fixed with
  `recreateTable`. Optional change: an override-reason field on the close-table
  admin action that maps to `ActionOverride`; treat sanctions as read-only
  surfaced from `/stream`.
- **Mismatch — timer:** Archon has a round/finals timer (`round_time`,
  `timer/start|pause|add-time|reset`). JOL games are async and not hard-timed.
  Leave the Archon timer unused unless JOL adds round clocks.

### Stage 5 — Standings between rounds → stop computing them locally

JOL computes its own standings (`PlayerStanding`, `getRoundSummary`). Archon
computes `standings[]` (`gw/vp/tp/toss/finalist/disqualified`, ranked) from the
scores you push, using the canonical VtES tie-breakers.

- **Change:** after `FinishRound`, read `standings` straight from the action
  response and display those. Eliminates any chance of JOL's tie-break
  implementation diverging from the official one — a whole bug class gone.

### Stage 6 — Finals → fits cleanly

- Archon: `ActionStartFinals` seats the finals from standings (top 5); seeding
  draw is modelled explicitly as `ActionSetToss {player_uid, toss}` /
  `ActionRandomToss`; `FinalsTable.seed_order` holds the order; `AlterSeating`
  with `round` = one-past-last overrides finals seating; then `SetScore` +
  `FinishFinals`.
- JOL: `TournamentFinals {seeding[], seating[]}`, `createFinal`,
  `setFinalsSeeding` — maps almost 1:1.
- **Mismatch:** Archon records the seeding *draw* (toss integers) as provenance;
  JOL only stores the resulting ordered list. If a random draw happens, JOL keeps
  the order but loses the "how". Minor — accept it, or store the toss values in a
  new field.

### Stage 7 — Close & results → the payoff

- JOL `closeTournament` just sets status `CLOSED`.
- Archon `ActionFinishTournament` — *"Standings are final and ratings move."*
  Calling this is **what removes the manual VEKN result re-entry** that admins do
  today.
- Also push `ActionUpsertDeck {player_uid, deck:{cards:{<krcg_id>:n}}}` per
  player (JOL has full deck content) so Archon has decklists for archive/TWDA per
  the tournament's `decklists_mode`.
  - **Mismatch:** Archon deck cards use **krcg card IDs**. JOL's card DB is
    VEKN-sourced (`vtescrypt.csv`/`vteslib.csv`). krcg IDs are essentially the
    VEKN card IDs, so this is likely near-1:1, but a mapping/verification step is
    needed (krcg IDs resolve at `v4.api.krcg.org`).

### Cross-cutting — both UIs can edit the same tournament

Archon's own web UI stays usable during the event (a judge could DQ someone, file
a sanction, drop a player there). `GET /stream?tournament=<uid>` (SSE:
`tournament` / `sanctions` / `user` / `sync_complete` messages, full state replay
on reconnect) lets JOL detect out-of-band changes and avoid clobbering them on
its next push. Optional but advisable given the split-brain risk.

## Consolidated list of model gaps & required changes

| JOL today | Archon expects | Change needed |
|---|---|---|
| Tournament originates in JOL | No create endpoint; consent needs an existing `uid` | Create shell in Archon manually; store `archonUid`/`eventCode` on `TournamentDefinition` |
| No auth link to Archon | PKCE `event:run`, per-tournament consent, 30-day rotating refresh token | New OAuth client (DEV role), callback endpoint, encrypted per-tournament token table, refresh scheduler |
| Player = JOL username + loose `vekn` string | Player = `user_uid` (UUID v7) everywhere | Add `archonUserUid` to `TournamentRegistration`; roster read-back after `bulk-register`; fallback for no-account players |
| Registered + deck = ready | Explicit `CloseRegistration` + `CheckIn` gate | Call `CloseRegistration` + `CheckInAll` before seating; optional check-in UI |
| Shuffle / CSV-import seating | `StartRound` (auto) or `AlterSeating`; seat order significant; 4–5/table; starting a round == playing it | Use `StartRound` response as seating source; uid→username map; enforce table sizes; keep round-state in sync |
| `closeTableGame` snapshots VP/GW | `SetScore` per seat `{gw,vp,tp}` + `FinishRound` | Add push calls; derive per-seat `Score`; leave `tp` to Archon |
| No judge/override/sanction model | `ActionOverride{comment}`, `/sanctions/`, `call-judge` | Optional override-reason field; surface sanctions read-only from `/stream` |
| JOL computes standings + tie-breaks | Archon computes canonical `standings[]` | Read `standings` back from action responses; retire local calc |
| `TournamentFinals.seeding` = ordered list | Toss-draw integers, `seed_order`, `StartFinals` | Map order → `AlterSeating`/`SetToss`; optionally store toss values |
| `closeTournament` → `CLOSED` | `FinishTournament` → ratings move | Call `FinishTournament` — removes manual VEKN entry |
| Full deck content, VEKN card IDs | `UpsertDeck` with krcg card IDs | Card-ID mapping VEKN→krcg; push decklists |
| Round timer absent | Archon round/finals timer | Leave unused unless JOL adds clocks |
| Archon UI editable during event | `/stream` SSE | Optional: subscribe to avoid clobbering out-of-band edits |

## Suggested phased rollout

1. **VEKN validation** via Public API `GET /v1/users/{vekn_id}` — no OAuth, ship
   independently.
2. **OAuth onboarding + `bulk-register`** at registration close — establishes the
   uid mapping. Tournament still created by hand in Archon.
3. **Seating import** via `StartRound` — replaces the manual copy/CSV step.
4. **Score push** (`SetScore`/`FinishRound`) + read standings back from Archon.
5. **Finals + `FinishTournament` + `UpsertDeck`** — closes the loop, ends manual
   VEKN result entry.

The one structural constraint to accept up front: Archon will not let JOL *own*
the tournament record — no creation API, and its state machine (Registration →
Waiting → check-in → per-round Playing) is stricter than JOL's. JOL becomes the
play engine and a driver of Archon's state machine, not the system of record.

## Reference — Archon endpoints

### Public API (`https://api.archon.krcg.org`)

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/tournaments` | list (NDJSON stream) |
| GET | `/v1/tournaments/{code_or_uid}` | full tournament by event code or uid |
| GET | `/v1/leagues`, `/v1/leagues/{uid}` | leagues |
| GET | `/v1/users` | all members (NDJSON), filter by `country`/`category`/`tournament` |
| GET | `/v1/users/{uid_or_vekn_id}` | member by uid or VEKN ID |
| GET | `/v1/decks` | decks, filter by `tournament` |
| GET | `/v1/community-links` | member community links |
| GET | `/v1/export` | gzipped JSONL full corpus |
| POST | `/oauth/token` | `client_credentials` (public) or `authorization_code`/`refresh_token` (member) |
| POST | `/oauth/revoke` | invalidate tokens |
| GET | `/oauth/userinfo` | `sub` (member uid), `roles`, `vekn_id`, `capabilities` |

### Member API (`https://archon.krcg.org`, `event:run`, one tournament)

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/tournaments/{uid}/action` | all state changes (see action types below); returns full `Tournament` |
| POST | `/api/tournaments/{uid}/bulk-register` | `BulkRegisterRequest {rows:[{vekn_id,email,name,paid}], default_paid}` |
| GET | `/api/tournaments/{uid}/decks` | decks of the round currently in play |
| POST | `/api/tournaments/{uid}/announce`, DELETE `.../announce/{id}` | announcements |
| POST | `/api/tournaments/{uid}/call-judge` | `{table, round}` |
| POST | `/api/tournaments/{uid}/timer/{start\|pause\|add-time\|reset}` | round timer |
| GET/POST/DELETE | `/api/tournaments/{uid}/banner` | event image |
| GET | `/sanctions/reference`, POST `/sanctions/` | judge sanctions |
| GET | `/stream?tournament={uid}` | SSE live feed (`tournament`/`sanctions`/`user`/`sync_complete`) |

### Action types (`type` discriminator on `/action`)

Registration: `OpenRegistration`, `CloseRegistration`, `ReopenRegistration`,
`CancelRegistration`, `Register`, `Unregister`, `AddPlayer`, `RemovePlayer`,
`SetWaitlisted`, `SetPaymentStatus`, `MarkAllPaid`, `SetNonCompeting`.

Check-in: `CheckIn`, `CheckInAll`, `CheckOut`, `ResetCheckIn`.

Rounds/seating: `StartRound` (optional `seating`), `AlterSeating {round, seating}`,
`SeatPlayer {player_uid, table, seat, round?}`, `UnseatPlayer`,
`SwapSeats {round, table1, seat1, table2, seat2}`, `AddTable`, `RemoveTable`,
`SelfOrganizeRound {player_uids}`, `FinishRound {round?}`, `CancelRound {round?}`,
`RestoreRound {round?}`.

Scoring: `SetScore {round, table, scores:[{gw,vp,tp}]}`,
`Override {round, table, comment}`, `Unoverride {round, table}`,
`SetArchivalResults {winner, players, reported_player_count}`.

Finals: `StartFinals`, `CancelFinals`, `FinishFinals`,
`SetToss {player_uid, toss}` (one player's draw), `RandomToss` (draw for all
tied finalists at once).

Lifecycle/config: `FinishTournament`, `UpdateConfig {config}`,
`UpsertDeck {player_uid, deck, multideck?}`, `DeleteDeck {player_uid, multideck?}`,
`DropOut {player_uid}`, `SetNonCompeting`.

Raffle: `RaffleDraw`, `RaffleUndo`, `RaffleClear`.

### Key object shapes

- **Tournament states:** `Planned` → `Registration` → `Waiting` → `Playing` → `Finished`
- **Player states:** `Registered`, `Checked-in`, `Playing`, `Finished`, `Completed`, `Disqualified`
- **Table states:** `In Progress`, `Finished`, `Cancelled`, `Invalid`
- **`Score`:** `{gw:int, vp:float, tp:int}`
- **`Tournament.rounds`:** array (per round) of `Table` — `{seating:[{player_uid, result, judge_uid}], state, override, organized_by}`
- **`Tournament.finals`:** `FinalsTable` — as `Table` plus `seed_order:[uid]`
- **`Tournament.standings`:** `[{user_uid, gw, vp, tp, toss, finalist, disqualified, non_competing}]`
- **`Tournament.players`:** `[{user_uid, state, toss, result:Score, finalist, non_competing}]`
- All uids are UUID v7. `event_code` is a permanent public handle usable in place of the uid on Public API routes.
