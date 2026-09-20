# §6 investigation — WS payloads, optimistic UI, text-mode cards

Follow-up to `game-page-review-brief.md` §6 / `game-page-review-findings.md`'s
"Investigation / discussion items" — those three were flagged as
architectural, "raise findings before implementing," and left undecided.
This is that raise. No code changed as part of this doc; §6a stays deferred,
§6b and §6c have concrete next steps below.

## §6b — perceived lag: measured, not guessed

**Method:** wrapped the chat-send action in `performance.now()` / the
Resource Timing API (`performance.getEntriesByType('resource')`) around a
`POST /game/{id}/view/submit`, live in Chrome:

- **Dev/localhost** (`ShanDow`, game `6bffd3f9-…`): **61ms**.
- **Real network** (`ShanDow`/`marbig` on `test.deckserver.net`, game "Afraid
  detribalizing breakfast"): **1096ms**, then **1203ms** on a repeat — not a
  one-off connection warm-up cost.
- For comparison, two plain `GET`s on the same test-server connection:
  `GET /nav` **269ms**, `GET /game/{id}/view` **274ms**.

**So the submit POST costs ~4× a plain GET on the same server.** Traced the
code path: `GameStateResource.submit` → `GameModel.submit` →
`JolAdmin.saveGameState` → `GameService.saveGame` — a single synchronous JPA
write-through (chat lines are explicitly batched into the *same* transaction
as the game_state write, per `GameModel.submit`'s comment — this isn't N
serial round-trips, it's one). That one write is the ~800ms+ delta above an
equivalent GET.

**A second assumption from the original brief turned out to be wrong:**
I expected the WS-signal → refetch dance to be adding its own cost on top of
the POST. It isn't, for the acting player — confirmed empirically (exactly
one network request appears per submit, not two). See §6a below for why.

**Recommendation:** optimistic UI is the right lever here, and only that
lever — no WebSocket redesign touches this cost, because it's inside the POST
response, before the client can render anything. D15 already pre-writes
lock/play/phase/end-turn locally and reconciles on response. Extend the same
pattern to the actions users fire most often and expect to feel instant:
**chat send** (render the line immediately, reconcile/rollback on response)
and **`ActQuickBar`'s one-tap commands** (unlock all, edge, draw) — these are
exactly the "everyone does this every turn" commands the ~1s write currently
makes feel sluggish. `CommandForm`'s free-text input is a reasonable
exception to leave un-optimistic (arbitrary text, harder to predict the
resulting state).

Whether `GameService.saveGame`'s write itself can be made faster (connection
pool sizing, payload size of the `game_state` JSON blob, write batching
beyond what's already batched) is a backend/persistence question, not a UI
one — flagging it here since it's the actual root cause, but it's out of
scope for this review to chase further.

**Not measured this pass:** a second (non-acting) viewer's update latency
after the WS push arrives — would need a second live connected session on
the same game to measure for real. Expected, from the code path, to be
roughly the plain-GET cost (~270ms) plus WS delivery (typically tens of ms),
not independently confirmed live.

## §6a — WS payloads instead of signals

The original framing ("today `/ws/updates` carries only a 'something
changed' signal") undersold what's already there. `WebSocketRegistry.notifyGame`
sends `{"type":"invalidate","key":["game",gameId],"stamp":<n>}` — a
per-game monotonic version stamp (D8), not a bare signal — and
`ws/useQueryInvalidation.ts` already skips the refetch entirely when the
client's cached snapshot is at or past that stamp. That's exactly what makes
the acting player's own tab cost **one** request per submit (the POST) and
**zero** extra GETs: the POST response already updates the cache to a stamp
≥ the one the WS push will carry, so the invalidate the actor's own socket
receives (self-notification is intentional — other tabs of the same player
need it) is a no-op.

So dropped-frame recovery, actor self-exclusion, and stale-cache detection —
the concerns the brief said this investigation would need to weigh — are
already solved. What's left is narrower than the original framing suggested:
**only other already-connected viewers of the same game** (opponents,
spectators, a judge) whose cached stamp is behind still pay a full
`GET /view` — measured at ~270ms on the test server.

**Recommendation: don't build this now.** The remaining gap is real but
small (270ms, and only for people who aren't the one who just acted, so
least latency-sensitive), while the cost to fix it is large:
`GameSnapshotFactory`'s HAND/RESEARCH enrichment is scoped per requesting
viewer, so a single broadcast payload can't serve every connected session
identically — the server would have to build N different payloads per push
(one per connected session) inside `WebSocketRegistry.notifyGame`, a
materially bigger change than the stamp mechanism it already has. Revisit
only if there's an actual complaint about opponent-side staleness; nothing
in this review surfaced one, and §6b's measurement shows the real lag
complaint lives entirely in the POST latency, which this wouldn't touch.

## §6c — image-free / text-only card rendering: design options

**Gap, confirmed in code, is narrower and more specific than "no images":**
`CardSnapshot` already carries every *structural* field a text row would
want — clan, sect, path, disciplines, capacity, votes, label, typeClass —
and `MinionTile`/`CardSimple`/`PermanentChip` already render all of it as
icon glyphs regardless of `textMode`. `TextModeContext`'s own doc comment
("nothing is lost") overclaims: what's actually missing is the card's
**rules text** (and `cost`, today enriched only for the viewer's own
HAND/RESEARCH cards via `GameSnapshotFactory`). `Card.cardText()` exists
server-side (`CardRegistry`, parsed from the VEKN CSVs at boot) but nothing
exposes it to the client outside the image-adjacent static HTML tooltip
fragments — and `useCardTooltips` explicitly tears those down when
`enabled=false` (text mode). A sighted mouse user in text mode, or anyone on
mobile (where text mode is forced), currently has **no way to read what a
card on the board actually does.**

### Data path — how the text gets to the client

1. **Batch lookup endpoint** (recommended) — mirror the deck editor's
   `CardSearchService` pattern: `GET /jol/api/cards/detail?ids=...` →
   `{id, cardText, cost, …}` per id. Fetched lazily, only for cards the
   viewer actually opens; cached client-side indefinitely (card text is
   static reference data, not game state — `staleTime: Infinity` in
   react-query, no snapshot bloat, no repeat fetches across a session).
2. Extend `CardSnapshot` to always carry `cardText`/`cost` for every card on
   the board. Simplest to wire, but bloats every `/view` response (and every
   refetch) with text for cards that may never be inspected — works against
   the "no per-card JSON definition" direction the Quarkus migration
   deliberately took (the old `CardService`/`cards.json` path was removed on
   purpose).
3. Revive a static `cards.json`-style bundle. Explicitly killed in the
   migration per `CLAUDE.md` — not recommended.

### Surface — where the text appears

1. **Extend the existing click-to-act surface** (recommended) —
   `MinionTile`/`CardSimple`/`PermanentChip` already open `CardContextMenu`
   or `PlayCardModal` on click; add a card-text section there (or a small
   info affordance that expands one) instead of a new interaction mode.
   Reuses the click *and* F10's new keyboard path for free; no new
   component, no layout disruption, and it's the one option that helps
   mobile (where text mode is forced) without competing for rail width —
   which NF2 already flagged as tight past a point.
2. **Docked detail panel** — click populates a persistent side panel, good
   for keeping a card's text up during a whole turn, but a new rail tenant
   competing with Chat/History for the same width budget.
3. **Inline expandable row** — tapping a tile grows it in place. Keeps
   everything on the board, but `MinionTile`'s fixed-height rows becoming
   variable reflows every neighboring tile — more layout work than 1 or 2
   for comparable benefit.

**Recommendation:** data path 1 + surface 1 — a batch text/cost lookup baked
into the card's existing action menu/play modal, not a new panel or mode.

**Scope note:** chat/log card mentions (`[card:<id>:<name>]` tokens,
rendered by `<CardToken>`) lose the same image tooltip in text mode and want
the identical fix — the same batch endpoint, a lightweight text popover in
place of the image tooltip. No separate design needed; call it in scope for
whoever builds this.

## Summary

| Item | Verdict |
|---|---|
| §6a (WS payloads) | **Defer.** Already mostly solved (stamp-based skip); remaining gap is small and the fix is disproportionately expensive. |
| §6b (optimistic UI) | **Build.** Root cause is a ~800ms-1s synchronous write inside the POST, confirmed on the real test server; extend D15's pattern to chat send + `ActQuickBar` commands. |
| §6c (text-mode cards) | **Design chosen, not built.** Batch `/cards/detail` endpoint + surface it in the existing action menu/play modal. |
