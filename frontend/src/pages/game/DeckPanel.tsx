import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Deck } from '../../api/types';
import { DeckPreview } from '../../components/DeckPreview';
import { showError } from '../../components/toast';

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
    <div className="card shadow" id="gameDeckCard">
      <div className="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>Deck</span>
        <button className="border-0 shadow rounded-pill bg-light" onClick={onToggleNotes}>
          <i className="bi bi-journal me-2" />
          Notes
        </button>
      </div>
      <div className="card-body p-2 scrollable">{deck && <DeckPreview deck={deck} />}</div>
    </div>
  );
}
