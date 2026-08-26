# frontend (src/main/webui)

The React SPA for [V:TES Online](https://deckserver.net) — replaces the legacy JSP/jQuery/DWR client entirely. Built with Vite + TypeScript + React Router, served directly by Quarkus via the Quinoa extension (see `../../../CLAUDE.md` for how the backend wires this in). Lives at `src/main/webui` — Quinoa's own default location for a project's frontend, not a repo-root `frontend/` directory the way it did before the Quarkus migration.

For build/run commands, environment variables, and the overall system architecture (backend + frontend together), see the root [`CLAUDE.md`](../../../CLAUDE.md). This file covers only the frontend project itself.

## Development

```bash
npm install
npm run dev
```

Requires the backend running instead, from the repo root — `JOL_DB_PASSWORD=jol ENABLE_CAPTCHA=false ./mvnw quarkus:dev`, which starts this frontend's own `npm run dev` as a subprocess automatically (via Quinoa) and proxies to it. Running `npm run dev` standalone from here is only useful for iterating on the frontend build config itself; for actually using the app, run `quarkus:dev` from the repo root instead.

## Scripts

| Script | Purpose |
|---|---|
| `npm run dev` | Vite dev server (started automatically by `quarkus:dev` via Quinoa — see above) |
| `npm run build` | Type-check (`tsc -b`) then production build to `dist/` (Quinoa's default expected location) |
| `npm run lint` | `oxlint` |
| `npm run test` | Unit/component tests (Vitest) |
| `npm run test:e2e` | End-to-end tests (Playwright) — see below |
| `npm run preview` | Preview a production build locally |

## Testing

**Unit/component tests** (Vitest + React Testing Library, `src/**/*.test.ts(x)`) run in `jsdom` — see `vitest.config.ts` and `src/test/setup.ts`. Pure logic (`coordinates.test.ts`, `cardCommands.test.ts`) and component behavior (`LoginPage.test.tsx`, `CommandForm.test.tsx`, `ReplacePlayer.test.tsx`) both live alongside the code they test. `api/client.ts`'s `api` object is the usual mock seam — components call it directly rather than through a context/DI layer, so `vi.mock('../../api/client')` is enough.

**End-to-end tests** (`e2e/*.spec.ts`, Playwright) drive a real browser against the actual app — one `quarkus:dev` process, not a mocked environment. `playwright.config.ts`'s `webServer` entry starts it automatically:

```bash
npm run test:e2e
```

Requires local Postgres already running (`docker compose -f local-docker-compose.yml up -d db` from the repo root) — unlike the old Tomcat/JSON-file setup, state now lives there, not in a directory Playwright can copy/discard per run. The `webServer` command resets it to the `Player1`..`Player5` fixture set (`./load-test-fixtures.sh`) before every run instead, so these specs — which submit real chat/commands, mutating that data — always start from known state. `e2e/game.spec.ts` drives "Test Game," a fixture game all five are already registered in.

Cold start (first Maven/npm dependency resolution + Quarkus boot) can take a couple of minutes; subsequent runs reuse the already-running server if one is up (`reuseExistingServer: true`) — kill anything on `:8443` first if you want a guaranteed-clean run.

## Project layout

- `src/pages/` — one entry component per route (`LobbyPage.tsx`, `GamePage.tsx`, etc.), each with a same-named subdirectory for its own sub-components (`pages/game/`, `pages/lobby/`, ...)
- `src/api/` — `client.ts` (thin fetch wrapper used by everything), `types.ts` (hand-written mirrors of the Java response beans), `config.ts`, `connectivity.ts`
- `src/ws/` — WebSocket client (`socket.ts`) and the `useGameSocket`/`useJolSocket` hooks that translate push signals into re-fetches
- `src/nav/` — `NavContext`/`useNav`/`useAuth` — the authenticated shell's nav state (current player, per-game buttons, etc.), sourced from `GET /jol/api/nav`
- `src/components/` — shared UI: `TopBar`, modal/dialog/toast hosts, `ChunkErrorBoundary`, `UpdateBanner`
- `src/content/help/` — MDX help content (see `@mdx-js/rollup` in `vite.config.ts`)
- `src/hooks/` — small reusable hooks (card tooltips, dropdown open/close state)
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
