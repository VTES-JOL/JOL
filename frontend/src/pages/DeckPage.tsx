import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { DeckInfoBean, DeckPage as DeckPageData } from '../api/types';
import { DeckListPanel } from './deck/DeckListPanel';
import { DeckEditor } from './deck/DeckEditor';
import { DeckPreviewPanel } from './deck/DeckPreviewPanel';
import { runRequest } from '../api/mutate';
import { PageLoading } from '../components/PageLoading';
import './DeckPage.css';

const PAGE_QUERY_KEY = ['decks', 'page'];
const DECK_FILTER_STORAGE_KEY = 'deckFilter';

export function DeckPage() {
  const queryClient = useQueryClient();
  // Remembered per-browser (not per-player-on-the-server) — a SPA-appropriate
  // home for a UI preference like this.
  const [deckFilter, setDeckFilter] = useState(() => localStorage.getItem(DECK_FILTER_STORAGE_KEY) ?? '');
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    localStorage.setItem(DECK_FILTER_STORAGE_KEY, deckFilter);
  }, [deckFilter]);

  const { data } = useQuery({
    queryKey: PAGE_QUERY_KEY,
    queryFn: () => api.get<DeckPageData>('/decks/player'),
  });

  const { data: decks = [] } = useQuery({
    queryKey: ['decks', 'list', deckFilter],
    queryFn: () => api.get<DeckInfoBean[]>(`/decks?filter=${encodeURIComponent(deckFilter)}`),
  });

  if (!data) return <PageLoading />;

  const applyPage = (page: DeckPageData) => queryClient.setQueryData(PAGE_QUERY_KEY, page);
  const refreshDeckList = () => queryClient.invalidateQueries({ queryKey: ['decks', 'list'] });

  const newDeck = () => {
    runRequest(api.post<DeckPageData>('/decks/player/new'), 'Failed to start new deck', (page) => {
      applyPage(page);
      setEditing(true);
    });
  };

  const loadDeck = (deckName: string) => {
    runRequest(api.post<DeckPageData>('/decks/player/load', { deckName }), 'Failed to load deck', (page) => {
      applyPage(page);
      setEditing(false);
    });
  };

  const deleteDeck = (deckName: string) => {
    runRequest(api.del<DeckPageData>(`/decks/player/${encodeURIComponent(deckName)}`), 'Failed to delete deck', (page) => {
      applyPage(page);
      refreshDeckList();
    });
  };

  const saveDeck = (deckName: string, contents: string, comment: string) => {
    runRequest(
      api.post<DeckPageData>('/decks/player', { deckName, contents, comment }),
      'Failed to save deck',
      (page) => {
        applyPage(page);
        setEditing(false);
        refreshDeckList();
      },
    );
  };

  const validate = (name: string, contents: string, format: string) => {
    runRequest(api.post<DeckPageData>('/decks/player/validate', { name, contents, format }), 'Failed to validate deck', applyPage);
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
