import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { DeckInfoBean, DeckPage as DeckPageData } from '../api/types';
import { DeckListPanel } from './deck/DeckListPanel';
import { DeckEditor } from './deck/DeckEditor';
import { DeckPreviewPanel } from './deck/DeckPreviewPanel';

export function DeckPage() {
  const [data, setData] = useState<DeckPageData | null>(null);
  const [decks, setDecks] = useState<DeckInfoBean[]>([]);
  const [deckFilter, setDeckFilter] = useState('');
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    api
      .get<DeckPageData>('/decks/player')
      .then((page) => {
        setData(page);
        setDeckFilter(page.deckFilter);
      })
      .catch((err) => console.error('Failed to load deck page', err));
  }, []);

  useEffect(() => {
    api
      .get<DeckInfoBean[]>(`/decks?filter=${encodeURIComponent(deckFilter)}`)
      .then(setDecks)
      .catch((err) => console.error('Failed to load deck list', err));
  }, [deckFilter]);

  if (!data) return null;

  const newDeck = () => {
    api
      .post<DeckPageData>('/decks/player/new')
      .then((page) => {
        setData(page);
        setEditing(true);
      })
      .catch((err) => console.error('Failed to start new deck', err));
  };

  const loadDeck = (deckName: string) => {
    api
      .post<DeckPageData>('/decks/player/load', { deckName })
      .then((page) => {
        setData(page);
        setEditing(false);
      })
      .catch((err) => console.error('Failed to load deck', err));
  };

  const deleteDeck = (deckName: string) => {
    api
      .del<DeckPageData>(`/decks/player/${encodeURIComponent(deckName)}`)
      .then((page) => {
        setData(page);
        api
          .get<DeckInfoBean[]>(`/decks?filter=${encodeURIComponent(deckFilter)}`)
          .then(setDecks)
          .catch((err) => console.error('Failed to reload deck list', err));
      })
      .catch((err) => console.error('Failed to delete deck', err));
  };

  const saveDeck = (deckName: string, contents: string, comment: string) => {
    api
      .post<DeckPageData>('/decks/player', { deckName, contents, comment })
      .then((page) => {
        setData(page);
        setEditing(false);
        api
          .get<DeckInfoBean[]>(`/decks?filter=${encodeURIComponent(deckFilter)}`)
          .then(setDecks)
          .catch((err) => console.error('Failed to reload deck list', err));
      })
      .catch((err) => console.error('Failed to save deck', err));
  };

  const validate = (contents: string, format: string) => {
    api
      .post<DeckPageData>('/decks/player/validate', { contents, format })
      .then(setData)
      .catch((err) => console.error('Failed to validate deck', err));
  };

  return (
    <div className="deck-layout p-3">
      <div className="deck-col-left">
        <DeckListPanel
          decks={decks}
          tags={data.tags}
          deckFilter={deckFilter}
          onFilterChange={setDeckFilter}
          onSelect={loadDeck}
          onDelete={deleteDeck}
          onNew={newDeck}
        />
      </div>
      <div className="deck-col-right">
        {editing ? (
          <DeckEditor
            selectedDeck={data.selectedDeck}
            contents={data.contents}
            tags={data.tags}
            deckFilter={deckFilter}
            onSave={saveDeck}
            onValidate={validate}
            onCancel={() => setEditing(false)}
          />
        ) : (
          <DeckPreviewPanel selectedDeck={data.selectedDeck} onEdit={() => setEditing(true)} />
        )}
      </div>
    </div>
  );
}
