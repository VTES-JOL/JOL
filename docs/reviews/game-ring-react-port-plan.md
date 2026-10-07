# Game ring board: React port plan

Plan for porting the Claude Design "ring" prototype (variants **C** wedge/blood-gauge
and **D** panel-per-seat) into `src/main/webui/src/pages/game/` as Storybook-first
components. Source prototype: the Design artifact "Game Screen: Player, Judge,
Spectator" (`TableRing`, `RingLab`, `MiniCard`, `SeatLegend`, `Main`, `Tablet`,
`UltraWide`, `Mobile`). Existing structure: `game-ui-component-map.md`.

Decisions recorded:
- Ring variants A (upright wedge) and B (cards face owner) are discarded.
- **C (wedge) and D (panel) are two user-selectable seat layouts** for the overview
  level. D has full parity with C: same interactions, same zoom/pan controls, only
  the seat layout strategy differs.
- **Three detail levels, switchable seamlessly by every audience** (player, judge,
  spectator): *table* (all seats, C or D), *triad* (focused seat + its predator +
  its prey), *seat* (one seat only).

## 1. Guiding decisions

1. **One geometry engine, pure and tested; thin presentational components on top.**
   The prototype's hard part is placement (packing cards inside a wedge). That is
   pure math and lives in plain `.ts` with Vitest unit tests, not in a component.
2. **Reuse the real data model.** Components take `PlayerSnapshot` / `CardSnapshot`
   directly (`api/types.ts`), through one small adapter (`ringModel.ts`) that turns
   a seat into the view-model the ring needs. No parallel type hierarchy.
3. **Two card renderers, one shared model.** `MiniCard` (and `UncontrolledMarker`)
   are the overview-level glyphs used by Wedge and Panels: small, glanceable, fixed
   footprint. Triad and Seat levels use a *separate*, richer detail renderer built
   on the existing `MinionTile` family (more text, disciplines, attachments, steppers,
   menus). Both are fed by one pure `cardView.ts` that derives status (locked,
   contested, torpor, face-down/hidden, blood/life vs capacity, attachments, title
   tags) from `CardSnapshot`, so the rules for *what a card means* live once while
   each renderer decides *how much to show*. Each renderer can evolve independently.
4. **Ring is a *board renderer*, chosen by layout, not a new page.** `GamePage`
   keeps its HUD, dock, talk rail, sheets. Only the "opponents area" swaps
   (`SeatGrid` today → `TableRing` / `SeatPanels`). Feature-flag it (per-viewer
   preference like `BoardDensity`) so the old grid remains until the ring is proven.
5. **Layout and detail level are independent axes.** `layout: 'wedge' | 'panels'`
   (overview only) and `level: 'table' | 'triad' | 'seat'` live in one small
   `useBoardView` state (per-viewer, remembered per game in localStorage like
   `BoardDensity`). Switching level never remounts the viewport or drops zoom.
6. **Behaviour parity later.** Prototype ignored interactions. Port visuals first
   (stories), then wire click → existing `CardContextMenu` / `CardDialog` /
   region drawers via the same `TableCardClick` callbacks `MinionTile` uses.

## 2. Component tree

