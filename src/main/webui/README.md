# frontend (src/main/webui)

The React SPA for [V:TES Online](https://deckserver.net) — replaces the legacy JSP/jQuery/DWR client entirely. Built with Vite + TypeScript + React Router, served directly by Quarkus via the Quinoa extension (see `../../../CLAUDE.md` for how the backend wires this in). Lives at `src/main/webui` — Quinoa's own default location for a project's frontend, not a repo-root `frontend/` directory the way it did before the Quarkus migration.

For build/run commands, environment variables, and the overall system architecture (backend + frontend together), see the root [`CLAUDE.md`](../../../CLAUDE.md). This file covers only the frontend project itself.

## Development

```bash
npm install    # required once before any standalone script here (storybook, test, test:e2e, …)
npm run dev
```

> `quarkus:dev` from the repo root drives its own frontend install/build via Quinoa, so running the app doesn't populate a `node_modules` you can rely on here. Run `npm install` in this directory before using any of the scripts below directly.

Requires the backend running instead, from the repo root — `JOL_DB_PASSWORD=jol ENABLE_CAPTCHA=false ./mvnw quarkus:dev`, which starts this frontend's own `npm run dev` as a subprocess automatically (via Quinoa) and proxies to it. Running `npm run dev` standalone from here is only useful for iterating on the frontend build config itself; for actually using the app, run `quarkus:dev` from the repo root instead.

## Scripts

| Script | Purpose |
|---|---|
| `npm run dev` | Vite dev server (started automatically by `quarkus:dev` via Quinoa — see above) |
| `npm run build` | Type-check (`tsc -b`) then production build to `dist/` (Quinoa's default expected location) |
| `npm run lint` | `oxlint` |
| `npm run test` | Unit/component tests (Vitest) + Storybook interaction tests — see below |
| `npm run test:storybook` | Just the Storybook interaction tests (`vitest run --project=storybook`) |
| `npm run test:e2e` | End-to-end tests (Playwright) — see below |
| `npm run storybook` | Storybook dev server (`http://localhost:6006`) for browsing/authoring component stories |
| `npm run build-storybook` | Static Storybook build, to `storybook-static/` (not deployed anywhere yet — a local/CI artifact only) |
| `npm run preview` | Preview a production build locally |

## Testing

**Unit/component tests** (Vitest + React Testing Library, `src/**/*.test.ts(x)`) run in `jsdom` — see `vitest.config.ts`'s `unit` project and `src/test/setup.ts`. Pure logic (`coordinates.test.ts`, `cardCommands.test.ts`) and component behavior (`LoginPage.test.tsx`, `CommandForm.test.tsx`, `ReplacePlayer.test.tsx`) both live alongside the code they test. `api/client.ts`'s `api` object is the usual mock seam — components call it directly rather than through a context/DI layer, so `vi.mock('../../api/client')` is enough.

**Storybook + interaction tests** (`src/components/*.stories.tsx`) cover the shared components in `src/components/` — `Card`, `Modal`, `TopBar`, `DeckPreview`, the toast/dialog hosts, etc. Each story renders one component in isolation; some also carry a `play` function (from `storybook/test`) that drives it with `userEvent` and asserts on the result — e.g. `Modal.stories.tsx`'s `ClosesOnEscape`, or `DialogHost.stories.tsx` round-tripping a real `confirmDialog()` call. `vitest.config.ts`'s `storybook` project (the `@storybook/addon-vitest` plugin) runs every story as a test in a real headless Chromium via Playwright:

```bash
npm run test:storybook   # just the stories
npm run storybook        # interactive — browse stories, re-run/debug a play function
```

`.storybook/preview.tsx` wraps every story in a `QueryClientProvider` + `MemoryRouter` (most components need at least one) and loads the same Bootstrap/theme CSS the real app does — see its comments and `.storybook/main.ts`'s `staticDirs` for how, since this app doesn't bundle that CSS via Vite (`legacyStyles.ts` loads it at runtime instead). A component that reads a react-query cache the backend would normally fill (e.g. `TopBar`'s `useNav()`) gets a story-local decorator that seeds the cache with `queryClient.setQueryData(...)` instead of mocking a fetch — see `TopBar.stories.tsx`.

**End-to-end tests** (`e2e/*.spec.ts`, Playwright) drive a real browser against the actual app — one `quarkus:dev` process, not a mocked environment. `playwright.config.ts`'s `webServer` entry starts it automatically:

```bash
npm run test:e2e
```

Requires local Postgres already running (`docker compose -f local-docker-compose.yml up -d db` from the repo root) — unlike the old Tomcat/JSON-file setup, state now lives there, not in a directory Playwright can copy/discard per run. The `webServer` command resets it to the `Player1`..`Player5` fixture set (`./load-test-fixtures.sh`) before every run instead, so these specs — which submit real chat/commands, mutating that data — always start from known state. `e2e/game.spec.ts` drives "Test Game," a fixture game all five are already registered in.

Cold start (first Maven/npm dependency resolution + Quarkus boot) can take a couple of minutes; subsequent runs reuse the already-running server if one is up (`reuseExistingServer: true`) — kill anything on `:8443` first if you want a guaranteed-clean run.

## Project layout

- `src/App.tsx` — the provider stack only (`QueryClientProvider` → `BrowserRouter` → app-wide overlay hosts). Routing lives in `src/app/`.
- `src/app/` — top-level composition: `AppRoutes` (login vs. authenticated split) → `AuthGate` (holds the shell back until `GET /nav` confirms a session) → `AppShell` (nav bar + the lazy-loaded routed page area). The `<Route path>` table reads its patterns from `ROUTE_PATHS` in `src/routes.ts` — the single source of truth, shared with the `path*()` link helpers there.
- `src/pages/` — one **entry component per route**, always as a sibling file named for the route (`LobbyPage.tsx`, `GamePage.tsx`, `DeckPage.tsx`, `HelpPage.tsx`, ...), each with a same-named subdirectory for its own sub-components (`pages/game/`, `pages/lobby/`, `pages/deck/`, ...). Sub-components stay page-private; promote to `src/components/` only when a second page needs them.
- `src/api/` — `client.ts` (thin fetch wrapper used by everything), `types.ts` (hand-written mirrors of the Java response beans), `config.ts`, `apiBase.ts`, `useConnectivity.ts`, `mutate.ts` (`runRequest` — fire-and-forget with a shared failure toast), `useInvalidate.ts` (`() => queryClient.invalidateQueries({ queryKey })` as a hook)
- `src/stores/` — app-global state as **module singletons** (`toast.ts`, `dialog.ts`, `connectivity.ts`, and the WS `socket.ts` pub/sub), each `module-level state + listener set + useSyncExternalStore`. This is the deliberate alternative to React context here: they must be callable from plain event handlers and `.catch()` blocks with no component-tree position of their own. Their render surfaces (`DialogHost`, `ToastHost`) live in `src/components/` and are mounted once in `App.tsx`.
- `src/ws/` — the `useGameSocket` / `useQueryInvalidation` hooks that translate WS push signals into re-fetches (the socket itself is `src/stores/socket.ts`)
- `src/auth/` — `useNav` / `useNavAuthState` / `useAuth` — the authenticated shell's session + nav state (current player, per-game buttons, etc.), backed by the react-query cache entry for `GET /jol/api/nav` (no context/provider — see `useNav.ts`)
- `src/components/` — **shared, cross-page UI**: `TopBar`, `Modal` (+ dialog/toast hosts), `TabBar`, `CountryFlag`, `Card`, `ChunkErrorBoundary`, `UpdateBanner`, `SplitLayout`, `DeckPreview` — each with a co-located `*.stories.tsx` (see Testing above). Bootstrap-styled, except `DeckPreview` which is on tokens.
  - `src/components/ui/` — the **Tailwind component kit** (`jt:`-prefixed classes, jol-quarkus tokens): `Button`, `Input`, `Select`, `Textarea`, `Switch`, `Card`, `Badge`, `Panel`, `Modal`, `EmptyState`, `Spinner`, `SectionLabel`, `FormFeedback` (`FieldHint`/`InlineAlert`), `MasterDetailView`, `icons/`. Every route is on the kit except **`/game`** (`pages/game/*`), which stays Bootstrap: it's deeply coupled to server-emitted CSS class strings (`card-name`/`.icon` from ParserService) and game-specific iconography. `TournamentAdminPage` (`pages/tournamentAdmin/*`) also still uses the Bootstrap `Card`/`SplitLayout` pending its own migration. Migrate a component across on touch; don't add new Bootstrap primitives. Name overlaps that persist while both sets exist: `components/ui/Card` vs `components/Card`, `components/ui/Modal` vs `components/Modal`, `components/ui/Panel` vs `pages/game/GamePanel` (game-only chrome).
- `src/hooks/` — small reusable hooks (card tooltips, dropdown/tooltip open-close state, submit guard, `useDebouncedCallback`)
- `src/utils/` — framework-agnostic helpers with no React or domain dependency (`relativeTime.ts`). Domain- or page-specific helpers stay next to their page (`pages/main/chatFormatting.ts`, `pages/game/coordinates.ts`, ...).
- `src/content/help/` — MDX help content + `meta.ts` (the ordered section list that drives both the help nav and its routes). The presentation side — `HelpPage.tsx`, `pages/help/HelpSection.tsx`, `pages/help/components/` — lives under `src/pages/` like any other page; help is the one feature split content-vs-presentation this way.
- `src/styles/` — global CSS only: `fonts.css`, `theme.css` (tokens/typography), `tailwind.css` (the `jt:` utilities layer), `card-visuals.css` (the shared card-name/icon vocabulary). Component- and page-scoped CSS is co-located instead (`pages/GamePage.css`, `pages/game/Clan.css`, `components/TopBar.css`). Third-party/theme sheets (Bootstrap, dark-mode) are fetched at runtime by `src/legacyStyles.ts`, not bundled.
- `.storybook/` — Storybook config (`main.ts`, `preview.tsx`) — see Testing above
- `e2e/` — Playwright specs (see Testing above)

## Expanding the Oxlint configuration

If enabling type-aware lint rules, install `oxlint-tsgolint` and edit `.oxlintrc.json`:

```json
{
  "$schema": "./node_modules/oxlint/configuration_schema.json",
  "plugins": ["react", "typescript", "oxc"],
  "options": {
    "typeAware": true
  },
  "rules": {
    "react/rules-of-hooks": "error",
    "react/only-export-components": ["warn", { "allowConstantExport": true }]
  }
}
```

See the [Oxlint rules documentation](https://oxc.rs/docs/guide/usage/linter/rules) for the full list of rules and categories.
