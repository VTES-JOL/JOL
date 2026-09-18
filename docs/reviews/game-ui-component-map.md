# Game UI: component map, breakpoints, audience

Reference doc for `GamePage.tsx` (`src/main/webui/src/pages/game/`). Describes what
renders, at which widths, and how the same screen differs for a seated player, a
judge, and a spectator. Written from the code as of `feature/quarkus` (2026-09-11),
not from a redesign brief — see `docs/reviews/game-page-review-brief.md` /
`game-page-review-findings.md` for the review that shaped a lot of this layout.

## 1. Top-level structure

`GamePage.tsx` is one component; it composes four regions that persist across every
breakpoint, plus a fifth (talk rail) that only exists ≥1024px:

```
┌─────────────────────────────────────────── TableHud (sticky) ───┐
│ game name · turn/phase · pending-response flag · edge · Call    │
│ Judge / Notes / History — plus the rejected-command banner and  │
│ PendingActionBar when one applies                                │
├───────────────────────────────────┬─────────────────────────────┤
│  #table-col (the board)           │  #talk-rail (≥1024px only)   │
│   - opponents                      │   Game Chat / History,       │
│   - your seat + hand + commands    │   side by side for a         │
│     ("the dock")                   │   spectator/judge, or the    │
│                                     │   single toggled panel for   │
│                                     │   a seated player            │
└───────────────────────────────────┴─────────────────────────────┘
```

Below 768px the talk rail and dock disappear into `BottomSheet`s toggled by a
`MobileTabBar` docked at the foot of the column instead.

### Component inventory (by role)

| Role | Components |
|---|---|
| Persistent HUD | `TableHud`, `PhaseStepper`, `PendingActionBar`, `CallJudgeButton` (+ `JudgeRequestModal`), `NotesToggleButton` |
| Board / seats | `SeatGrid`, `SeatPager` (mobile), `SeatColumn`, `PlayerBoard`, `Region`, `MinionTile`, `AttachedCards`, `PermanentChip`, `PilesFooter` |
| Cards | `Card`, `CardSimple`, `CardHidden`, `CardImage`, `AttachedCards`, `CardAttrEditor`, `CardDialog` |
| Your seat / hand | `YourSeatDock`, `HandDock` (wide), `HandStrip` (mid/mobile), `BoardDensityToggle` |
| Acting | `DockCommandStack` (wraps `PendingActionBar` + `ActQuickBar` + `CommandForm` [+ `CallJudgeButton` on the mobile sheet]), `CommandForm`, `ActQuickBar`, `QuickCommandModal`, `QuickChatModal`, `PlayCardModal`, `CardContextMenu`, `TargetPicker` |
| Talk | `GameChatPanel`, `GameChatLog`, `ChatCompose`, `HistoryPanel` |
| Mobile shell | `MobileTabBar`, `BottomSheet` |
| Side drawers/modals | `NotesDeckDrawer`, `PlayCardModal`, `CardContextMenu`, `JudgeRequestModal` |

`TextModeContext` (image-free card rendering) and `BoardDensityContext` (2-up
tiles vs. text rows) wrap the whole tree so any card renderer can read them
without prop drilling.

## 2. Breakpoints