```
game/ring/
  RingBoard.tsx          layout switch: wedge (C) | panels (D); owns zoom/pan viewport
  RingViewport.tsx       clipped viewport, wheel zoom, right-drag pan, +/-/Reset toolbar
  RingWedges.tsx         SVG: wedges, dividers, predator/prey chevrons, hub
  RingSeat.tsx           one seat: wedge shell + ready cards + rim + uncontrolled
  RingHub.tsx            centre text (turn / phase / direction hint)
  MiniCard.tsx           46x64 overview card face (own file, own CSS, own stories)
  miniCardFootprint.ts   the ONLY layout contract: size, locked rotation, badge overhang
  UncontrolledMarker.tsx circle-with-number crypt/hidden marker (own footprint export)
  cardView.ts            CardSnapshot -> CardView (status flags, counters, tags); shared
  detail/DetailCard.tsx  Triad/Seat card (extends MinionTile family; size 'triad'|'seat')
  SeatPanels.tsx         D: rectangular per-seat panels arranged round the ring
  BoardViewSwitch.tsx    segmented control: Table / Triad / Seat + Wedge/Panels toggle
  useBoardView.ts        {layout, level, focusSeat}; persisted; drives all three levels
  TriadView.tsx          focused seat centre-stage + its predator (right) and prey (left)
  SeatDetailView.tsx     single seat, large cards, detail rail (wraps PlayerBoard content)
  seatFocus.ts           focus -> {prey, predator} using PlayerSnapshot.prey/predator
  SeatLegend.tsx         compact standings table (pool, VP, edge, hand count)
  ringModel.ts           PlayerSnapshot -> RingSeatModel (ready / torpor / uncontrolled)
  ringGeometry.ts        angles, wedge polygons, membership tests
  ringPacker.ts          in-wedge lattice packer + rim layout
  useRingViewport.ts     zoom/pan state, clamping, wheel/pointer handlers
  useContainerSize.ts    ResizeObserver -> {w,h} (drives ring size)
  __fixtures__/ringFixtures.ts   turn-9, 2-5 player boards from real game.json data
```

### Responsibilities

| Unit | Input | Output / notes |
|---|---|---|
| `ringModel` | `PlayerSnapshot`, `viewerName` | `RingSeatModel { seat, ready[], torpor[], uncontrolled[], flags }`. READY region cards + attachments; TORPOR region to rim; UNCONTROLLED region to rim placeholders with counters. Hidden cards (`visible:false`) become placeholders. Reuses `seatOrder` so self sits at the bottom, prey left, predator right. |
| `ringGeometry` | seat count N, size, self index | Seat angles `-90 + i*360/N` rotated so self = 90 deg; annular sector paths; point/AABB-in-wedge tests. |
| `ringPacker` | wedge, card size | Square-cell lattice tried at 9 sub-cell offsets, cells fully inside wedge (8 sample points), global scale search 1 to 0.52; rim spread by tangential extent. **Pure**, ported from `TableRing.computeLayout`. |
| `MiniCard` | `CardView`, `style: 'gauge'|'pips'`, `scale?` | Locked = `rotate(90deg)`; contested outline; torpor dashed/grey; attachments as small squares; title tags. Renders only its own box; never positions itself. Footprint comes from `miniCardFootprint(view)`. |
| `UncontrolledMarker` | `CardView` (counters vs capacity) | Dashed circle + number, hidden/face-down variant. Footprint exported the same way. |
| `DetailCard` | `CardView`, level, callbacks | Triad/Seat card: full name, disciplines, capacity, blood stepper, attachments list, lock/contested chips, context menu. Independent of `MiniCard`. |
| `RingSeat` | `RingSeatModel`, layout result | Absolutely positions `MiniCard`s inside the wedge; whole wedge is one button (opens region view, replaces the old region icons). |
| `RingViewport` | children, size | Wheel zoom toward cursor (60-320%), right-drag pan, clamped; toolbar; stage scaled by `k = size/820`. |
| `useBoardView` | `seats`, `viewer`, `gameId` | `{layout, level, focusSeat, setLevel, setLayout, focus(seat)}`. Focus defaults to the viewer's seat, else the seat with the edge, else first seat. Clicking a wedge/panel focuses it and (on a second action or the switch) changes level. |
| `TriadView` | focus + prey + predator models | Three columns/panels in prey-left, predator-right order, focus centre and larger; reuses `MiniCard`/`MinionTile` at higher scale plus the shared viewport (zoom/pan). Prey/predator come from the server fields (`PlayerSnapshot.prey/predator`, falling back to `relationOf`), so ousted seats are skipped. |
| `SeatDetailView` | one seat model | Full detail: `MinionTile` grid with attachments, torpor, uncontrolled, piles; reuses the existing `PlayerBoard` internals where possible rather than a second implementation. |
| `SeatLegend` | seats | Auto-height table anchored top-left; row click focuses that seat. |

## 3. Data flow

