# Game table page — UI review brief

Brief for a fresh-context UI/design review of the in-game table page (`GamePage`),
the last page in the page-by-page review pass. Written so a new session can start
cold.

## 1. Scope

In scope:

- `src/main/webui/src/pages/GamePage.tsx` + `src/main/webui/src/pages/game/**` + `GamePage.css`
- the backend it talks to: `net.deckserver.rest.GameStateResource` (`/game/{id}/view`,
  `view/submit`, `view/end-turn`, `judge-request*`), `GameActionResource`,
  `net.deckserver.rest.bean.GameSnapshotFactory` / `GameSnapshot`,
  `net.deckserver.game.model.*` (`JolGame`, `DoCommand`, `GameModel`, `GameView`)

Out of scope:

- Tournament / Tournament Admin (being rebuilt from scratch)
- Every other page — already reviewed this pass (Home, Watch, Admin, Profile, Help,
  Judge, Login)

## 2. Review method (keep consistent with the prior pages)

- Read the page + its components + the backend beans/resources it uses. Then view it
  live in Chrome, **light and dark**, at desktop width and a narrow width.
- Produce a **ranked findings list** (short title, what's wrong, why it matters, fix
  sketch). Separate "clearly right, small" from "needs a direction call" from
  "product decision — flag only".
