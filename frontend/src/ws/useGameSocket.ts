import { useEffect } from 'react';
import { onOpen, send, subscribe } from './socket';

/**
 * Joins the given game's WS "room" (see JolWebSocketEndpoint's join/leave
 * handling, WebSocketRegistry.joinGame/leaveGame) so {"type":"game","id":...}
 * pushes for THIS game reach this tab, and re-invokes onMessage whenever one
 * arrives. Mirrors ds.js's wsJoinGame/wsLeaveGame — the join is re-sent on
 * every reconnect (see socket.ts's onOpen), since the server only tracks room
 * membership for the current live session.
 */
export function useGameSocket(gameId: string | null, onMessage: () => void) {
  useEffect(() => {
    if (!gameId) return;
    const join = () => send({ type: 'join', game: gameId });
    join();
    const unsubOpen = onOpen(join);
    const unsubMessage = subscribe('game', (msg) => {
      if (msg.id === gameId) onMessage();
    });
    return () => {
      send({ type: 'leave', game: gameId });
      unsubOpen();
      unsubMessage();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [gameId]);
}
