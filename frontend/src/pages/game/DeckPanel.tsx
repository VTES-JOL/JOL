import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Deck } from '../../api/types';
import { DeckPreview } from '../../components/DeckPreview';
import { showError } from '../../components/toast';
import { Panel } from './Panel';

// Mirrors game-deck.jsp/doShowDeck() — GET .../deck is already a dedicated,
// envelope-free endpoint. Fetched once (like legacy's own html==="" cache
// check), not refetched on every toggle back to this panel.
export function DeckPanel({ gameId, onToggleNotes }: { gameId: string; onToggleNotes: () => void }) {
  const [deck, setDeck] = useState<Deck | null>(null);

  useEffect(() => {
    api
      .get<Deck>(`/game/${gameId}/deck`)
      .then(setDeck)
      .catch((err) => {
        console.error('Failed to load game deck', err);
        showError('Failed to load game deck.');
      });
  }, [gameId]);

  return (
    <Panel id="gameDeckCard" bodyClassName="p-2 scrollable" title="Deck" toggle={{ icon: 'bi-journal', label: 'Notes', onClick: onToggleNotes }}>
      {deck && <DeckPreview deck={deck} />}
    </Panel>
  );
}
