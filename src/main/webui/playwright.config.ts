import { defineConfig, devices } from '@playwright/test';

// End-to-end smoke tests driving the real app through a browser — one
// `quarkus:dev` process (Quinoa proxies this frontend's own `npm run dev`
// automatically), exactly the CLAUDE.md local-dev setup, not a mocked
// environment. Run with `npm run test:e2e` (see package.json), or let the
// webServer entry below start it automatically.
//
// Requires local Postgres already running and reachable
// (`docker compose -f local-docker-compose.yml up -d db` from the repo
// root) — same precondition `quarkus:dev` itself has outside these tests.
// Not started here: unlike the old Tomcat+JSON-file setup, state now lives
// in Postgres, which isn't something a webServer command should spin up and
// tear down per test run.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: 'list',
  use: {
    baseURL: 'https://localhost:8443',
    // Quarkus dev mode's TLS is a locally-generated self-signed cert (see
    // application.properties's %dev.quarkus.http.ssl-port block) — required
    // because AuthService's cookies are unconditionally Secure.
    ignoreHTTPSErrors: true,
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    // cwd defaults to the config file's directory (src/main/webui/); the
    // command below runs from the repo root, three levels up. Resets
    // Postgres to the Player1-5 fixture set before every run (same
    // ./load-test-fixtures.sh a developer runs manually — see root
    // CLAUDE.md) so these specs — which submit real chat/commands, mutating
    // that data — always start from known state instead of accumulating
    // drift across runs. Quarkus's own dependency resolution + first boot
    // can take a while on a cold cache, hence the generous timeout;
    // subsequent runs are much faster.
    command:
      'cd ../../.. && ./load-test-fixtures.sh && JOL_DB_PASSWORD=jol ENABLE_CAPTCHA=false ./mvnw quarkus:dev',
    url: 'https://localhost:8443/jol/login',
    ignoreHTTPSErrors: true,
    timeout: 180_000,
    reuseExistingServer: true,
  },
});
