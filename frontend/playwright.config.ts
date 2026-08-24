import { defineConfig, devices } from '@playwright/test';

// End-to-end smoke tests driving the real app through a browser — Vite dev
// server (self-signed TLS, see vite.config.ts) proxying to the real Tomcat
// backend, exactly the CLAUDE.md local-dev setup, not a mocked environment.
// Run with `npm run test:e2e` (see package.json) from frontend/, or let the
// webServer entries below start both processes automatically.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: 'list',
  use: {
    baseURL: 'https://localhost:5173',
    // Vite's TLS is a locally-generated self-signed cert (plugin-basic-ssl),
    // not a trusted CA — see vite.config.ts's comment on why HTTPS is needed
    // at all (AuthService's Secure cookie).
    ignoreHTTPSErrors: true,
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: [
    {
      // cwd defaults to the config file's directory (frontend/); the command
      // below is relative to the repo root, so this must run one level up.
      // Tomcat's first cold start (dependency resolution + JVM boot) can
      // take a while, hence the generous timeout — subsequent runs are much
      // faster.
      //
      // Copies the fixture data into target/ (gitignored) instead of
      // pointing JOL_DATA at src/test/resources/data directly — these specs
      // submit real commands/chat, which the running server persists back to
      // disk (game logs at minimum, full state on shutdown/schedule), and
      // that would otherwise dirty checked-in fixture files on every run.
      command:
        'rm -rf target/e2e-data && mkdir -p target && cp -r src/test/resources/data target/e2e-data && JOL_DATA=target/e2e-data ./mvnw tomcat9:run',
      cwd: '..',
      url: 'http://localhost:8080/jol/',
      timeout: 180_000,
      reuseExistingServer: true,
    },
    {
      command: 'npm run dev',
      url: 'https://localhost:5173/jol/login',
      ignoreHTTPSErrors: true,
      timeout: 60_000,
      reuseExistingServer: true,
    },
  ],
});