- **Do not implement until the user picks.** Then implement, verify the change live in
  Chrome, and run:
  - backend: `JAVA_HOME=…corretto-21… ./mvnw -q compile`
  - frontend: `npx tsc --noEmit -p .`, `npm run lint` (expect 0 errors — note any
    pre-existing warnings, don't chase them), `npm run test`
- Don't spawn subagents. Don't port the old JSP/DWR UI verbatim — design for the
  content, keep the app's theme.

## 3. Environment

- Build needs Java 21:
  `JAVA_HOME=/Users/shannon/Library/Java/JavaVirtualMachines/corretto-21.0.9/Contents/Home`
  (Lombok breaks on the default Java 25).
- Dev server — run from the repo root, and `cd` in the **same** command (the Bash
  tool's cwd can reset between calls):

  ```
  cd /Users/shannon/IdeaProjects/jol && \
  JAVA_HOME=/Users/shannon/Library/Java/JavaVirtualMachines/corretto-21.0.9/Contents/Home \
  JOL_DB_URL=jdbc:postgresql://localhost:5432/jol JOL_DB_USER=jol JOL_DB_PASSWORD=jol \
  ENABLE_CAPTCHA=false \
  nohup ./mvnw -q quarkus:dev -Dquarkus.profile=prodlike > /tmp/jol-dev.log 2>&1 &
  ```

  Serves `https://localhost:8443/jol` (self-signed cert). Quinoa proxies the
  frontend's own `npm run dev` (vite, port 5173). Bean/record changes trigger a full
  ~17s live reload.
- **Stopping the server:** `kill -9` on the Maven launcher orphans the forked Quarkus
  JVM (a grandchild), which keeps port 8443 and makes a fresh start silently fail to
  bind. Always:

  ```
  pkill -9 -f "quarkus:dev"; pkill -9 -f "classworlds.launcher"
  # then confirm both are clear:
  lsof -nP -iTCP:8443 -sTCP:LISTEN      # expect no rows
  ps aux | grep -iE "jol-legacy|quarkus:dev" | grep -v grep   # expect none
  ```

  If you stop it, restart it before finishing.
- Local Postgres: docker container `jol-db` (postgres:16, db/user/pw all `jol`, port
  5432) — a **disposable prod snapshot** (~915 players, 206 games, 5 tournaments).
  Safe to mutate for testing; restore afterward and report the final counts.
  The app **rewrites `player` rows to the DB on graceful shutdown**, clobbering SQL
  changes with its stale in-memory copy — set player state via SQL only while the
  server is stopped, or via the API.
- Test creds: `ShanDow` (admin), `Stolas`, `acbishop` — all password `password`.
  ShanDow is seated in active games `01M18SA96Q3585WDN99K5FPVHV` and
  `6bffd3f9-7ecb-4f6f-a711-79d873b9ee88` — use one of these to view a live table
  (`https://localhost:8443/jol/game/<id>`). Verify the id is still live first.
- Chrome automation quirks seen this session:
  - `navigate` sometimes leaves the tab un-screenshottable ("browser-internal or
    unparseable URL") — create a fresh tab with `tabs_create_mcp` and re-navigate.
  - Window resize does not reliably change the render viewport; screenshots come back
    at a fixed downscaled size. Test narrow layouts by reasoning from the CSS +
    `read_page`, or accept the limitation.
  - Prefer `read_page` element refs + `form_input` over coordinate clicks (the panel
    shifts between calls).
  - Theme: `localStorage['jol-theme'] = 'dark' | 'light' | 'system'`, then reload.

## 4. Page map

- `GamePage.tsx` (~203 lines) — orchestrator. Snapshot query `/game/{id}/view`;
  `useGameSocket` for push → refetch; click-to-act modals (`PlayCardModal`,
  `CardActionModal`, `TargetPicker`); board ref + `coordinates.ts` hit-testing;
  history / deck drawers.
- `game/CommandForm.tsx` (~284) — free-text command bar + quick-command / quick-chat
  modals; `useSubmitGuard`. Keeps command-validation errors in **local** state on
  purpose (see §5).
- Board: `PlayerBoard.tsx`, `Region.tsx`, `Card.tsx` / `CardSimple` / `CardHidden` /
  `CardImage`, `HandStrip.tsx`; `Clan` / `Path` / `Sect` badges (+ their `.css`).
- Side panels: `GameChatPanel` / `GameChatLog`, `HistoryPanel`, `NotesPanel`,
  `DeckPanel`, `GamePanel`.
- Modals: `PlayCardModal`, `CardActionModal`, `QuickCommandModal`, `QuickChatModal`,
  `JudgeRequestModal` (the modal itself was left unchanged during the Judge-page
  review).
- Logic + tests: `cardCommands.ts`, `coordinates.ts`, `useShowCommands.ts` — all have
  `.test` files; mind them when changing behaviour. Also `CommandForm.test.tsx`,
  `GameChatLog.test.tsx`.
- Backend: `GameStateResource`, `GameActionResource`, `GameSnapshotFactory`
  (enriches only the viewer's own HAND / RESEARCH cards with play-mode data),
  `JolGame` / `DoCommand` / `GameModel` / `GameView`.

## 5. Known constraints / gotchas for this page

- **Self-refetch overwrite:** a state-saving action also notifies the actor's own
  socket, so any state derived only from a full-snapshot refetch (not the mutating
  request's own response) can be overwritten within the same round trip.
  `CommandForm`'s `status` field keeps server-side command-validation errors in local
  component state for exactly this reason — do not "simplify" that away.
- WebSocket carries **signals, not payloads** today — the client re-fetches the REST
  endpoint on each push (`ws/useGameSocket.ts`, `ws/useQueryInvalidation.ts`).
  (See §6a for the item to revisit this.)
- `GameView` (per-player region collapse state) is mostly client-owned now — see
  `PlayerBoard.tsx`'s `ALWAYS_COLLAPSED` comment.
- Card images / tooltip HTML are static assets from `static.deckserver.net` (served
  locally in dev by Vite's `serveCardAssets.ts`). Play-card-modal data
  (`modes` / `multiMode` / `doNotReplace` / `preamble` / `cost`) rides on
  `CardSnapshot` for the viewer's own HAND / RESEARCH cards only — the client does not
  fetch a per-card definition.
- `WebSocketRegistry` identity-confusion bug and a CDI proxy field-access bug were
  both fixed during the Quarkus migration and are documented at the relevant classes —
  worth knowing before assuming the WS/identity code is naive.

## 6. Investigation / discussion items (architectural — raise findings before implementing)

### 6a. WebSocket payloads instead of signals

Today `/ws/updates` carries only a "something changed" signal; the client re-fetches
the full REST snapshot on every push. Investigate pushing the actual game-state update
over the socket — a delta or the full `GameSnapshot` — so the common case skips the
extra HTTP round-trip.

Consider: bandwidth vs. round-trip latency; delta vs. full snapshot (versioning /
dropped-frame recovery); keeping a single render path (a stale tab must still be able
to full-fetch); the per-viewer enrichment `GameSnapshotFactory` does (hand/research
only) means the payload is viewer-specific, not broadcastable as-is; and the
self-refetch-overwrite constraint in §5. Pairs naturally with 6b.

### 6b. Laggy interface — opportunistic (optimistic) updates

Users report a visible delay between clicking a button and the UI updating. Likely
chain: click → POST → server mutate → WS signal → snapshot refetch → re-render —
several round-trips before anything moves.

First **measure** the real latency (network panel, request timings) on the worst
offenders. Then investigate optimistic UI: apply the expected change locally on click,
reconcile or roll back on the server response. Candidates: end turn, play card,
lock/unlock, counter +/-, chat send. Note the interaction with 6a (payloads would also
shrink this) and that `CommandForm` deliberately keeps validation errors in local
state because the self-triggered refetch would clobber them.

### 6c. Image-free / text-only card UI (discussion item — do not implement from this brief)

The profile has an `imageTooltipPreference` setting (now toggleable after the Profile
review) but nothing in the game UI reads it. To honour "images off" we need a new
mechanism that presents the same card information — clan, capacity, disciplines, type,
cost, card text — in a compact **text** form instead of the image tooltip/modal.

Open questions: what fields to surface and how dense; the surface (inline expandable
row? text tooltip? docked detail panel?); data source (`CardRegistry` has full
structured data; `CardSnapshot` already carries some for the viewer's own cards,
others would need enriching); how it threads through `Card` / `CardImage` /
`CardSimple` / `CardHidden`; the accessibility upside. Produce options, not a patch.

## 7. Recurring standards from the prior pages — apply the same

- Primary action = `Button variant="primary"`, not `secondary`.
- One save-feedback pattern per surface — see `pages/profile/saveState.ts` +
  `pages/profile/SaveNote.tsx` (`useSave()` + `<SaveNote>`).
- Timestamps: `adminTimestamp` (UTC, `3-Aug-26 14:05 UTC`) for records;
  `relativeTime` for "live" freshness.
- Card-name references get a visible affordance on pages that are about cards
  (`a.card-name` dotted underline — see `HelpSection.css` / `JudgePage.css`
  `.help-prose` / `.judge-prose`).
- Cap the reading measure on wide screens (`max-w-4xl` used elsewhere); prefer
  count-aware panel titles over titles that just restate the tab.
- Theme-aware CSS only, via `--jt-*` tokens / semantic Tailwind classes; always verify
  dark mode.
- For any free-typed entity reference, use a datalist/autocomplete +
  a `resolvePlayerName`-style resolve-and-guard (see `pages/admin/adminControls.tsx`).
- Lint: mixing a hook/helper export with a component in one `.tsx` trips
  `react-refresh/only-export-components` — split into a `.ts` + `.tsx` pair.

## 8. State at handoff

- Everything from the review session is **uncommitted** on branch `feature/quarkus`:
  parked items (stats sticky first column, "Elapsed"/drop-seconds relabel),
  Profile P1–P7, Help H1–H5, Judge J1–J7, Login L1–L10 (incl. the `Recaptcha` →
  `Turnstile` backend rename), plus the earlier Admin games-tab / rollback-modal and
  Home games-list work.
- No Game-page changes have been made.
- Dev server left running with `ENABLE_CAPTCHA=false`; DB at ~915 players (any
  test rows created during the Login review were deleted).
