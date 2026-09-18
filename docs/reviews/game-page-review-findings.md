# Game table page — UI review findings

Review of `GamePage` + `pages/game/**` from three viewpoints (seated **player**,
**judge**, **spectator**) across the Tailwind breakpoints plus a 2560 ultra-wide
column. Method: static analysis of the components + backend beans, then live in
Chrome (logged in as `ShanDow` — seated — and `acbishop` — judge, unseated) on the
active 5‑player game `Rapid clearing graphic`.

## Method / confidence caveat

The Chrome instance would not honour window resizing — the render viewport stayed
locked at 1800 CSS px (the brief anticipated this). So:

- **Live‑verified**: the wide layout (~1800px, which lands in this page's "1400+"
  band) in the Light theme and the dark **Nightshade Slate** theme, for the player
  and judge personas.
- **Simulated** (matchMedia shim + a width‑clamped `#root`, then a remount): the
  `<md` mobile shell, the 768–1023 band, and the 1024–1399 band. CSS `md:`
  utilities and `createPortal` sheets do **not** follow the shim, so mobile
  type‑scale / sheet width and the 3‑letter phase labels could not be confirmed
  visually — those are marked *verify on device*.
- **Static only**: 2560 ultra‑wide (nothing on this machine renders it), and the
  a11y keyboard‑path findings (read from the components).

This page's real layout breakpoints are **767.98**, **1024**, and **1399** — not
Tailwind's sm/md/lg/xl/2xl. So Tailwind `xl` (1280) is inside the "1024–1399"
band, and `2xl` (1536), 1800 and 2560 are all the *same* "1400+" layout.

---

## Findings — ranked

Each: **severity** · personas · breakpoints · (evidence) — problem → fix sketch.
"Needs a direction call" and "product decision" are separated at the end.

### F1 — Decorative route background bleeds through the whole play area
**High** · player / judge / spectator · all widths, worse in dark, worse mobile ·
(live: Light + Nightshade Slate; root cause in code)

`RouteBackground` paints `Locations23.jpg` on a `position:fixed; z-index:-1` layer
for `/game/*`. Every page root is transparent by design — the readability
contract is "text sits on translucent `bg-surface/85` plates; the plate shows
through in the gutters" (`RouteBackground.css` comment). That assumption holds on
list/form pages with thin gutters. **The game table is mostly gutter**: the
wide‑dock area, the space below the hand column, and (judge/spectator) the gap
between seat rows have *no* plate at all, so a busy high‑contrast sparks/welding
photo sits directly behind live game state. In the dark themes it dominates the
lower third of the screen and reads as a rendering bug. The dark veil
(`body[data-bs-theme="dark"] .route-bg::after`, base @ 58%) *is* applied by
`theme.ts`'s `paint()`, but 42% transparency + a 2px blur is nowhere near enough
against this image over this much bare space.

Fix (pick one): give the game route an opaque ground — `bg-base` on `#table-row`
or the `GamePage` root (the table is not an "atmosphere" page); **or** a
game‑route‑only heavy veil (~85–90%); **or** give the dock, hand column and
board area their own `bg-surface` panels like the opponent seats already have.

### F2 — 768–1023 band: opponents pane and chat pane are both crushed unusable
**High** · player / spectator (judge uses a different branch) · 768–1023 ·
(sim @900)

The stacked `else` branch puts three vertical scrollers in one column:
`#opponents` (`flex-[3]`, 7rem floor), the chat panel (`flex-[1]`, 7rem floor /
35vh ceiling) and the dock (`min-h-[13rem]`). At this height they all lose —
opponents showed ~1½ minion rows per seat before clipping; the chat panel showed
a **single** line plus the compose box. A laptop or split‑screen user at
1000px cannot see opponent board state or read the log without scrolling each
pane on its own.

Fix: at `<lg` collapse to **one** primary scroller — e.g. move the log to a
bottom sheet / tab as `<md` already does, or make opponents a horizontal
scroll‑snap row so each seat gets full height. Don't run three fighting scrollers.

### F3 — 1024–1399: your prey and predator are the seats below the fold
**Med‑High** · player · 1024–1399 (includes Tailwind `xl` 1280) · (sim @1200)

`countedLayout` folds 4 opponents to a 2‑col grid ordered
`[…cross‑table, prey, predator]` — prey and predator **last**. `#opponents` is a
`flexBasis: 50%` `overflow-y-auto` box, so in a 5‑player game the two seats you
act on and defend against are exactly the ones scrolled out of view.

Fix: order prey + predator **first** in the fold (or pin them to the top row),
and/or give the 2×2 enough height to show all four without scrolling.

### F4 — Opponent boards clip even at full width
**Med‑High** · player · lg+ · (live @1800)

