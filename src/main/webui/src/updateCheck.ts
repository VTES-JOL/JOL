import { useSyncExternalStore } from 'react';

// Detects a stale bundle by polling version.json (emitted by vite.config.ts's
// emit-version-json plugin, next to index.html/assets/) and comparing its
// buildId to __BUILD_ID__ baked into the currently running bundle. Same
// module-singleton + useSyncExternalStore shape as connectivity.ts/toast.ts.
//
// Supersedes the old ds.js WS ping/pong version check (JolWebSocketEndpoint
// still answers it, but nothing here uses it) — that scheme assumed one
// monolithic app version string, which doesn't fit content-hashed Vite
// output; comparing a per-build id fetched fresh is the equivalent for this
// build system.

const POLL_INTERVAL_MS = 10 * 60 * 1000;

let updateAvailable = false;
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((listener) => listener());
}

function checkForUpdate() {
  if (updateAvailable) return;
  fetch(`${import.meta.env.BASE_URL}version.json`, { cache: 'no-store' })
    .then((res) => (res.ok ? res.json() : null))
    .then((data: { buildId?: string } | null) => {
      if (data?.buildId && data.buildId !== __BUILD_ID__) {
        updateAvailable = true;
        notify();
      }
    })
    .catch(() => {
      // version.json unreachable — no worse off than not checking; the next
      // scheduled/visibility-triggered check tries again.
    });
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function getSnapshot(): boolean {
  return updateAvailable;
}

export function useUpdateAvailable(): boolean {
  return useSyncExternalStore(subscribe, getSnapshot);
}

// A plain location.reload() can be handed a still-cached index.html that
// references the previous hashed bundle, so __BUILD_ID__ never changes and
// the next checkForUpdate() flips the banner straight back on — the "banner
// won't go away without a hard refresh" symptom. Force a fresh copy of the
// app shell into the HTTP cache first (`cache: 'reload'` bypasses the cache
// for the request and replaces the stored entry with the response), then the
// reload parses the new index.html and loads the new bundle.
export async function reloadForUpdate(): Promise<void> {
  try {
    await fetch(import.meta.env.BASE_URL, { cache: 'reload' });
  } catch {
    // offline or blocked — a plain reload is still the best we can do
  }
  location.reload();
}

// Started once from App.tsx. No-op in dev — vite dev never emits
// version.json, and a dev session isn't "deployed" in the sense this exists
// to detect.
export function startUpdateCheck() {
  if (import.meta.env.DEV) return;

  checkForUpdate();
  setInterval(checkForUpdate, POLL_INTERVAL_MS);
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') checkForUpdate();
  });
}