`GamePage` (already has `game: GameSnapshot`, `seatOrder`, relation helpers)
-> `<RingBoard seats={ordered} viewer={me} audience={...} density onCardClick onRegionClick />`
-> `ringModel` (memoised per seat by snapshot identity) -> `ringPacker`
(memoised on `seats`, `size`, `selfIndex`; pan/zoom must not re-pack; that was a
real cost in the prototype) -> presentational leaves.

Audience differences are **props, not forks**: `audience: 'player' | 'judge' | 'spectator'`
controls (a) which seat is at the bottom (self, else first seat), (b) whether hand
counts / judge-only detail are shown (`game.judge`), (c) whether counter-bump and
context-menu actions are enabled (reusing `counterBumpFor` rules).
**Not** controlled by audience: the view switch. All three audiences get the same
Table / Triad / Seat control and the same Wedge / Panels choice. Judges and
spectators simply have no "self" seat, so focus starts on the edge holder or the
first seat and the triad relations are relative to the focused seat, not the viewer.

### Level transitions

`table` (C or D) -> click a seat -> focus it; switch (or double-click / Enter on the
wedge) -> `triad` of that seat -> click prey/predator to re-centre the triad ->
switch -> `seat`. In `seat` level, prev/next arrows step to the focused seat's prey
and predator. Zoom/pan state is per level (a table-level zoom must not carry into
seat level), but stays alive when switching back. Animate with a short shared-element
transition of the focused seat (CSS transform on the stage), no remounts.

## 4. Responsive mapping (prototype breakpoints to existing ones)

Existing hooks: `useIsMobile` (<768), `wideLayout` (>=1024), `midWide` (1024-1399).

| Prototype | Existing breakpoint | Board renderer | Notes |
|---|---|---|---|
| Ultra-wide 2560 | >=1024 (add `xl2` >= 2200) | `RingBoard` wedge, size ~900, legend left, talk rail on right as toggle | ring size derived from container, not hard-coded |
| Desktop 1920x1080 (Main) | >=1400 | `RingBoard` wedge C ~820, legend top-left, dock at bottom, talk rail right | dock grid stays; only the opponents pane is replaced |
| Tablet 1180 | 1024-1399 (`midWide`) | `RingBoard` size ~640, legend left | replaces the 2x2 `SeatGrid` fold; ring is inherently compact |
| Mid 768-1023 | mid | `SeatPanels` (D) or ring at container width | to decide: D reads better in a short/wide pane |
| Mobile | <768 | keep `SeatPager` initially; ring as an overview sheet | ring is unreadable at 390px; use it as a "table overview" bottom sheet with tap-a-wedge to jump the pager to that seat |

Size rule: `size = clamp(560, min(containerW, containerH) - gutter, 1000)` via
`useContainerSize`; zoom/pan stays independent of size.

**Variant D** (panel per seat) is the same `ringModel` output rendered in
`SeatPanels`: no packer, cards in a flat grid per panel, panels placed on the ring.
It is a user choice (Wedge / Panels toggle in `BoardViewSwitch`, remembered per
viewer), with the same viewport, zoom/pan and click interactions as C. Because it
avoids the packer it is also the graceful choice for seats with very many cards.
The default per breakpoint is set in Storybook review (proposal: wedge >=1024,
panels 768-1023).

## 5. Storybook plan (ring-first, fixtures from real games)

Fixtures: build `ringFixtures.ts` on top of `gameFixtures.ts` (`minion`, `player`,
`region`), seeded from the turn-9 board already used in the prototype (5 players,
mixed locked / contested / torpor / uncontrolled / attachments). Extend
`gameFixtures` only if a builder is missing (e.g. `capacity`, `locked`).

