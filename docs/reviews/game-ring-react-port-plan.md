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
