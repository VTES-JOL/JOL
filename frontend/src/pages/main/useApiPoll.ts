import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useJolSocket } from '../../ws/useJolSocket';

/**
 * Fetches a targeted endpoint on mount and again on every WebSocket signal
 * matching wsType (e.g. "main:games", "main:chat", "main:notes" — see
 * WebSocketRegistry.notifyMainScope on the backend). Each caller owns
 * exactly the one GET it needs and refreshes only on the event that
 * actually affects it, not a shared page-wide "something changed" signal.
 */
export function useApiPoll<T>(path: string, initial: T, wsType: string): T {
  const [value, setValue] = useState<T>(initial);

  const refresh = () => {
    api.get<T>(path).then(setValue).catch((err) => console.error(`Failed to load ${path}`, err));
  };

  useEffect(refresh, [path]);
  useJolSocket(wsType, refresh);

  return value;
}
