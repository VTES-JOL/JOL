import { useEffect, useRef, useState } from 'react';

type LineTime = { postedAt?: string };

const key = (gameId: string) => `jol:chatSeen:${gameId}`;

// #12 (D32): drives the chat log's "new since your last visit" divider.
// "Visit" = last page load / navigation — when this component unmounts (or the
// tab closes) the newest chat line it had rendered is written to localStorage;
// on the next mount we freeze that stored value and hand it to GameChatLog,
// which rules a divider before the first line newer than it.
//
// Client-only and per-device, deliberately — same model as the mobile Log-tab
// unread dot (`seenChatLen`). No backend field: the server re-stamps its own
// per-player access time on every WS-driven refetch, so it can't answer
// "since I last looked" anyway.
export function useChatSeenMarker(gameId: string, lines: LineTime[]): string | null {
  // Frozen once, on first render for this game — the divider must not jump as
  // new lines arrive during the visit.
  const [frozen] = useState<string | null>(() => {
    try {
      return localStorage.getItem(key(gameId));
    } catch {
      return null;
    }
  });

  // Newest postedAt currently in the log, kept in a ref so the unmount / unload
  // writer always sees the latest without re-subscribing.
  const newest = useRef<string | null>(null);
  for (const l of lines) {
    if (l.postedAt && (!newest.current || l.postedAt > newest.current)) {
      newest.current = l.postedAt;
    }
  }

  useEffect(() => {
    const write = () => {
      if (!newest.current) return;
      try {
        localStorage.setItem(key(gameId), newest.current);
      } catch {
        /* private mode / storage disabled — the divider just won't persist */
      }
    };
    window.addEventListener('beforeunload', write);
    return () => {
      window.removeEventListener('beforeunload', write);
      write();
    };
  }, [gameId]);

  return frozen;
}
