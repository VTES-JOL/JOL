// Thin wrapper around /jol/ws/updates (see JolWebSocketEndpoint). Auth rides the
// jol_at cookie automatically since the handshake is same-origin. Messages are
// small JSON envelopes: {"type":"invalidate","key":[...]}, {"type":"pong"}.
import { checkNow } from '../api/connectivity';

type Listener = (data: Record<string, unknown>) => void;

const listeners = new Map<string, Set<Listener>>();
const openListeners = new Set<() => void>();
let socket: WebSocket | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

// Exponential backoff for reconnect attempts, reset to the base delay on any
// successful open. Without this, a tab that can never reconnect (e.g. a
// revoked session with no valid refresh cookie either — see
// JolWebSocketEndpoint.Configurator for the cases a reconnect *can* recover
// from) retries once every RECONNECT_BASE_MS forever, indefinitely spamming
// the server's handshake-rejection log.
const RECONNECT_BASE_MS = 3000;
const RECONNECT_MAX_MS = 30000;
let reconnectDelay = RECONNECT_BASE_MS;

// Identifies this browser tab's connection to the server (one per page load,
// stable across reconnects) — sent to client.ts as a header on every REST
// call too, so a handler whose own response already carries fresh state can
// ask the server to skip re-notifying this same tab over the socket. See
// WebSocketRegistry.notifyInvalidate(key, excludeClientId).
export const CLIENT_ID = crypto.randomUUID();

function wsUrl(): string {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  // clientId rides the handshake itself (query param), not a follow-up message — a message
  // has to round-trip after the socket is already open, leaving a window right after connect
  // (or reconnect) where a REST call fires before the server can exclude this tab from its
  // own broadcast. See JolWebSocketEndpoint.Configurator.modifyHandshake.
  return `${protocol}//${window.location.host}/jol/ws/updates?clientId=${encodeURIComponent(CLIENT_ID)}`;
}

function dispatch(message: Record<string, unknown>) {
  const type = message.type as string | undefined;
  if (!type) return;
  listeners.get(type)?.forEach((listener) => listener(message));
}

function connect() {
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
    return;
  }
  socket = new WebSocket(wsUrl());
  socket.onopen = () => {
    reconnectDelay = RECONNECT_BASE_MS;
    // clientId is already tagged via the handshake's own ?clientId= query param (see
    // wsUrl above) — no follow-up 'hello' message needed.
    // A successful WS (re)connect is a strong, near-instant hint the server
    // is back — check immediately rather than waiting for connectivity's own
    // backoff-scheduled poll, which could be up to 30s away by this point.
    checkNow();
    // e.g. re-sending {type:'join', game: id} after a reconnect — the server
    // only tracks room membership per live session, so it doesn't survive a
    // dropped/reopened socket on its own.
    openListeners.forEach((listener) => listener());
  };
  socket.onmessage = (event) => {
    try {
      dispatch(JSON.parse(event.data));
    } catch {
      // ignore malformed frames
    }
  };
  socket.onclose = () => {
    socket = null;
    reconnectTimer = setTimeout(connect, reconnectDelay);
    reconnectDelay = Math.min(reconnectDelay * 2, RECONNECT_MAX_MS);
  };
  socket.onerror = () => socket?.close();
}

export function subscribe(type: string, listener: Listener): () => void {
  if (!listeners.has(type)) listeners.set(type, new Set());
  listeners.get(type)!.add(listener);
  connect();
  return () => {
    listeners.get(type)?.delete(listener);
    if (listeners.get(type)?.size === 0) listeners.delete(type);
  };
}

export function send(message: Record<string, unknown>) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(message));
  }
}

// Re-invoked every time the socket (re)connects — see the onopen comment above.
export function onOpen(listener: () => void): () => void {
  openListeners.add(listener);
  return () => openListeners.delete(listener);
}

export function disconnect() {
  if (reconnectTimer) clearTimeout(reconnectTimer);
  socket?.close();
  socket = null;
  reconnectDelay = RECONNECT_BASE_MS;
}
