import { defineConfig } from 'vitest/config'

// Deliberately standalone, not merged with vite.config.ts — that config's
// plugins (serveCardAssets) and build settings exist only to run/build the
// app itself and have no bearing on (and could only complicate) running
// unit tests.
export default defineConfig({
  test: {
    // jsdom (not 'node') so component tests can render into a DOM — the
    // existing pure-logic tests (coordinates/cardCommands) don't need it but
    // run identically either way, so one environment for the whole suite is
    // simpler than per-file overrides.
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    // e2e/ holds Playwright specs (its own `test`/`expect` imports, a
    // different runner entirely) — without this vitest tries to collect them
    // too and fails on the unrelated `test.describe` API.
    exclude: ['**/node_modules/**', 'e2e/**'],
  },
})
