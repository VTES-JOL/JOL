import { useSyncExternalStore } from 'react';

// Theme preference. A flat named list — NOT a light/dark axis and no
// 'system'/prefers-color-scheme following. 'light' is the default; the four
// dark themes each also carry body[data-bs-theme="dark"] so Tailwind's `dark`
// custom-variant and the data-bs-theme="dark" branches in RouteBackground.css /
// markdown.css keep working unchanged. The per-theme token blocks live in
// styles/tailwind.css, keyed on body[data-theme="<name>"].
//
// The server (player.theme, delivered on NavBean) is the source of truth.
// localStorage['jol-theme'] is only a pre-paint HINT so a reload / the login
// page don't flash the wrong theme before /nav resolves — the authenticated
// shell reconciles it against NavBean.theme on every load (see TopBar). A
// minimal copy of the apply logic runs as an inline <script> in index.html —
// keep the two in sync. On logout the hint is cleared (TopBar.logout).

export type Theme = 'light' | 'amethyst' | 'candlelit' | 'oxblood' | 'nightshade';

export const DEFAULT_THEME: Theme = 'light';

const DARK_THEMES = new Set<Theme>(['amethyst', 'candlelit', 'oxblood', 'nightshade']);

export const THEMES: { value: Theme; label: string; hint: string }[] = [
  { value: 'light', label: 'Light', hint: 'Parchment and ink — the default.' },
  { value: 'amethyst', label: 'Amethyst', hint: 'The classic gothic purple, dialled back for readability.' },
  { value: 'candlelit', label: 'Candlelit Crypt', hint: 'Warm charcoal, gold accents — a lit room, not a void.' },
  { value: 'oxblood', label: 'Oxblood Cathedral', hint: 'Dried-blood wine and tarnished copper.' },
  { value: 'nightshade', label: 'Nightshade Slate', hint: 'Cool blue-violet — moonlight on stone.' },
];

const KEY = 'jol-theme';
const listeners = new Set<() => void>();

function coerce(raw: string | null): Theme {
  switch (raw) {
    case 'amethyst':
    case 'candlelit':
    case 'oxblood':
    case 'nightshade':
    case 'light':
      return raw;
    // Legacy hint values from the old system/light/dark control.
    case 'dark':
      return 'amethyst';
    default:
      return DEFAULT_THEME;
  }
}

function readHint(): Theme {
  try {
    return coerce(localStorage.getItem(KEY));
  } catch {
    // private mode / storage disabled — fall through to the default
    return DEFAULT_THEME;
  }
}

let current: Theme = readHint();

function paint(theme: Theme): void {
  document.body.dataset.theme = theme;
  if (DARK_THEMES.has(theme)) document.body.setAttribute('data-bs-theme', 'dark');
  else document.body.removeAttribute('data-bs-theme');
}

export function getTheme(): Theme {
  return current;
}

export function isDarkTheme(theme: Theme): boolean {
  return DARK_THEMES.has(theme);
}

/**
 * Apply a theme and persist it as the local pre-paint hint. Used both by the
 * top-bar switcher (which also PUTs it to the server) and by the /nav
 * reconciliation effect (which brings a value changed on another device into
 * this browser). No-ops the notify when nothing actually changed.
 */
export function applyTheme(theme: Theme): void {
  paint(theme);
  try {
    localStorage.setItem(KEY, theme);
  } catch {
    // ignore — the in-memory value still drives this session
  }
  if (theme !== current) {
    current = theme;
    listeners.forEach((l) => l());
  }
}

/**
 * Drop the local hint and revert to the default. Called on logout so a shared
 * browser's login form doesn't keep the previous user's theme.
 */
export function clearThemeHint(): void {
  try {
    localStorage.removeItem(KEY);
  } catch {
    // ignore
  }
  // Repaint to the default without re-persisting — a post-logout browser
  // should be indistinguishable from a never-seen one (no hint at all).
  paint(DEFAULT_THEME);
  if (current !== DEFAULT_THEME) {
    current = DEFAULT_THEME;
    listeners.forEach((l) => l());
  }
}

/** Called once from main.tsx — re-paints from the stored hint. */
export function initTheme(): void {
  paint(current);
}

export function useTheme(): Theme {
  return useSyncExternalStore(
    (l) => {
      listeners.add(l);
      return () => {
        listeners.delete(l);
      };
    },
    getTheme,
    getTheme,
  );
}
