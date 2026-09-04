import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { EnrichedDeck } from '../../api/types';
import { DeckView } from '../../components/DeckView';
import { runRequest } from '../../api/mutate';

// Mirrors game-deck.jsp/doShowDeck() — GET .../deck is already a dedicated,
// envelope-free endpoint. Fetched once on mount (NotesDeckDrawer keeps this
// mounted after the drawer's first open, so switching Notes⇄Deck doesn't
// refetch — matching legacy's own html==="" cache check).
export function DeckPanel({ gameId }: { gameId: string }) {
  const [deck, setDeck] = useState<EnrichedDeck | null>(null);

  useEffect(() => {
    runRequest(api.get<EnrichedDeck>(`/game/${gameId}/deck`), 'Failed to load game deck', setDeck);
  }, [gameId]);

  return (
    <div className="flex-1 min-h-0 overflow-y-auto scrollable">
      {deck?.deck && <DeckView deck={deck.deck} details={deck.details} />}
    </div>
  );
}
