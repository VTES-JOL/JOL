// Tracks whether the backend is reachable at all, fed from client.ts's single
// request() choke point. Deliberately keyed on *network*-level failures
// (fetch() itself rejecting — connection refused, DNS failure, timeout), not
// HTTP error responses: a 404/500 means the server responded, so it's up,
// just erroring — that's a different problem from "unavailable" and each
// call site already handles it with its own .catch().
import { API_BASE } from './apiBase';

// A lone failed request shouldn't blank the whole app — only publish
// "offline" if failures persist past this window, so a single dropped
// packet doesn't cause a visible flash.
const OFFLINE_DEBOUNCE_MS = 1500;
const HEALTH_CHECK_INITIAL_MS = 5000;
const HEALTH_CHECK_MAX_MS = 30000;

export interface ConnectivityState {
  online: boolean;
  // Distinguishes "never successfully connected" from "was connected, lost
  // it" — same `online: false` either way, but worth different copy (a cold
  // load against an already-dead backend isn't a connection that was "lost").
  everConnected: boolean;
}

type Listener = (state: ConnectivityState) => void;

let state: ConnectivityState = { online: true, everConnected: false };
const listeners = new Set<Listener>();

let offlineDebounceTimer: ReturnType<typeof setTimeout> | null = null;
let healthCheckTimer: ReturnType<typeof setTimeout> | null = null;
let healthCheckDelay = HEALTH_CHECK_INITIAL_MS;
// Whether the most recent request failed — read by the debounce timer to
// confirm the failure is still current when it fires (a success in between
// clears this), independent of whether `state.online` has been flipped yet.
let failing = false;

function notify() {
  listeners.forEach((listener) => listener(state));
}

function setState(patch: Partial<ConnectivityState>) {
  const next = { ...state, ...patch };
  if (next.online === state.online && next.everConnected === state.everConnected) return;
  state = next;
  notify();
}

export function getConnectivity(): ConnectivityState {
  return state;
}

export function subscribeConnectivity(listener: Listener): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function reportSuccess() {
  failing = false;
  if (offlineDebounceTimer) {
    clearTimeout(offlineDebounceTimer);
    offlineDebounceTimer = null;
  }
  stopHealthCheck();
  healthCheckDelay = HEALTH_CHECK_INITIAL_MS;
  setState({ online: true, everConnected: true });
}

export function reportFailure() {
  failing = true;
  if (state.online && !offlineDebounceTimer) {
    offlineDebounceTimer = setTimeout(() => {
      offlineDebounceTimer = null;
      if (failing) {
        setState({ online: false });
        startHealthCheck();
      }
    }, OFFLINE_DEBOUNCE_MS);
  }
}

/**
 * Forces an immediate health-check attempt, bypassing whatever backoff wait
 * is currently in progress — used for a manual "Retry now" action, and by
 * the WebSocket reconnecting successfully (a strong, near-instant hint the
 * server may be back, rather than waiting up to healthCheckDelay for the
 * next scheduled poll).
 */
export function checkNow() {
  fetch(`${API_BASE}/config`, { credentials: 'include' })
    .then((res) => {
      if (res.ok) reportSuccess();
    })
    .catch(() => {
      // still down
    });
}

// Once offline, recovery can't rely on some other component's own retry —
// whatever triggered the failure (e.g. MainPage's children) may no longer be
// mounted or fetching. This owns its own recovery check, with exponential
// backoff so a long outage doesn't get hammered with requests forever.
function startHealthCheck() {
  if (healthCheckTimer) return;

  const scheduleNext = () => {
    healthCheckDelay = Math.min(healthCheckDelay * 2, HEALTH_CHECK_MAX_MS);
    healthCheckTimer = setTimeout(tick, healthCheckDelay);
  };

  const tick = () => {
    fetch(`${API_BASE}/config`, { credentials: 'include' })
      .then((res) => {
        if (res.ok) {
          reportSuccess();
        } else {
          scheduleNext();
        }
      })
      .catch(scheduleNext);
  };

  healthCheckTimer = setTimeout(tick, healthCheckDelay);
}

function stopHealthCheck() {
  if (healthCheckTimer) {
    clearTimeout(healthCheckTimer);
    healthCheckTimer = null;
  }
}