| Story file | Stories |
|---|---|
| `MiniCard.stories.tsx` | Ready, Locked, Contested, Torpor, WithAttachments, Hidden, Gauge vs Pips, Ally (life), Master location, long name wrap |
| `BoardViews.stories.tsx` | Same 5-player game at Table(wedge) / Table(panels) / Triad / Seat, for Player, Judge, Spectator; a stateful story wiring `BoardViewSwitch` with play tests for level switching and focus re-centre |
| `TriadView.stories.tsx` | Focus with live neighbours, focus next to an ousted seat (neighbour skipped), 2-player (prey = predator) |
| `RingSeat.stories.tsx` | Empty, Sparse, Full (stress packing), TorporRim, UncontrolledRim, Ousted |
| `RingBoard.stories.tsx` | Players 2/3/4/5; Player vs Judge vs Spectator; Zoom + pan; controlled `size` sweep 560/820/1000 |
| `SeatPanels.stories.tsx` | 2-5 players (variant D) |
| `SeatLegend.stories.tsx` | 2-5 players, ousted, edge holder |
| `RingResponsive.stories.tsx` | Same board at 390 / 768 / 1180 / 1920 / 2560 via viewport parameters |

Add Storybook `play` / interaction tests for zoom (wheel event), pan (right-button
drag), Reset, and wedge click callback. Include the a11y addon check on wedge
buttons (each wedge: `aria-label="Sable Voss: 3 ready, 1 torpor"`).

## 6. Testing

- `ringPacker.test.ts`: property tests over N=2..5 x sizes x card counts 0..24 —
  every placed card fully inside its wedge, no pairwise overlap (AABB/SAT), rim
  items ordered by arc. Port the prototype's node check scripts directly.
- `ringModel.test.ts`: region mapping, hidden handling, viewer rotation, ousted.
- `useRingViewport.test.tsx`: zoom-about-cursor invariant, clamps, pan bounds.
- Storybook interaction tests as above (`npm run test:storybook`).

## 7. Known gaps to close during the port

1. **Legibility:** header names are ~5-7px at bird's-eye; locked cards' text runs
   sideways. Needs a size floor, abbreviation rules, and hover/zoom detail
   (promote via `CardDialog` / tooltip on the zoomed view).
2. **Interactions:** none in the prototype. Map wedge click to a seat drawer (reusing
   `Region` / `PlayerBoard` contents), card click to `CardContextMenu`, and keep
   keyboard access (roving tabindex around the ring; wedges are buttons).
3. **Triad and Seat levels** were prototyped with older `SegCard` columns
   (`ZoomColumns`). Rebuild them as `TriadView` / `SeatDetailView` on the shared
   viewport, reusing `MinionTile` and `PlayerBoard` internals; do not port
   `ZoomColumns`.
4. **Text mode / density:** `TextModeContext` users (touch, tooltips off) need a
   non-graphical equivalent; keep `SeatGrid` + `PlayerBoard` as the text-mode board.
5. **Wheel/pan:** register the wheel handler non-passive on the viewport element
   (`addEventListener('wheel', ..., {passive:false})`); JSX `onWheel` is passive.
   Right-drag also needs `onContextMenu` suppression and a touch story (pinch /
   two-finger pan) for tablets.
6. **Theme:** prototype tokens map to the app theme (`theme.css`); seat colours
   should come from existing tokens, not new hex values.
7. **Prototype data drift:** Tablet/UltraWide/Mobile boards still hold older
   sample data; the stories, not those artboards, become the reference.

## 8. Delivery phases

| Phase | Scope | Exit |
|---|---|---|
| 0 | Fixtures + `ringModel`, `ringGeometry`, `ringPacker` with tests | Packer tests green, no UI |
| 1 | `MiniCard`, `MiniCardPlaceholder` + stories | Visual parity with artifact, locked/contested/torpor/hidden |
| 2 | `RingSeat`, `RingWedges`, `RingHub`, `RingBoard` (static, size prop) | 2-5 player stories match Main |
| 3 | `RingViewport` + `useRingViewport` (zoom/pan/toolbar) | interaction tests pass |
| 4 | `SeatLegend`, `SeatPanels` (D) with viewport parity, responsive stories | D matches C interactions; breakpoint matrix reviewed |
| 4b | `useBoardView`, `BoardViewSwitch`, `TriadView`, `SeatDetailView` | seamless Table/Triad/Seat switching for all 3 audiences in stories |
| 5 | Profile preference (migration, beans, Preferences UI) + wire into `GamePage` behind the flag; click-through to existing menus/dialogs; keyboard + a11y | usable alongside old grid, behind flag |
| 6 | Legibility pass, retire `SeatPager` on mobile, decide default | flag flip decision |

