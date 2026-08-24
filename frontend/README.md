# frontend

The React SPA for [V:TES Online](https://deckserver.net) — replaces the legacy JSP/jQuery/DWR client entirely. Built with Vite + TypeScript + React Router, bundled into the Java WAR at `/react/*` and served for every top-level view (see `../CLAUDE.md` for how the backend wires this in).

For build/run commands, environment variables, and the overall system architecture (backend + frontend together), see the root [`CLAUDE.md`](../CLAUDE.md). This file covers only the frontend project itself.

## Development

```bash
npm install
npm run dev
```

Requires the backend running alongside it (`JOL_DATA=src/test/resources/data ./mvnw tomcat9:run` from the repo root) — Vite proxies everything it doesn't own itself (the REST API, WebSocket, static card assets) through to Tomcat on `:8080`. See `vite.config.ts`'s top comment for exactly which paths are frontend-owned vs. proxied, and why dev runs over HTTPS with a self-signed cert.

## Scripts

| Script | Purpose |
|---|---|
| `npm run dev` | Vite dev server (HTTPS, proxies to Tomcat) |
| `npm run build` | Type-check (`tsc -b`) then production build to `../target/react-dist` |
| `npm run lint` | `oxlint` |
| `npm run test` | Unit/component tests (Vitest) |
| `npm run test:e2e` | End-to-end tests (Playwright) — see below |
| `npm run preview` | Preview a production build locally |

## Testing

**Unit/component tests** (Vitest + React Testing Library, `src/**/*.test.ts(x)`) run in `jsdom` — see `vitest.config.ts` and `src/test/setup.ts`. Pure logic (`coordinates.test.ts`, `cardCommands.test.ts`) and component behavior (`LoginPage.test.tsx`, `CommandForm.test.tsx`, `ReplacePlayer.test.tsx`) both live alongside the code they test. `api/client.ts`'s `api` object is the usual mock seam — components call it directly rather than through a context/DI layer, so `vi.mock('../../api/client')` is enough.

**End-to-end tests** (`e2e/*.spec.ts`, Playwright) drive a real browser against the actual app — Vite dev server proxying to a real Tomcat backend, not a mocked environment. `playwright.config.ts`'s `webServer` entries start both automatically:

```bash
npm run test:e2e
```

The backend entry copies `src/test/resources/data` into `../target/e2e-data` (gitignored) before starting Tomcat against that copy, rather than pointing `JOL_DATA` at the real fixture directory — these specs submit real chat/commands, which the running server persists back to disk, and that would otherwise dirty checked-in fixture files on every run. Test credentials are `Player1`..`Player5` / `password` (see root `CLAUDE.md`'s test data note); `e2e/game.spec.ts` drives "Test Game," a fixture game all five are already registered in.

Cold start (first Maven dependency resolution + Tomcat boot) can take a couple of minutes; subsequent runs reuse the already-running server if one is up (`reuseExistingServer: true`) — kill anything on `:8080` first if you want a clean run against fresh fixture data.

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
