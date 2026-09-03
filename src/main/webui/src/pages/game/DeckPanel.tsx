import { useEffect, useState } from 'react';
import { NotebookPen } from 'lucide-react';
import { api } from '../../api/client';
import type { EnrichedDeck } from '../../api/types';
import { DeckView } from '../../components/DeckView';
import { runRequest } from '../../api/mutate';
import { GamePanel } from './GamePanel';

// Mirrors game-deck.jsp/doShowDeck() — GET .../deck is already a dedicated,
// envelope-free endpoint. Fetched once (like legacy's own html==="" cache
// check), not refetched on every toggle back to this panel.
export function DeckPanel({ gameId, onToggleNotes }: { gameId: string; onToggleNotes: () => void }) {
  const [deck, setDeck] = useState<EnrichedDeck | null>(null);

  useEffect(() => {
    runRequest(api.get<EnrichedDeck>(`/game/${gameId}/deck`), 'Failed to load game deck', setDeck);
  }, [gameId]);

  return (
    <GamePanel
      id="gameDeckCard"
      bodyClassName="scrollable"
      title="Deck"
      toggle={{ icon: <NotebookPen size={13} />, label: 'Notes', onClick: onToggleNotes }}
    >
      {deck?.deck && <DeckView deck={deck.deck} details={deck.details} />}
    </GamePanel>
  );
}
