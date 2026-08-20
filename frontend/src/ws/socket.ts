// Thin wrapper around /jol/ws/updates (see JolWebSocketEndpoint). Auth rides the
// jol_at cookie automatically since the handshake is same-origin. Messages are
// small JSON envelopes: {"type":"main"}, {"type":"game","id":...}, {"type":"pong"}.
import { checkNow } from '../api/connectivity';

type Listener = (data: Record<string, unknown>) => void;

const listeners = new Map<string, Set<Listener>>();
let socket: WebSocket | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;

// Identifies this browser tab's connection to the server (one per page load,
// stable across reconnects) — sent to client.ts as a header on every REST
// call too, so a handler whose own response already carries fresh state can
// ask the server to skip re-notifying this same tab over the socket. See
// WebSocketRegistry.notifyMainScope(scope, excludeClientId).
export const CLIENT_ID = crypto.randomUUID();

function wsUrl(): string {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/jol/ws/updates`;
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
    socket?.send(JSON.stringify({ type: 'hello', clientId: CLIENT_ID }));
    // A successful WS (re)connect is a strong, near-instant hint the server
    // is back — check immediately rather than waiting for connectivity's own
    // backoff-scheduled poll, which could be up to 30s away by this point.
    checkNow();
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
    reconnectTimer = setTimeout(connect, 3000);
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

export function disconnect() {
  if (reconnectTimer) clearTimeout(reconnectTimer);
  socket?.close();
  socket = null;
}
