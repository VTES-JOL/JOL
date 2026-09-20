# Game page — breakpoint × audience review

Visual audit of `/jol/game/{id}` across its responsive breakpoints and its 3 viewer
audiences, captured against the e2e fixture's "Test Game" (`01K6CP9GMWMG78RERJVA2QM0R3`,
5 seated players, real board state — Player1 through Player5). Intended to seed a
rethink of the breakpoint/layout design, not as a completeness check.

## The 3 breakpoint bands

The game page does not use Tailwind's `md:`/`lg:` utility prefixes for its main
layout split — it drives everything off 3 JS media-query hooks in `GamePage.tsx`:

| Band | Query | Hook | Tailwind alignment |
|---|---|---|---|
| **mobile** | `max-width: 767.98px` | `useIsMobile()` (`src/hooks/useMediaQuery.ts:22`) | = `md` (768px) |
| **mid** | `768px – 1023px` (i.e. `!isMobile && !wideLayout`) | — | = `md`–`lg` |
| **wide** | `min-width: 1024px` | `wideLayout` (`GamePage.tsx:116`) | = `lg` (1024px) |
| **wide / midwide sub-band** | `1024px – 1399px` | `midWide` (`GamePage.tsx:121`) | bespoke, no Tailwind equivalent |

`midWide` only affects one thing: `SeatGrid`'s `countedLayout` (`src/pages/game/SeatGrid.tsx:9-30`)
folds the opponent grid to 2 columns instead of 3+ whenever `midWide && seats.length >= 4`.
Screenshots below cover **mobile** (390px), **mid** (900px), **midwide** (1200px, inside
the wide band but showing the 2-column fold), and **wide** (1600px). A fifth, **ultrawide**
(2000px, player audience only) shows the board's 1800px max-width cap (`GamePage.tsx:503`,
`TableHud.tsx:190`) engaging with visible side gutters.

## The 3 audiences

Gated by `game.player` (seated) / `game.judge` (has the `JUDGE` role **and** is not
seated — `GameSnapshotFactory.java:65`: `isJudge = !isPlayer && JolAdmin.isJudge(viewer)`).
Neither flag set = spectator.

- **Player** — Player1, seated. `showHand = game.player` (`GamePage.tsx:295`), so only
  a seated player gets a Hand tab/dock. `showAct = game.player || game.judge`.
- **Judge** — Player7, granted the `JUDGE` role for this review but deliberately left
  unseated in Test Game. Gets `canChat` (`GamePage.tsx:296`: `game.player || game.judge`),
  the counter-bump affordance on *every* seat's minions (`:304`: `game.judge || seatName === viewerName`),
  and a standalone `judgeCommandBar` (`:438`) since they have no seated dock to hang a
  command input off. Also sees judge-only chat content: raw mistyped-command attempt rows
  interleaved in Game Chat, and a "Commands" chat-filter toggle.
- **Spectator** — Player6, no roles, unseated. `canChat` is false (no `ChatCompose`),
  `showAct`/`showHand` both false (no Hand/Act tabs, no command bar anywhere), counter-bump
  disabled on every tile.

## Mobile (390px)

### Player — `screenshots/player-mobile.png`
`TableHud` collapses to its 2-row mobile header (`TableHud.tsx:191-198`): title + 2
icon buttons (`metaElsMobile` — Notes, History; Call Judge is absent here, it lives in
the Act sheet instead per the comment at `TableHud.tsx:166-167`) on row 1, turn/phase
info wrapping on row 2. Below that, `SeatPager` (`GamePage.tsx:507`) shows **one seat at
a time** — page 0 is the viewer's own board (`me`), swipe/arrows move through opponents.
`MobileTabBar` at the foot has all 4 tabs: Table / Hand (badge = hand count) / Log
(unread dot) / Act.

- `screenshots/player-mobile-hand.png` — the Hand bottom sheet, a 2-column card grid,
  full-bleed over the table.
- `screenshots/player-mobile-act.png` — the Act sheet: command input + Submit/End Turn,
  Quick Actions row (Unlock all / Edge / Burn edge / Draw), and Call Judge folded in here
  (not in the header) for a seated player at this width.
- `screenshots/player-mobile-log.png` — the Log sheet: full Game Chat, scrollable,
  compose bar pinned at the bottom of the sheet.