Even in the wide layout `#opponents` is `flexBasis: topPercent%` (default **50%**).
With 4 opponent columns each board is ~250–260px tall before its internal scroll —
Askelon's board was already clipped at the last READY minion in the first
screenshot. A seated player can't see a full opponent board without dragging the
divider or scrolling the pane.

Fix: raise the default split (opponents 60–65%), or default the archival piles
collapsed (mostly are) and cap READY/TORPOR tile height so a seat fits, or a
"peek / expand this seat" affordance.

### F5 — Game‑log SYSTEM lines clip their first character
**Med** · all · all widths · (live zoom — "6:41" rendered for "16:41")

`p.chat` sets `text-indent:-1.35rem`; `.chat-system` only adds
`padding-left:0.5rem`, so the first glyph of every machine line (phase/turn
markers, ousts, rulings) is cut off on the left.

Fix: `p.chat.chat-system { padding-left:1.35rem; text-indent:0 }` (or adopt the
`.chat-accent` 0.85 / −0.85 pair).

### F6 — A judge cannot issue a command or correction from the table
**Med** · judge · all widths · (live: acbishop) — *product decision*

`game.judge` viewers get the `!me` branch: no `DockCommandStack`, and
`CommandForm` early‑returns on `!game.player`. So a judge can read the log with
command annotations and type in table chat, but has **no** control to actually
act — despite `GameStateResource.submit` explicitly allowing `canJudge`.

Fix (decide): a command input in the judge rail, or an explicit "act as judge"
mode; or confirm judges are expected to correct games only via some other surface.

### F7 — "Call Judge" shows to the judge (and spectator)
**Med** · judge / spectator · all widths · (live)

`CallJudgeButton` is in the HUD's unconditional `metaEls`. A judge doesn't summon
themselves; a spectator can't be helped by it.

Fix: hide when `game.judge`; hide or relabel for pure spectators.

### F8 — 2560 ultra‑wide adds whitespace, not information
**Med** · all · 2560 · (static — same "1400+" layout as 1800)

Nothing keys off width past 1399. The opponent grid is
`repeat(<seatCount>, 1fr)` so seats just get wider; the rail is a fixed
`w-[30rem]` / `w-[40rem]`; the log caps at `max-width:68ch`. Net effect at 2560:
inflated gutters and a rail that's mostly empty. There's also no
comfortable/compact **spacing** control — only the tiles↔text density toggle.

Fix (decide the intent): either cap + centre the board at ~1800 and stop
pretending, **or** spend the width — persistent History beside Chat for the
seated player too, larger card art, a second log column, or per‑seat detail.

### F9 — "End Turn" is a near‑invisible secondary control
**Med** · player · all widths · (live — worse in Light)

One of the three highest‑value actions on the screen is
`Button variant="secondary"`, and when disabled (not your turn — the common
state) it's a faint outline that's easy to miss entirely.

Fix: give it a visible disabled state and a "Not your turn" tooltip; treat it as
primary‑adjacent (outline‑accent), not a throwaway secondary.

### F10 — Board click‑to‑act has no keyboard path
**Med** · player / judge · all widths · (static — confirmed in components)

`MinionTile`, `CardSimple`, `CardHidden`, `PermanentChip` are `<li onClick>` with
no `role="button"`, `tabIndex`, or `onKeyDown`. The nested lock / counter buttons
are focusable, but the **tile‑body click that opens the action menu** (bleed,
block, transfer, rescue, …) — the core interaction of the whole page — cannot be
reached or fired from the keyboard. Same pattern to check on `HandStrip`.

Fix: make the tile a real `<button>` (or add `role="button"` + `tabIndex={0}` +
Enter/Space), with a visible focus ring.

### F11 — Seated player's rail never goes side‑by‑side; wastes width at xl+
**Low‑Med** · player · xl / 2xl / 2560 · (live + code)

`railContent` gives Chat **and** History side‑by‑side only to `!me` viewers. A
seated player always gets the single 480px panel with the HUD toggle swapping
between them, at every width. Past ~1700px there's room for both.

Fix: let the seated rail widen and split past a threshold, or narrow it and give
the reclaimed width to the board.

### F12 — Log line measure vs rail width mismatch
**Low‑Med** · all · lg+ · (live + css)

`p.chat { max-width: 68ch }` caps the line, but the rail is a fixed 30/40rem, so
there's a dead strip on the right of every log line at wide widths, and the
mobile Log sheet / lg rail were the reason the cap exists — fine there, just
visually loose in the fixed rail.

Fix: align rail width to the measure, or left‑align content and use the slack for
a timestamp gutter / actor avatar.

### F13 — "Burn edge" quick action always reads as active/destructive
**Low‑Med** · player · all widths · (live)

