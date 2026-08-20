import { useEffect } from 'react';
import { subscribe } from './socket';

/** Re-invokes onMessage whenever a {"type": <type>} frame arrives over /jol/ws/updates. */
export function useJolSocket(type: string, onMessage: () => void) {
  useEffect(() => subscribe(type, onMessage), [type, onMessage]);
}