Phases 0-4 are pure additions under `game/ring/`; nothing in `GamePage` changes
until phase 5, so the branch stays low-risk and each phase is reviewable in
Storybook alone.

## 9. Open questions

Resolved: D is a user-selectable layout with full parity; three detail levels for all
audiences.

Also decided:
- **Layout preference (wedge / panels):** a persisted **profile preference**, read on
  first load of a game, with a **temporary per-game toggle** in `BoardViewSwitch`.
  Precedence: session toggle for this game > profile preference > width default
  (wedge >=1024, panels 768-1023). The toggle is held in component state /
  `sessionStorage` per game (not written to the profile) and is discarded on leaving
  the game; Preferences is the only place that persists it. Same model for the
  opt-in ring flag ("Board style: grid / ring").
  Backend: follows the `V23__player_theme.sql` / `imageTooltipPreference` precedent —
  new Flyway migration `V24__player_board_prefs.sql` (nullable `board_style`,
  `ring_layout` columns on `player`), fields on `NavBean` + `ProfileBean` + `api/types.ts`,
  a control in `pages/profile/Preferences.tsx`. Null = use the width default. Since
  `useNav` already loads on the game page, no extra request is needed.
- Ring rollout is **opt-in** per viewer; the existing grid stays the default.
- **Mobile:** the level model (Table / Triad / Seat) replaces `SeatPager`. Default
  level on mobile is Seat (own seat first), with the switch to reach Triad/Table.
  `SeatPager`'s swipe becomes prev/next on the focused seat (prey/predator order).
- **2-player games skip the Triad level** (switch shows Table / Seat only; neighbour
  arrows in Seat level go to the single opponent).

Still open: none blocking phases 0-4 (the preference plumbing lands in phase 5).

## 10. Status

| Phase | State |
|---|---|
| 0 layout engine + fixtures + tests | done |
| 1 `MiniCard`, `UncontrolledMarker` + stories | done |
| 2 static `RingBoard` (wedge, C) | done |
| 3 `RingViewport` zoom / pan (`viewportMath`, `useRingViewport`, `TableView`) | done — wheel (non-passive), right-drag, +/-/Reset, keyboard; no touch gestures yet |
| 4 `SeatLegend`, Panels (D: `panelLayout`, `SeatPanels`), `TableView` (layout switch + shared viewport), `boardPrefs.resolveLayout` | done |
| 4b `seatFocus`, `boardViewState` (reducer), `useBoardView`, `BoardViewSwitch`, `DetailCard`, `SeatDetail`, `TriadView`, `SeatDetailView`, `BoardView` | done — Triad / Seat are scrollable columns, not zoom/pan viewports (deliberate deviation from §2) |

Storybook / Tailwind notes learned during the port: in Storybook's *vitest* run the Tailwind
utilities layer is not applied (`@import must precede...` warning in `styles/tailwind.css`),
so anything a play test must measure (container sizes) uses inline styles, not utility classes.
Utility classes also failed to appear in the dev server for a freshly added class until a restart,
so story frames use inline styles.

## 11. Phase 5 — desktop conversion (partial)

Scoped down from the full phase-5 description: **judge / spectator opponents view only**,
wide layout only. A seated player's own board still uses the existing grid + dock —
see below for why, and what full seated integration would need.

Done:
- **Backend preference**: `V24__player_board_prefs.sql` (nullable `board_style`,
  `ring_layout` on `player`), `PlayerInfo`/`PlayerEntity`/`PlayerService`/`JolAdmin`,
  `NavBean` + `ProfileBean` fields, `PUT /jol/api/profile/board-view` (returns the
  updated `ProfileBean`, not 204 — see the bug note below).
