import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

// theme.ts reads localStorage at module load, so each test re-imports it
// fresh via vi.resetModules() + dynamic import after seeding storage.

async function loadTheme(hint?: string) {
  localStorage.clear();
  if (hint !== undefined) localStorage.setItem('jol-theme', hint);
  vi.resetModules();
  return import('./theme');
}

beforeEach(() => {
  document.body.removeAttribute('data-theme');
  document.body.removeAttribute('data-bs-theme');
});

afterEach(() => {
  localStorage.clear();
});

describe('initial hint coercion', () => {
  it('defaults to light with no hint', async () => {
    const t = await loadTheme();
    expect(t.getTheme()).toBe('light');
  });

  it('migrates the legacy "dark" hint to amethyst', async () => {
    const t = await loadTheme('dark');
    expect(t.getTheme()).toBe('amethyst');
  });

  it('migrates legacy "system" and unknown values to light', async () => {
    expect((await loadTheme('system')).getTheme()).toBe('light');
    expect((await loadTheme('bogus')).getTheme()).toBe('light');
  });

  it('keeps a valid named theme', async () => {
    expect((await loadTheme('nightshade')).getTheme()).toBe('nightshade');
  });
});

describe('applyTheme', () => {
  it('paints light with no data-bs-theme', async () => {
    const t = await loadTheme();
    t.applyTheme('light');
    expect(document.body.dataset.theme).toBe('light');
    expect(document.body.hasAttribute('data-bs-theme')).toBe(false);
    expect(localStorage.getItem('jol-theme')).toBe('light');
  });

  it('paints a dark theme with data-bs-theme="dark" and persists the hint', async () => {
    const t = await loadTheme();
    t.applyTheme('candlelit');
    expect(document.body.dataset.theme).toBe('candlelit');
    expect(document.body.getAttribute('data-bs-theme')).toBe('dark');
    expect(localStorage.getItem('jol-theme')).toBe('candlelit');
    expect(t.getTheme()).toBe('candlelit');
  });

  it('is idempotent when re-applying the current theme', async () => {
    const t = await loadTheme();
    t.applyTheme('oxblood');
    t.applyTheme('oxblood');
    expect(t.getTheme()).toBe('oxblood');
    expect(document.body.dataset.theme).toBe('oxblood');
  });

  it('exposes isDarkTheme for the four dark themes only', async () => {
    const t = await loadTheme();
    expect(t.isDarkTheme('light')).toBe(false);
    for (const name of ['amethyst', 'candlelit', 'oxblood', 'nightshade'] as const) {
      expect(t.isDarkTheme(name)).toBe(true);
    }
  });
});

describe('clearThemeHint', () => {
  it('removes the stored hint and reverts to light', async () => {
    const t = await loadTheme('nightshade');
    t.initTheme();
    expect(document.body.getAttribute('data-bs-theme')).toBe('dark');

    t.clearThemeHint();
    expect(localStorage.getItem('jol-theme')).toBeNull();
    expect(document.body.dataset.theme).toBe('light');
    expect(document.body.hasAttribute('data-bs-theme')).toBe(false);
    expect(t.getTheme()).toBe('light');
  });
});