In the quick‑actions band "Burn edge" renders in a red/orange treatment that
looks selected even when the viewer doesn't hold the edge and it isn't their
turn.

Fix: neutral styling unless applicable; disable when not actionable.

### F14 — "…" quick‑command trigger is an opaque affordance
**Low‑Med** · player · all widths · (live)

A bare dotted square containing an ellipsis. `aria-label` is set, but visually it
reads as "truncated / more", not "quick commands".

Fix: an icon (`Zap` / `Command`) and, at ≥lg where there's room, a "Quick" label.

### F15 — Hand column has large dead space below the cards
**Low** · player · lg+ · (live @1200/1800)

The wide‑dock hand column is full height; 7 cards in a 2‑up grid leave ~40% of
the column empty.

Fix: grow the hand grid's column count with available width/height, or dock the
command band into the foot of that column.

### F16 — "waiting" chip doesn't escalate
**Low** · all · all widths · (live — "waiting 1d")

Good that it's a duration, not "1 day ago". But a 5‑minute wait and a 1‑day wait
are the same gold chip.

Fix: escalate colour/weight past a threshold (e.g. >24h → blood).

### F17 — PhaseStepper contrast when it's not your turn
**Low‑Med (a11y)** · all · all widths · (static)

Past phases are `text-ink-muted`; the whole group also drops to `opacity-55` when
`!active`. Muted ink under a 55% opacity wash almost certainly fails AA.

Fix: use a dedicated dimmed token instead of stacking opacity on muted text;
check the current‑phase pill's white‑on‑accent both ways too.

### Verify‑on‑device (couldn't emulate)

- **F18** Phase stepper 3‑letter labels `<md` — code handles it (`md:hidden`);
  confirm the HUD turn row never wraps on a 360–390px phone.
- **F19** Mobile hand sheet renders through `createPortal(document.body)`; check
  `layout="list"` + `max-h` with 7+ cards on a real phone (it escaped the width
  clamp in the sim).
- **F20** Mobile: below the seat panel and above the tab bar there's bare space —
  with F1 unfixed that's pure background photo. Re‑check after F1.

---

## By breakpoint

| Width | Layout | Player | Judge / Spectator |
|---|---|---|---|
| `<768` mobile | SeatPager + tab bar + sheets | IA is sound (Table/Hand/Log/Act). F1 bleed, F18–20 to verify | Judge/spectator get the same shell; Act tab gates on `player \|\| judge` |
| 768–1023 | stacked, no rail | **F2** (panes crushed), F1, F4 | Judge uses `!me` branch → all seats + side‑by‑side rail; far better than the player's stacked view here |
| 1024–1399 (incl. xl 1280) | L‑dock + 480px rail, 2×2 opp. fold | **F3** (prey/predator below fold), F4, F1 | `!me` → `#opponents` full height, 640px rail Chat+History; good |
| 1400–~1900 (2xl 1536, 1800) | L‑dock + rail, counted columns | F4, F9, F11, F1 | Solid — all seats visible, command annotations, turn picker |
| 2560 ultra‑wide | *identical to 1400* | **F8** wasted width, F11, F12 | Same; rail could hold more |

## By persona

- **Player** — the core loop (see turn → read board → act → read log) works at
  1400+ but degrades hard from 1023 down (F2) and the two seats that matter most
  can be off‑screen (F3, F4). Click‑to‑act has no keyboard path (F10). End Turn is
  too quiet (F9).
- **Judge** — best‑served layout (all seats, Chat+History, command annotations),
  but **cannot act** (F6) and is shown a control meant for players (F7).
- **Spectator** — inherits the judge layout minus the command annotations and
  chat compose (correct). Same F7. No dedicated issues beyond the shared ones.

## Already strong (don't regress)

Optimistic pre‑writes (D15) so lock/play/phase/end‑turn move on click; one
dismissible rejected‑command home in the HUD; tiles↔text density toggle persisted
per game; prey/predator rail colours + chips + neutral "across the table" tag;
`poolTone` magnitude colouring; reconnecting chip on socket/online loss;
per‑actor log accent runs + New / date separators + jump‑to‑latest;
region auto‑expand on card gain and hidden‑UNCONTROLLED collapse;
3‑face pending‑action bar + end‑turn "seats still owe a response" warning;
mobile thumb‑zone tab bar + seat pager + stay‑mounted sheets; `tabular-nums`
on counts; `memo` discipline so an opponent's action doesn't re‑render every board.

## Test‑state note

`ShanDow`'s theme was toggled to Nightshade Slate during the review and **restored
to Light** via `PUT /jol/api/profile/theme`. No games or player rows mutated. Dev
server left running.