- **Preferences UI**: a switch + ring-layout select in `pages/profile/Preferences.tsx`.
- **`GamePage.tsx`**: for `!me` (judge/spectator) at `wideLayout`, `#opponents` renders
  `BoardView` instead of `SeatGrid` when the resolved style is `'ring'`
  (`ring/useBoardStyle.ts`: session toggle > profile preference > `'grid'` default,
  same precedence model as the layout toggle). A small button switches board style
  for that visit only, next to `#opponents`.
- Verified live against a real Postgres-backed `quarkus:dev` instance (not just
  Storybook): logged in, toggled the Preferences switch and select, confirmed the
  round trip via `GET /profile` after each change, confirmed 400 validation for a
  bad value. Found and fixed a real bug this way (see below) that no amount of
  Storybook or unit testing would have caught, since it only shows up against the
  real endpoint contract.

**Bug found and fixed during live verification**: `setBoardView` initially returned
`Response.noContent()` (204, mirroring `setTheme`), but `Preferences.tsx` reads the
updated `Profile` bean from the response to update the on-page switch — a 204 body
parses as `undefined`, and `queryClient.setQueryData(key, undefined)` is a documented
no-op in TanStack Query, so the switch silently stopped reflecting its own saves (the
POST succeeded server-side the whole time). `setTheme` gets away with 204 because its
only caller never reads the `profile` cache. Fixed by returning `ProfileBean` directly,
matching `setPreferences`/`setEdgeColor` — the pattern to follow for any endpoint whose
result needs to redraw the page that called it, not `setTheme`'s.

**Not done — deferred, not silently dropped:**
1. **Seated players.** The ring's "you at the bottom" framing needs your own seat
   included in the model (`buildRingModel` computes prey/predator relative to it),
   so it can't cleanly slot into a `SeatGrid`-only replacement for a seated player —
   it would need to replace the opponents pane AND the board half of `dockGrid`'s
   left column, keeping only the hand + command band in the dock. That's a real
   restructure of `GamePage`'s most complex layout region, not done here.
