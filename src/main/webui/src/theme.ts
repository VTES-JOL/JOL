import { useSyncExternalStore } from 'react';

// Theme preference. 'system' follows the OS; 'light' / 'dark' pin it. The
// preference is stored in localStorage under 'jol-theme' and applied by
// toggling `body[data-bs-theme="dark"]` — the selector styles/tailwind.css's
// `dark` custom-variant keys off. A minimal copy of the apply logic also runs
// as an inline <script> in index.html so the right theme is on the <body>
// before first paint (no light flash on reload) — keep the two in sync.

export type ThemePref = 'system' | 'light' | 'dark';

const KEY = 'jol-theme';
const listeners = new Set<() => void>();

function read(): ThemePref {
  try {
    const v = localStorage.getItem(KEY);
    if (v === 'light' || v === 'dark' || v === 'system') return v;
  } catch {
    // private mode / storage disabled — fall through to the default
  }
  return 'system';
}

let pref: ThemePref = read();

function systemPrefersDark(): boolean {
  return typeof window !== 'undefined' && !!window.matchMedia?.('(prefers-color-scheme: dark)').matches;
}

export function resolveDark(p: ThemePref): boolean {
  return p === 'dark' || (p === 'system' && systemPrefersDark());
}

function paint(p: ThemePref): void {
  if (resolveDark(p)) document.body.setAttribute('data-bs-theme', 'dark');
  else document.body.removeAttribute('data-bs-theme');
}

export function getThemePref(): ThemePref {
  return pref;
}

export function setThemePref(next: ThemePref): void {
  pref = next;
  try {
    localStorage.setItem(KEY, next);
  } catch {
    // ignore — the in-memory value still drives this session
  }
  paint(next);
  listeners.forEach((l) => l());
}

/**
 * Called once from main.tsx. Re-paints from the stored preference and keeps a
 * 'system' preference in step with later OS theme changes.
 */
export function initTheme(): void {
  paint(pref);
  window.matchMedia?.('(prefers-color-scheme: dark)').addEventListener('change', () => {
    if (pref !== 'system') return;
    paint(pref);
    listeners.forEach((l) => l());
  });
}

export function useThemePref(): ThemePref {
  return useSyncExternalStore(
    (l) => {
      listeners.add(l);
      return () => {
        listeners.delete(l);
      };
    },
    getThemePref,
    getThemePref,
  );
}
