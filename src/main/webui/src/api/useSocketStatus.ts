import { useSyncExternalStore } from 'react';
import { getConnected, subscribeStatus } from '../stores/socket';

/**
 * `true` while the WS push channel is open. When `false`, change signals are
 * being missed and the board only re-syncs on the next reconnect (see
 * useGameSocket) — the HUD shows a quiet "reconnecting" cue for that window.
 */
export function useSocketConnected(): boolean {
  return useSyncExternalStore(subscribeStatus, getConnected);
}