2. **Card / marker clicks.** The existing grid's click-to-act menu is itself gated
   on `isSeatedPlayer` (`Region.tsx`'s `clickMode`) — a non-seated judge already gets
   **no** click-to-act menu on the classic grid today (judges act via the free-text
   command form in the rail instead). The ring matches that today: clicking a card
   or marker does nothing for `!me`; clicking a seat still switches Table/Triad/Seat
   level, a ring-only addition with no grid equivalent. Wiring real card actions
   (`coordinate` must be the card's index in the *original* `RegionSnapshot.cards`
   array — `ringModel`'s READY sort must not shift it) becomes relevant once seated
   integration happens, not before.
3. **Mobile.** `SeatPager` is untouched.
4. **RESEARCH region / archival piles.** The ring only models READY/TORPOR/
   UNCONTROLLED (matching the plan); `PlayerBoard`'s RESEARCH region and its
   `PilesFooter` (ash heap/library/crypt/RFG/hand counts) have no ring equivalent yet.

## 12. Player-conversion scaffolding (data model + click plumbing — no GamePage change)

Completed ahead of the actual seated-player GamePage restructure (§11 item 1, still
not started — that one needs a product decision on YourSeatDock's shape first).
Everything below is additions under `game/ring/`; nothing outside it changed.

- **Coordinate correctness** (`cardView.ts`, `ringModel.ts`): every `CardView` /
  `MarkerView` now carries `coordinate` — its 1-based index in the *original*
  `RegionSnapshot.cards` array, assigned before `ringModel`'s READY kind-sort
  (vampire → ally → location) can reorder the display. A command built from a
  resorted position would silently target the wrong card the first time a READY
  region mixes kinds; `ringModel.test.ts`'s `coordinate` suite constructs a
  region where the sort actually moves a card and asserts the coordinate stays
  put. This was a real, previously-unnoticed bug risk, not speculative.
- **Click anchor plumbing** (`clickAnchor.ts`, new): a popover needs a screen
  position, real clicks report one, keyboard activation doesn't (`detail: 0`) —
  falls back to the element's own centre, the same fallback `MinionTile`'s
  keyboard path already uses. `MiniCard`, `UncontrolledMarker`, `DetailCard`
  all now call `onClick(view, anchor)` / `onClick(marker, anchor)`; `RingSeat`,
  `SeatPanels`, `SeatDetail` propagate it; `RingBoard`, `TableView`, `BoardView`
  widen `onCardClick`/`onMarkerClick` to carry it through. `onSeatClick` and
  `onCounter` are unaffected — a seat click never needed a popover position,
  and the counter stepper isn't one.
- **RESEARCH region** (`ringModel.ts`): `RingSeatModel.research: CardView[]`,
  usually empty. Rendered at Triad/Seat (`SeatDetail`) using the same
  `DetailCard` as READY/TORPOR; **not yet laid out inside the wedge/panel** —
  doing that needs packer/panel-layout changes (a third bucket alongside ready
  and the rim), not just component wiring, and research is rare enough that
  Triad/Seat coverage handles it for now. Flagged, not silently dropped.
- **Archival piles** (`ringModel.ts`, `detail/PileSummary.tsx`): `RingSeatModel.piles`
  (ash heap / library / crypt / removed-from-game counts — hand is already
  `handCount`). `PileSummary` is the ring's read-only counterpart to
  `PilesFooter`: count chips only, shown at Triad/Seat, **no expand-to-cards**
  interaction (that stays a classic-board affordance; opening a pile's actual
  contents from the ring is a follow-up, not scaffolding). Renders nothing when
  every pile is empty, same restrained "footer" role `PilesFooter` itself has.
- **Counter bump**: already fully wired end to end since phase 4b
  (`BoardView` → `SeatDetail` → `DetailCard`'s −/+ buttons) — nothing to add.
- **Deliberately not built this pass** (still open, listed for when GamePage
  wiring actually starts): an inline lock/unlock quick-action on `MiniCard`
  (today only reachable via the eventual `CardContextMenu`, which itself isn't
  wired to `GamePage` yet either); laying RESEARCH out inside the wedge/panel;
  `influencePriority`'s region-reorder-to-top behaviour has no ring analogue.

Verified: `tsc --noEmit` clean, `oxlint` clean (only the pre-existing ref-during-
render warnings), all 564 frontend tests pass (169 under `game/ring`), and the
research/pile rendering checked visually in Storybook (`BoardLevels — Player —
Seat`, dark theme).

## 13. Seated-player restructure (§11 item 1 — done)

Adopted the prototype's own answer (`Main.dc.html`'s dock), not a fresh design: a
single full-width strip along the foot of the column — identity, Pool/VP, a
horizontal hand strip, and the command band in one row — replacing YourSeatDock's
board *and* HandDock entirely. Your own board is never shown twice: at Table level
you're a wedge/panel like everyone else (BoardView takes the full roster, not just
`others`); at Triad/Seat level, `SeatDetail` shows it in full.

**Responsive fallback** (the piece the prototype's single 1920px mock didn't cover):
at the `midWide` band (1024–1399px) the strip's `compact` prop switches it from
`flex-nowrap` to `flex-wrap` — the row becomes two or three as needed, nothing is
cut or hidden. Checked in Storybook at 560px: identity+stats, hand, and commands
each keep their own line, all still visible. No new breakpoint constant added —
reuses the `midWide` boolean `GamePage` already computes.

**New**: `PlayerDock.tsx` (the strip — built from existing pieces: `HandStrip`,
`DockCommandStack` variant="dock", no new hand/command components).

**`GamePage.tsx` changes** (seated, `wideLayout`, `boardStyle === 'ring'` only —
`boardStyle === 'grid'` stays pixel-identical to before):
- `BoardView` replaces the opponents grid AND YourSeatDock's board; `PlayerDock`
  replaces the rest of `dockGrid`. No resizable split in ring mode (`PlayerDock` is
  fixed-content, not a second scroller).
- Real click-to-act wiring, not skipped this time (judges/spectators still get
  none, matching Region.tsx's own `isSeatedPlayer` gate — see phase-11 note): ring
  card/marker clicks build a `TableCardContext` from `CardView.region` +
  `.coordinate` and call the *same* `handleTableCardClick` / `counterBump` the
  classic grid uses — same `CardContextMenu`, same command builders, nothing
  duplicated. One real nuance caught while wiring this: RESEARCH's `clickMode` in
  `Region.tsx` is `'play'`, not `'action'` — and only on your own seat (an
  opponent's/neighbour's RESEARCH isn't actionable at all in the classic grid
  either). `onRingCardClick` branches on `view.region === 'RESEARCH'` to route to
  `handlePlayCardClick` instead, and no-ops for anyone else's research — this
  would have been a silent behavioural bug (wrong modal, or a menu appearing where
  the classic grid shows none) if ported over uniformly.
- A per-game "Switch to classic/ring board" toggle sits above the opponents pane,
  now for seated players too, same as the judge/spectator one already had.

**Verification:** `tsc --noEmit` clean, `oxlint` clean, all 570 frontend tests pass
(no test file broke from the `CardView.region` / `MarkerView.source` additions
beyond one story fixture). `PlayerDock` checked visually in Storybook, both wide
and the 560px compact wrap. **Not checked live in the real app** — the dev
Postgres has no active game (devseed only seeds players, not games/decks) and
starting one to check this specifically was judged not worth the detour given
`tsc`'s prop-level checking against the real component signatures already covers
the main new risk (wiring, not visuals — those were already proven in Storybook
component-by-component). Flagging this the same way the judge/spectator pass did.

**Deliberately not done, staying in scope with what was asked:**
- Judge's own dock (`judgeCommandBar`, in the talk rail) — untouched; the
  prototype's judge-mode bottom strip was not built.
- Laying RESEARCH out inside the wedge/panel (still Triad/Seat only, per §12).
- `influencePriority` (float UNCONTROLLED to the top on your Influence-phase
  turn) — `ringModel` still doesn't reorder regions; lost when `YourSeatDock`
  (which supported it) is replaced by the ring in ring mode.

## 14. Live verification (§13's "not checked live" — now done)

Got a real 2-player Standard game running (`quarkus:dev`, real Postgres, not
Storybook) and drove it as a seated player, logged in as Player1:

- **Table level, Wedge and Panels**: both render correctly for a live 2-player
  game — Panels by default (the ring's own container was narrower than 1024px
  even though the browser window was wide, confirming layout resolution is
  correctly container-width-based, not a `window` media query), Wedge after
  toggling (a 2-seat ring is a half-circle, predator right / prey left, as
  designed).
- **Table → Seat**: real pool/VP/hand counts, a real "Nothing in play" READY
  section, five real uncontrolled crypt vampires with correct blood/capacity,
  and a real pile summary (`ASH 0 · LIBRARY 83 · CRYPT 7 · RFG 0`) — all pulled
  from the actual game state, not a fixture.
- **Click-to-act, for real**: clicked an uncontrolled marker at both Table and
  Seat level — both opened the real `CardContextMenu` with the right card
  identity and the real blood-counter/turn-face-down/burn/remove-from-game
  actions. Used the menu's own stepper to add a blood counter; the chat log
  confirmed the server's own account of what happened: *"Player1 adds 1 blood
  to card #5 in their uncontrolled region"* — **card #5**, matching this
  card's real position in the original UNCONTROLLED array, not a display
  position. This is the coordinate-correctness fix from §12 confirmed against
  the real engine, not just a unit test.
- **The board-style toggle**: switched to the classic grid (full `PlayerBoard`
  + `HandDock`, real card art, untouched) and back to the ring — state (the
  blood counter just added) was intact across the switch both ways.
- **`PlayerDock`**: real hand cards, real Pool/VP, real prey/predator names,
  Quick Actions and the command input all present and consistent with the
  classic dock's own data.

No bugs found in this pass — the coordinate/region work from §12 held up
against the real engine on the first try. Left the game running in the dev
Postgres (throwaway, wiped on next `quarkus:dev` restart); no cleanup done.
