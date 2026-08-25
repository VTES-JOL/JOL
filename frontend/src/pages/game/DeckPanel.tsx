import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Deck } from '../../api/types';
import { DeckPreview } from '../../components/DeckPreview';
import { runRequest } from '../../api/mutate';
import { Panel } from './Panel';

// Mirrors game-deck.jsp/doShowDeck() — GET .../deck is already a dedicated,
// envelope-free endpoint. Fetched once (like legacy's own html==="" cache
// check), not refetched on every toggle back to this panel.
export function DeckPanel({ gameId, onToggleNotes }: { gameId: string; onToggleNotes: () => void }) {
  const [deck, setDeck] = useState<Deck | null>(null);

  useEffect(() => {
    runRequest(api.get<Deck>(`/game/${gameId}/deck`), 'Failed to load game deck', setDeck);
  }, [gameId]);

  return (
    <Panel id="gameDeckCard" bodyClassName="p-2 scrollable" title="Deck" toggle={{ icon: 'bi-journal', label: 'Notes', onClick: onToggleNotes }}>
      {deck && <DeckPreview deck={deck} />}
    </Panel>
  );
}
