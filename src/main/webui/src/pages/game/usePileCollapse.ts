import { useCallback, useState } from 'react';

// Collapse state for a seat's archival piles (ash heap / RFG / library / crypt
// / hand), persisted per (gameId, seat, region) so a board you've tidied stays
// tidy across reloads — and, since the state no longer lives in the unmounting
// <Region>, across a pile emptying and refilling (which used to reset it and
// force-expand it). Collapsed is the default; the store only records the
// *expanded* exceptions.
const key = (gameId: string, seat: string, region: string) =>
  `jol.pile.${gameId}.${seat}.${region}`;

function read(k: string): boolean {
  try {
    return localStorage.getItem(k) === '1';
  } catch {
    return false;
  }
}

export function usePileExpanded(
  gameId: string,
  seat: string,
  region: string,
): [boolean, () => void] {
  const k = key(gameId, seat, region);
  const [expanded, setExpanded] = useState(() => read(k));

  const toggle = useCallback(() => {
    setExpanded((prev) => {
      const next = !prev;
      try {
        if (next) localStorage.setItem(k, '1');
        else localStorage.removeItem(k);
      } catch {
        // private mode / storage disabled — toggle still works for this session
      }
      return next;
    });
  }, [k]);

  return [expanded, toggle];
}
