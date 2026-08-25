import { useEffect } from 'react';
import { onOpen, send } from './socket';

/**
 * Joins the given game's WS "room" (see JolWebSocketEndpoint's join/leave
 * handling, WebSocketRegistry.joinGame/leaveGame) so this tab is one of the
 * sessions WebSocketRegistry.notifyGame targets. Mirrors ds.js's
 * wsJoinGame/wsLeaveGame — the join is re-sent on every reconnect (see
 * socket.ts's onOpen), since the server only tracks room membership for the
 * current live session.
 *
 * Unlike before, this hook doesn't itself react to the push — notifyGame now
 * sends the same {"type":"invalidate","key":["game", gameId]} envelope as
 * every other migrated page, so ws/useQueryInvalidation.ts's generic bridge
 * (mounted once in App.tsx) picks it up and invalidates the ['game', gameId]
 * query on its own. This hook only needs to keep this tab in the room.
 */
export function useGameSocket(gameId: string | null) {
  useEffect(() => {
    if (!gameId) return;
    const join = () => send({ type: 'join', game: gameId });
    join();
    const unsubOpen = onOpen(join);
    return () => {
      send({ type: 'leave', game: gameId });
      unsubOpen();
    };
  }, [gameId]);
}
