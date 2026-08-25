import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { DeckInfoBean, DeckPage as DeckPageData } from '../api/types';
import { DeckListPanel } from './deck/DeckListPanel';
import { DeckEditor } from './deck/DeckEditor';
import { DeckPreviewPanel } from './deck/DeckPreviewPanel';
import { showError } from '../components/toast';
import { PageLoading } from '../components/PageLoading';
import './DeckPage.css';

const PAGE_QUERY_KEY = ['decks', 'page'];

export function DeckPage() {
  const queryClient = useQueryClient();
  const [deckFilter, setDeckFilter] = useState('');
  const [editing, setEditing] = useState(false);

  const { data } = useQuery({
    queryKey: PAGE_QUERY_KEY,
    queryFn: () => api.get<DeckPageData>('/decks/player'),
  });

  // Seed the filter from the page's saved preference on first load only —
  // later mutations (new/load/save/delete/validate) also write into this
  // same query's cache via setQueryData below, and re-seeding from those
  // would stomp whatever the user has since typed into the filter box.
  const [hasSeededFilter, setHasSeededFilter] = useState(false);
  useEffect(() => {
    if (data && !hasSeededFilter) {
      setDeckFilter(data.deckFilter);
      setHasSeededFilter(true);
    }
  }, [data, hasSeededFilter]);

  const { data: decks = [] } = useQuery({
    queryKey: ['decks', 'list', deckFilter],
    queryFn: () => api.get<DeckInfoBean[]>(`/decks?filter=${encodeURIComponent(deckFilter)}`),
    enabled: hasSeededFilter,
  });

  if (!data) return <PageLoading />;

  const applyPage = (page: DeckPageData) => queryClient.setQueryData(PAGE_QUERY_KEY, page);
  const refreshDeckList = () => queryClient.invalidateQueries({ queryKey: ['decks', 'list'] });

  const newDeck = () => {
    api
      .post<DeckPageData>('/decks/player/new')
      .then((page) => {
        applyPage(page);
        setEditing(true);
      })
      .catch((err) => {
        console.error('Failed to start new deck', err);
        showError('Failed to start new deck.');
      });
  };

  const loadDeck = (deckName: string) => {
    api
      .post<DeckPageData>('/decks/player/load', { deckName })
      .then((page) => {
        applyPage(page);
        setEditing(false);
      })
      .catch((err) => {
        console.error('Failed to load deck', err);
        showError('Failed to load deck.');
      });
  };

  const deleteDeck = (deckName: string) => {
    api
      .del<DeckPageData>(`/decks/player/${encodeURIComponent(deckName)}`)
      .then((page) => {
        applyPage(page);
        refreshDeckList();
      })
      .catch((err) => {
        console.error('Failed to delete deck', err);
        showError('Failed to delete deck.');
      });
  };

  const saveDeck = (deckName: string, contents: string, comment: string) => {
    api
      .post<DeckPageData>('/decks/player', { deckName, contents, comment })
      .then((page) => {
        applyPage(page);
        setEditing(false);
        refreshDeckList();
      })
      .catch((err) => {
        console.error('Failed to save deck', err);
        showError('Failed to save deck.');
      });
  };

  const validate = (contents: string, format: string) => {
    api
      .post<DeckPageData>('/decks/player/validate', { contents, format })
      .then(applyPage)
      .catch((err) => {
        console.error('Failed to validate deck', err);
        showError('Failed to validate deck.');
      });
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