### Judge — `screenshots/judge-mobile.png` / `screenshots/judge-mobile-act.png`
Same `TableHud` mobile header and `SeatPager`, but only **3** tabs: Table / Log / Act —
no Hand tab (`showHand` is false for an unseated judge). The Act sheet
(`judge-mobile-act.png`) is materially different from the player's: no End Turn, no
Quick Actions row, no Call Judge row — just the bare command input + Submit, because
this is `judgeCommandBar`, not the seated `CommandForm`.

### Spectator — `screenshots/spectator-mobile.png`
Only **2** tabs: Table / Log — no Hand, no Act (spectator has neither `showHand` nor
`showAct`). `SeatPager` still renders and still labels page 0 **"your seat"**
(`SeatPager.tsx:62`, `page === 0 ? 'your seat' : 'swipe the table'`) even though the
spectator has no seat — it's just showing the first player in seating order. Table Talk
panel is absent (that's part of the chat sheet, gated the same as `canChat`).

## Mid (900px, the 768–1023 band)

### Player — `screenshots/player-mid.png`
`TableHud` is one row (desktop layout — `isMobile` is false here), full turn/phase
chip row plus the meta cluster (Call Judge / Notes / History) inline on the right.
Below it: `SeatGrid variant="autofill"` (`GamePage.tsx:562`) — opponents auto-fill a
row, cards sized to a ~17rem min, no fixed column count. Per the F2 comment at
`GamePage.tsx:539-545`, chat is intentionally moved **out** of the scrolling column at
this width into a collapsible "Table Talk" bottom sheet (visible as a pill above "Your
seat") rather than fighting opponents/dock for space. Own board + hand dock stack at
the foot of the column with the command input and quick actions inline.

### Judge — `screenshots/judge-mid.png`
Same `SeatGrid variant="autofill"`, but rendering **all 5** seated players flat (no
"your board" section — the judge has none) — `others` here is every player, not 4
opponents. A "Table Talk" toggle is present (judge can chat). Below the board: a
standalone **"Judge Commands"** labelled bar with just the command input + Submit — no
quick actions, no end turn, no hand dock, so the whole bottom third of the column is
comparatively sparse.

### Spectator — `screenshots/spectator-mid.png`
Same all-5-flat `SeatGrid`, "Table Talk" toggle present (contradicts the earlier claim
that spectators can't chat — worth checking: the toggle renders but clicking it may
still gate on `canChat` before actually composing; not verified in this pass, flagged
below). No command bar, no hand dock. The result is roughly **40% of the viewport
height below the board is empty white space** — visible in the screenshot from y≈420
down to the viewport bottom.

## Wide (≥1024px)

### midwide sub-band (1200px — the 2-column fold)

- **Player** (`screenshots/player-midwide.png`) — `SeatGrid variant="counted"` folds to
  2 columns: prey/predator in the highlighted top row (green "YOUR PREY" / red-bordered
  "YOUR PREDATOR ▸ACTING" framing), the other 2 opponents below. Right rail is the full
  Game Chat panel (`wideLayout` gives chat its own column, per `GamePage.tsx:114-115`).
  Bottom-left: own board + quick actions; bottom-middle: hand grid, draggable divider
  between opponents and the dock (`⋯ drag ⋯`, `useResizableSplit`).
- **Judge** (`screenshots/judge-midwide.png`) — same 2-column fold, but for **5** flat
  seats (3 rows: 2+2+1), no prey/predator framing (judge has no seat to frame relative
  to), full chat rail including the judge-only mistyped-attempt rows and "Commands"
  filter toggle, standalone Judge Commands bar at the very bottom.
- **Spectator** (`screenshots/spectator-midwide.png`) — same 2-column/5-seat fold, full
  chat rail, **no command bar at all** — the column ends right after the last seat card,
  leaving a large empty area (roughly the bottom third of the left column) with nothing
  in it.

### wide (1600px, no fold)

- **Player** (`screenshots/player-wide.png`) — `SeatGrid` now fits opponents in a single
  row (`cols = 4` for 4 opponents at this width), same L-shaped dock (board+commands
  left, hand right) and chat rail.
- **Judge** (`screenshots/judge-wide.png`) — 5 seats now `cols = 3` (3+2), same judge
  chat affordances, Judge Commands bar full width at the bottom.
- **Spectator** (`screenshots/spectator-wide.png`) — same 3+2 grid, chat rail, still no
  command bar — at this width the empty area below the seats is even larger in absolute
  terms since the column is wider.

### ultrawide (2000px, player only) — `screenshots/player-ultrawide.png`
The `max-w-[1800px]` cap (`GamePage.tsx:503`) engages: board content stops growing and
centers, with visible side gutters exposing the page's photo background
(`RouteBackground`). `TableHud`'s own inner wrapper caps at the same 1800px
(`TableHud.tsx:190`), so the sticky header's content aligns with the board below it
rather than stretching edge-to-edge — confirmed working as documented.

## Observations (for the redesign discussion)

1. **`midWide`'s 1399px cutoff is arbitrary and invisible in the UI.** It doesn't
   correspond to any Tailwind breakpoint (`lg`=1024, `xl`=1280, `2xl`=1536) — a
   4-or-5-seat game folds to 2 columns from 1024–1399px, then unfolds again from
   1400px, with nothing in the UI signalling why. If the fold is meant to line up with
   "the opponent cards are too narrow to be legible," that threshold should probably be
   derived from content (card min-width) rather than a fixed viewport number that
   happens to sit between two real Tailwind breakpoints.

2. **Spectator view wastes significant vertical space at mid/midwide/wide.** With no
   hand dock and no command bar, the left column simply ends after the last seat card,
   leaving 30–40% of the column height empty at every width ≥768px (see
   `spectator-mid.png`, `spectator-midwide.png`, `spectator-wide.png`). A spectator-only
   layout could let the seat grid grow to fill the column, or promote chat/history to
   fill the reclaimed space instead of leaving it blank.

3. **`SeatPager`'s "your seat" label is wrong for judge/spectator mobile.** `SeatPager.tsx:62`
   unconditionally labels page 0 "your seat" — for an unseated viewer it's just showing
   the first player in seating order, mislabeled as if it were the viewer's own board.
   Minor but visible in `judge-mobile.png` / `spectator-mobile.png`.

4. **Judge's Act sheet (mobile) and Judge Commands bar (mid/wide) are much sparser than
   the player's equivalent** — just an input + Submit, no quick actions, no End Turn
   (correctly, since a judge isn't ending anyone's turn) — but there's also no shortcut
   for common judge actions (adjust pool, move a card, etc.) the way the player's Quick
   Actions row offers. Worth deciding whether judges need their own quick-action set or
   whether the bare command line is intentional.

5. **Spectator's "Table Talk" affordance appears at mid/midwide/wide even though
   spectators can't chat** (`canChat = game.player || game.judge`, `GamePage.tsx:296`).
   Not fully verified in this pass whether the toggle is inert or whether spectators can
   in fact read table talk without composing — worth a quick code check before assuming
   this is a bug vs. intentional read-only access.

6. **The 4-tab vs. 3-tab vs. 2-tab mobile bar is a real, well-executed audience
   distinction** (Table/Hand/Log/Act → Table/Log/Act → Table/Log) — this is one of the
   better-differentiated parts of the layout across audiences and is worth keeping as a
   pattern if the breakpoints themselves get reworked.

7. **`midWide`'s 2-column fold changes seat *count semantics* between audiences at the
   same width** — for the player it's "prey/predator get the visible top row, cross-table
   opponents scroll" (a deliberate, documented choice — `SeatGrid.tsx:9-16`); for
   judge/spectator there's no prey/predator concept, so the same 2-column fold just
   produces a plain 2-column list with no framing logic behind *why* 2 columns specifically
   at this width. The fold may be worth reconsidering as player-only, with judge/spectator
   using a different (e.g. always-3-or-4-column) rule since they don't get the
   prey/predator benefit that motivated the fold in the first place.

## Dev environment note

These screenshots were captured against the e2e fixture DB (`load-test-fixtures.sh`,
local Postgres via `local-docker-compose.yml`), with the dev server run as:

```
JOL_DB_URL=jdbc:postgresql://localhost:5432/jol JOL_DB_USER=jol JOL_DB_PASSWORD=jol \
  ENABLE_CAPTCHA=false ./mvnw quarkus:dev -Dquarkus.profile=prodlike
```

(`%dev.quarkus.datasource.devservices.enabled=true` means the plain `%dev` profile
always spins up a throwaway Dev-Services Postgres regardless of `JOL_DB_URL`/
`JOL_DB_PASSWORD` — `-Dquarkus.profile=prodlike` is required to actually point at the
fixture DB, matching CLAUDE.md's documented "real Postgres" dev mode.) Player7 was
granted the `JUDGE` role directly in Postgres for this review (`player_role` insert) —
it is not part of the stock fixture data. To return to normal Dev-Services mode, stop
this process and run `ENABLE_CAPTCHA=false ./mvnw quarkus:dev` as usual.
