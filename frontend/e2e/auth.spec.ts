import { test, expect } from '@playwright/test';

// Credentials fixed by src/test/resources/data/players.json — every
// Player1..Player5 account uses "password" (see CLAUDE.md's test data note).
const USERNAME = 'Player1';
const PASSWORD = 'password';

test.describe('login', () => {
  test('valid credentials land on the authenticated shell', async ({ page }) => {
    await page.goto('/jol/login');
    await page.getByLabel('Username').fill(USERNAME);
    await page.getByLabel('Password').fill(PASSWORD);
    await page.getByRole('button', { name: 'Log In' }).click();

    await expect(page).toHaveURL(/\/jol\/(main)?$/);
    await expect(page.locator('.user-menu-toggle')).toContainText(USERNAME);
  });

  test('invalid password shows an inline error and stays on the login page', async ({ page }) => {
    await page.goto('/jol/login');
    await page.getByLabel('Username').fill(USERNAME);
    await page.getByLabel('Password').fill('not-the-password');
    await page.getByRole('button', { name: 'Log In' }).click();

    await expect(page.getByText('Invalid username or password.')).toBeVisible();
    await expect(page).toHaveURL(/\/jol\/login$/);
  });

  test('logging out returns to the login page and a subsequent nav requires login again', async ({ page }) => {
    await page.goto('/jol/login');
    await page.getByLabel('Username').fill(USERNAME);
    await page.getByLabel('Password').fill(PASSWORD);
    await page.getByRole('button', { name: 'Log In' }).click();
    await expect(page.locator('.user-menu-toggle')).toContainText(USERNAME);

    await page.locator('.user-menu-toggle').click();
    await page.getByRole('link', { name: 'Log Out' }).click();

    await expect(page).toHaveURL(/\/jol\/login$/);
    await page.goto('/jol/lobby');
    await expect(page).toHaveURL(/\/jol\/login$/);
  });
});

test.describe('lobby smoke', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/jol/login');
    await page.getByLabel('Username').fill(USERNAME);
    await page.getByLabel('Password').fill(PASSWORD);
    await page.getByRole('button', { name: 'Log In' }).click();
    await expect(page.locator('.user-menu-toggle')).toContainText(USERNAME);
  });

  test('navigating to Lobby from the top bar renders the lobby page', async ({ page }) => {
    await page.getByRole('link', { name: 'Lobby' }).click();
    await expect(page).toHaveURL(/\/jol\/lobby$/);
  });
});