Three widths are read via `useMediaQuery`/`useIsMobile` (Tailwind's `md` = 768px):

| Name | Query | Constant in code |
|---|---|---|
| **Mobile** | `max-width: 767.98px` | `useIsMobile()` |
| **Mid** (768–1023px) | implicit — `!isMobile && !wideLayout` | — |
| **Wide** | `min-width: 1024px` | `wideLayout` |
| **Mid-wide** (1024–1399px) | `min-width: 1024px and max-width: 1399px` | `midWide` — passed into `SeatGrid` to fold 4 opponents into a 2×2 grid instead of 4 cramped columns |

### Mobile (<768px)
- Single scrolling column: `SeatPager` (swipeable, one seat at a time — your own
  seat first when seated, via `[me, ...others]`) + `MobileTabBar`.
- Talk, hand, and act each live in a `BottomSheet` toggled by the tab bar
  (`Table / Hand / Log / Act`); the sheets stay mounted so scroll position and
  loaded history survive close→reopen. Swiping up on the tab bar opens Hand.
- `textMode` is forced on below `md` — no image tooltips, since there's no
  hover on touch (`useCardTooltips` is disabled and cards fall back to
  text-only rendering).
- `HandDock`/`YourSeatDock`'s command band collapses into the `Act` sheet
  (`DockCommandStack variant="sheet"`), which also carries `CallJudgeButton`
  (moved off the HUD, which only shows two icon buttons here).
- `TableHud` stacks title+icons over the turn/phase row instead of one line.

### Mid (768–1023px)
- No talk rail; no dock grid. The column stacks, top to bottom:
  `#opponents` (auto-fill `SeatGrid`, flex-`3`) → chat/history panel
  (flex-`1`, 7rem–35vh) → your seat + hand + `CommandForm` band (content-sized,
  13rem–55vh cap). This 3:2 split (NF1/NF5/NF6 in the review findings) replaced
  an earlier fixed-`45vh` block that starved the opponent grid.
- Hand renders as `HandStrip` (a horizontal strip), not the full-height
  `HandDock` column used at wide widths.

### Wide (≥1024px)
- `#table-col` and `#talk-rail` sit side by side; the rail is `40rem` wide for
  a spectator/judge (Chat + History side by side) or `30rem` for a seated
  player (single toggled panel).
- Seated: the column is a resizable split (`useResizableSplit`, remembered per
  game) between `#opponents` (top) and the **dock grid** (bottom) — an L-shape:
  your board + command band on the left column, `HandDock` as a full-height
  column on the right (`grid-template-columns: 1fr 1.15fr`).
  `BoardDensityToggle` sits above your board.
- Unseated (spectator/judge): `#opponents` simply fills the whole column —
  there's no dock to make room for.

## 3. Audience type

Three booleans on `GameSnapshot` drive almost all of it: `player` (viewer holds
this seat), `judge`, `admin` (unused directly by the board — role checks here
are `player`/`judge`). `me` (from `seatOrder`) is non-null only when `player`
is true.

### Player (seated: `game.player === true`)
- Gets a seat in the ring — `me` is non-null, so wide layout renders the full
  dock grid (board + hand + commands); mid/mobile render the equivalent
  stacked/sheet forms.
- Only a player (or judge) can chat (`canChat = game.player || game.judge`) —
  `ChatCompose` is gated on this.
- `ActQuickBar` (context-sensitive quick actions) is gated on `game.player`
  specifically — a judge using the mobile Act sheet gets the free-text
  `CommandForm` but not the quick-action chips.
- The inline blood counter stepper (`counterBumpFor`) is interactive on the
  player's own seat (and any seat, for a judge); on an opponent's tile for a
  plain player it's a static pill — no wrong-affordance edit control.
- `PendingActionBar`/`pendingActionable` — only true when this viewer is the
  actor or is named in `pendingAction.awaiting`; otherwise the pending flag is
  shown in the HUD but isn't actionable from this seat.
- `CallJudgeButton` is visible ("Call Judge"); `PingOptions`-gated ping targets
  come from `game.pingOptions.includes(seat.name)`.

### Judge (`game.judge === true`)
- Not seated (`me` is null unless a judge is also a player in this game, which
  the type allows but the board doesn't special-case beyond the two flags) —
  wide layout falls into the "no dock" branch and gets the full-width
  opponents grid plus the two-panel Chat+History rail.
- Can chat and call/answer judge requests like a player (`canChat`,
  `CallJudgeButton` both check `game.judge` too).
- `HistoryPanel` exposes a **Commands** toggle (`useShowCommands`, judge-only)
  that reveals the raw command behind each chat line (`invocation`/
  `invocationSeq`) and fetches `CommandError` rows (`/command-errors`) for
  turns the judge is reviewing — mistyped commands that never produced chat,
  invisible to players/spectators.
- The counter-bump stepper is interactive on *any* seat's tile for a judge
  (`game.judge || seatName === viewerName` in `counterBumpFor`), matching the
  "judges legitimately adjust any board" affordance.
- On mobile, gets the `Act` sheet (`game.player || game.judge`) — free-text
  `CommandForm` + `CallJudgeButton`, but no `ActQuickBar` (player-only).

### Spectator (`player === false`, `judge === false`)
- No seat, no dock, no `ActQuickBar`, no `CommandForm`, no counter-bump
  affordance anywhere (`counterBumpFor` returns `undefined` unless
  judge-or-self).
- No chat compose (`canChat` false) — read-only `GameChatPanel`/`HistoryPanel`,
  still live via the same websocket-triggered refetch as everyone else.
- `HistoryPanel`'s Commands toggle and `CommandError` fetch never fire
  (`judgeCommands = game.judge && showCommands`).
- `CallJudgeButton` renders only if a request is already open
  (`!game.player && !game.judge && !request` short-circuits it away
  otherwise) — a spectator can see an active judge call but can't start one.
- Card visibility is still governed server-side, not by this role split: a
  hidden hand/library card never sends identity to any non-owning viewer
  (`CardHidden`/`CardVisibility`), so a spectator sees the same asterisked
  placeholders an opponent would, regardless of `judge`/`player`.
- Wide layout gets the two-panel Chat+History rail (`40rem`), same as a judge
  — the "no dock" branch is really keyed on `!me`, not on the judge flag
  specifically.

### Cross-cutting notes
- Ousted seats (`pool < 1`) collapse to a one-line strip for *every* audience
  (`SeatColumn`) — click to re-expand.
- `TextModeContext`/`BoardDensityContext` are per-viewer preferences
  (`nav.imageTooltipPreference`, per-game density), not audience-gated.
- Seat relation labels (`prey`/`predator`/`table`) are only computed relative
  to `me` — a spectator/judge sees no relation highlighting since there's no
  "your seat" to be relative to.
