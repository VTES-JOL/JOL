import { defineConfig } from 'vitest/config';
import path from 'node:path';
import { storybookTest } from '@storybook/addon-vitest/vitest-plugin';
import { playwright } from '@vitest/browser-playwright';

// Deliberately standalone, not merged with vite.config.ts — that config's
// plugins (serveCardAssets) and build settings exist only to run/build the
// app itself and have no bearing on (and could only complicate) running
// unit tests.
//
// Two projects: "unit" (jsdom, existing plain unit/RTL tests) and
// "storybook" (real Chromium via Playwright, running every *.stories.tsx's
// play function as a test — see
// https://storybook.js.org/docs/next/writing-tests/integrations/vitest-addon).
// `npm test` runs both by default; `npm run test:storybook` targets just
// the latter.
export default defineConfig({
  test: {
    projects: [
      {
        extends: true,
        test: {
          name: 'unit',
          // jsdom (not 'node') so component tests can render into a DOM —
          // the existing pure-logic tests (coordinates/cardCommands) don't
          // need it but run identically either way, so one environment for
          // the whole suite is simpler than per-file overrides.
          environment: 'jsdom',
          setupFiles: ['./src/test/setup.ts'],
          // e2e/ holds Playwright specs (its own `test`/`expect` imports, a
          // different runner entirely) — without this vitest tries to
          // collect them too and fails on the unrelated `test.describe` API.
          exclude: ['**/node_modules/**', 'e2e/**'],
        },
      },
      {
        extends: true,
        plugins: [
          storybookTest({
            configDir: path.join(import.meta.dirname, '.storybook'),
          }),
        ],
        test: {
          name: 'storybook',
          browser: {
            enabled: true,
            headless: true,
            provider: playwright({}),
            instances: [{ browser: 'chromium' }],
          },
        },
      },
    ],
  },
});
