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
