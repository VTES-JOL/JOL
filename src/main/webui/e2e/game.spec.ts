import { test, expect } from '@playwright/test';

// "Test Game" is a fixture ACTIVE game in src/test/resources/data (games.json)
// with all five Player1..5 registered (registrations.json) — a real,
// already-running game to drive through the UI rather than one this suite
// has to create and start itself (which would additionally need every
// player's deck registered before Start is even clickable).
const USERNAME = 'Player1';
const PASSWORD = 'password';
const TEST_GAME_ID = '01K6CP9GMWMG78RERJVA2QM0R3';

async function login(page: import('@playwright/test').Page, username: string = USERNAME) {
  await page.goto('/jol/login');
  // Retries the fill+submit as a unit: Vite's dev server can trigger a
  // dependency-reoptimization full-reload on a brand-new page's first hit
  // (see vite.config.ts's own "Re-optimizing dependencies" case), which
  // aborts an in-flight login POST with net::ERR_ABORTED — a dev-server-only
  // hazard, not an app bug, that's most likely to land exactly here (this is
  // usually the first real request a fresh context/page makes).
  await expect(async () => {
    await page.getByLabel('Username').fill(username);
    await page.getByLabel('Password').fill(PASSWORD);
    await page.getByRole('button', { name: 'Log In' }).click();
    await expect(page.locator('.user-menu-toggle')).toContainText(username, { timeout: 3_000 });
  }).toPass({ timeout: 20_000 });
}

async function openTestGame(page: import('@playwright/test').Page) {
  // Same Vite dev-server full-reload hazard as login() above can land here
  // too (mid-click) — retry the whole click sequence as a unit rather than
  // hanging on a single stuck locator.
  await expect(async () => {
    await page.getByRole('button', { name: 'My Games' }).click();
    await page.getByRole('link', { name: 'Test Game', exact: true }).click({ timeout: 3_000 });
    await expect(page).toHaveURL(/\/jol\/game\//, { timeout: 3_000 });
  }).toPass({ timeout: 20_000 });
}

test.describe('in-game', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await openTestGame(page);
  });

  test('renders the board with all five registered players', async ({ page }) => {
    for (const name of ['Player1', 'Player2', 'Player3', 'Player4', 'Player5']) {
      // Scoped to a board header, not just any text match — "Player2" for
      // instance also appears as a hidden <option> in the Ping <select>.
      await expect(page.locator('.player .card-header').getByText(name, { exact: true })).toBeVisible();
    }
  });

  test('sending a chat message shows it in the game chat log', async ({ page }) => {
    const message = `e2e chat check ${Date.now()}`;
    await page.getByLabel('Chat').fill(message);
    await page.getByRole('button', { name: 'Submit' }).click();

    await expect(page.locator('#gameChatCard').getByText(message)).toBeVisible();
    // The free-text field clears on success — a stale value would mean the
    // submit either failed or never happened.
    await expect(page.getByLabel('Chat')).toHaveValue('');
  });

  test('a valid command mutates real game state visible on the board', async ({ page }) => {
    // Exercises the full round trip through DoCommand into persisted game
    // state, not just the request/response — "vp +1" awards Player1 a
    // victory point, shown as a badge on their own board header (only
    // rendered once victoryPoints > 0, see PlayerBoard.tsx).
    const ownBoard = page.locator('.player .card-header', { hasText: 'Player1' });
    await expect(ownBoard.getByText(/VP$/)).toHaveCount(0);

    await page.getByLabel('Command').fill('vp +1');
    await page.getByRole('button', { name: 'Submit' }).click();

    await expect(ownBoard.getByText('1 VP')).toBeVisible();
  });

  test('an invalid command shows the server-side validation error and it survives the self-triggered board refresh', async ({ page }) => {
    // "vp" with no +/- amount is rejected server-side (DoCommand.vp). This
    // used to be flaky/unobservable: the error came back on game.status, but
    // saving state (even for a rejected command — GameModel.submit marks
    // stateChanged unconditionally) broadcasts a WebSocket push to the room,
    // including the sender, whose resulting GET /view refetch always carries
    // status: null — so the message could vanish within the same round trip,
    // before Playwright (or a real user) ever saw it. CommandForm.tsx now
    // keeps it in local state instead, so it survives exactly that refresh.
    await page.getByLabel('Command').fill('vp');
    await page.getByRole('button', { name: 'Submit' }).click();
    await expect(page.getByText('No amount given use +/-')).toBeVisible();

    // Give the self-triggered WebSocket round trip (join notify -> GET /view
    // refetch) time to land, then confirm the message wasn't wiped by it.
    await page.waitForTimeout(1500);
    await expect(page.getByText('No amount given use +/-')).toBeVisible();
  });
});

test.describe('cross-player sync', () => {
  // Two isolated browser contexts (separate cookie jars) logged in as
  // different real players in the same game — proves the WebSocket push
  // (WebSocketRegistry.notifyGame -> {"type":"invalidate","key":["game",id]})
  // reaches a genuinely different viewer's session and their
  // useQuery(['game', gameId]) refetches on its own, with no reload() call
  // anywhere in this test.
  test('a chat message from one player appears live for another without a reload', async ({ browser }) => {
    // Two logins, two navigations, and two contexts to tear down make this
    // inherently heavier than the single-page tests above — give it more
    // headroom under parallel worker load than the default 30s.
    test.setTimeout(60_000);
    const ctx1 = await browser.newContext();
    const ctx2 = await browser.newContext();
    try {
      const page1 = await ctx1.newPage();
      const page2 = await ctx2.newPage();
      await login(page1, 'Player1');
      await login(page2, 'Player2');
      // Direct navigation rather than openTestGame()'s click-through-the-nav
      // flow: with two contexts open at once, clicks through "My Games" hit
      // real DOM churn (React re-rendering that dropdown mid-click as the
      // ambient invalidation traffic from the *other* context's actions
      // lands) that single-page tests never see. Going straight to the
      // known fixture URL sidesteps that entirely.
      await Promise.all([page1.goto(`/jol/game/${TEST_GAME_ID}`), page2.goto(`/jol/game/${TEST_GAME_ID}`)]);
      await expect(page1.getByLabel('Chat')).toBeVisible();
      await expect(page2.getByLabel('Chat')).toBeVisible();

      const message = `cross-player e2e check ${Date.now()}`;
      await page1.getByLabel('Chat').fill(message);
      await page1.getByRole('button', { name: 'Submit' }).click();

      // page2 never reloads or navigates again — this only passes if the
      // WS push + query invalidation actually delivered a live update.
      await expect(page2.locator('#gameChatCard').getByText(message)).toBeVisible();
    } finally {
      await Promise.all([ctx1.close(), ctx2.close()]);
    }
  });
});
