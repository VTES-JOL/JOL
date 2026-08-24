import { test, expect } from '@playwright/test';

// "Test Game" is a fixture ACTIVE game in src/test/resources/data (games.json)
// with all five Player1..5 registered (registrations.json) — a real,
// already-running game to drive through the UI rather than one this suite
// has to create and start itself (which would additionally need every
// player's deck registered before Start is even clickable).
const USERNAME = 'Player1';
const PASSWORD = 'password';

async function login(page: import('@playwright/test').Page) {
  await page.goto('/jol/login');
  await page.getByLabel('Username').fill(USERNAME);
  await page.getByLabel('Password').fill(PASSWORD);
  await page.getByRole('button', { name: 'Log In' }).click();
  await expect(page.locator('.user-menu-toggle')).toContainText(USERNAME);
}

test.describe('in-game', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.getByRole('button', { name: 'My Games' }).click();
    await page.getByRole('link', { name: 'Test Game', exact: true }).click();
    await expect(page).toHaveURL(/\/jol\/game\//);
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
