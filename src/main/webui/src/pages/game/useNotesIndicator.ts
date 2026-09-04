import { useEffect, useRef, useState } from 'react';
import type { GameSnapshot } from '../../api/types';

export type NotesIndicator = 'none' | 'content' | 'update';

// Drives the dot on the "Notes" button (NotesToggleButton):
//   'content' — notes have text in them
//   'update'  — global notes changed while the drawer was closed
// Both clear the moment the drawer is opened (you've now seen the latest).
// Private notes never trigger 'update' — nobody else can touch them.
export function useNotesIndicator(game: GameSnapshot | undefined, open: boolean): NotesIndicator {
  const globalNotes = game?.globalNotes ?? '';
  const hasContent = !!(globalNotes.trim() || (game?.privateNotes ?? '').trim());

  // Seeded from the first real snapshot, not the undefined loading state —
  // otherwise the initial ""→content transition reads as an "update".
  const lastSeenGlobal = useRef<string | null>(null);
  const [staleUpdate, setStaleUpdate] = useState(false);

  useEffect(() => {
    if (!game) return;
    if (lastSeenGlobal.current === null || open) {
      lastSeenGlobal.current = globalNotes;
      setStaleUpdate(false);
    } else if (globalNotes !== lastSeenGlobal.current) {
      setStaleUpdate(true);
    }
  }, [game, open, globalNotes]);

  return staleUpdate ? 'update' : hasContent ? 'content' : 'none';
}
