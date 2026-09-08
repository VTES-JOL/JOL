import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { onOpen, send } from '../stores/socket';

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
 *
 * It also resyncs the board on every socket (re)open: the socket only delivers
 * change signals while it is connected, so anything that changed during a drop
 * — server redeploy, wifi blip, laptop sleep — produced an `invalidate` signal
 * this tab never received, leaving the board silently stale until the next
 * action happened to push. Re-pulling on open closes that gap; react-query
 * dedupes the one redundant refetch this causes right after the initial
 * fetch-on-mount.
 */
export function useGameSocket(gameId: string | null) {
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!gameId) return;
    const join = () => send({ type: 'join', game: gameId });
    join();
    const onSocketOpen = () => {
      join();
      queryClient.invalidateQueries({ queryKey: ['game', gameId] });
    };
    const unsubOpen = onOpen(onSocketOpen);
    return () => {
      send({ type: 'leave', game: gameId });
      unsubOpen();
    };
  }, [gameId, queryClient]);
}
