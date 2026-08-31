import { useCallback, useEffect, useMemo, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { FolderOpen } from 'lucide-react';
import { api } from '../api/client';
import { runRequest } from '../api/mutate';
import { useInvalidate } from '../api/useInvalidate';
import type { CardDetail, DeckInfoBean, DeckPageBean } from '../api/types';
import { Spinner } from '../components/ui/Spinner';
import { MasterDetailView } from '../components/ui/MasterDetailView';
import { Panel } from '../components/ui/Panel';
import { EmptyState } from '../components/ui/EmptyState';
import type { DeckEntry } from './deck/deckKit';
import { deckApi } from './deck/deckApi';
import { DeckListPane } from './deck/DeckListPane';
import { DeckEditorPane } from './deck/DeckEditorPane';
import { DeckAnalyticsPanel } from './deck/DeckAnalyticsPanel';
import { DeckImportModal } from './deck/DeckImportModal';
import { enrichEntries, entriesFromExtendedDeck, entryIds } from './deck/deckEntries';

const PAGE_KEY = ['decks', 'page'];
const LIST_KEY = ['decks', 'list'];
const FORMAT_FILTER_KEY = 'deckFilter';

/**
 * The deck workbench at /jol/deck: a 3-pane master/detail layout — deck list,
 * structured card editor, and live analytics.
 *
 * The editor pane is keyed by deck id and owns a working `DeckEntry[]` that
 * +/−, add-card and import mutate, with a debounced write-through to
 * `POST /decks/player` (which also recomputes the game-format tags and
 * per-format validity server-side). Inline rename does a save-as under the new
 * name followed by a delete of the old row.
 */
export function DeckPage() {
  const queryClient = useQueryClient();
  const [formatFilter, setFormatFilter] = useState(() => localStorage.getItem(FORMAT_FILTER_KEY) ?? '');
  const [detailMap, setDetailMap] = useState<Map<string, CardDetail>>(new Map());
  // The editor pane's working entries, mirrored up for the analytics pane.
  // Null until the first edit of the current deck → analytics falls back to
  // the server-loaded deck. Reset (render-phase) whenever the deck changes.
  const [liveEntries, setLiveEntries] = useState<DeckEntry[] | null>(null);
  const [liveEntriesDeckId, setLiveEntriesDeckId] = useState<string | null>(null);
  const [showImport, setShowImport] = useState(false);
  // Which pane is shown in the collapsed (mobile) layout. Picking a deck jumps
  // to the editor; deleting drops back to the list.
  const [panelKey, setPanelKey] = useState<'list' | 'editor' | 'analytics'>('list');

  useEffect(() => {
    localStorage.setItem(FORMAT_FILTER_KEY, formatFilter);
  }, [formatFilter]);

  const { data: page } = useQuery({
    queryKey: PAGE_KEY,
    queryFn: () => api.get<DeckPageBean>('/decks/player'),
  });

  const { data: decks = [] } = useQuery({
    queryKey: [...LIST_KEY, formatFilter],
    queryFn: () => api.get<DeckInfoBean[]>(`/decks?filter=${encodeURIComponent(formatFilter)}`),
  });

  const baseEntries = useMemo(() => entriesFromExtendedDeck(page?.selectedDeck ?? null), [page?.selectedDeck]);

  // Reset the mirrored working entries when the selected deck changes
  // (React's "adjust state during render" pattern — no effect, no flash).
  const currentDeckId = page?.deckId ?? null;
  if (currentDeckId !== liveEntriesDeckId) {
    setLiveEntriesDeckId(currentDeckId);
    setLiveEntries(null);
  }

  const analyticsEntries = useMemo(
    () => enrichEntries(liveEntries ?? baseEntries, detailMap),
    [liveEntries, baseEntries, detailMap],
  );

  // Fetch display details for the loaded deck's cards; merge (don't replace)
  // so details for cards added mid-session survive a deck reload.
  useEffect(() => {
    const ids = entryIds(baseEntries);
    if (ids.length === 0) return;
    let active = true;
    deckApi.cardDetails(ids).then((details) => {
      if (active && details.length) {
        setDetailMap((prev) => {
          const next = new Map(prev);
          details.forEach((d) => next.set(d.id, d));
          return next;
        });
      }
    });
    return () => {
      active = false;
    };
  }, [baseEntries]);

  const applyPage = useCallback(
    (next: DeckPageBean) => queryClient.setQueryData(PAGE_KEY, next),
    [queryClient],
  );

  const refreshList = useInvalidate(LIST_KEY);

  const addCardDetail = useCallback((card: CardDetail) => {
    setDetailMap((prev) => (prev.has(card.id) ? prev : new Map(prev).set(card.id, card)));
  }, []);

  const loadDeck = useCallback(
    (deck: DeckInfoBean) =>
      runRequest(api.post<DeckPageBean>('/decks/player/load', { deckName: deck.name }), 'Failed to load deck', (next) => {
        applyPage(next);
        setPanelKey('editor'); // focus the detail pane in the collapsed layout
      }),
    [applyPage],
  );

  const saveDeck = useCallback(
    async (deckName: string, contents: string, comment: string) => {
      const next = await api.post<DeckPageBean>('/decks/player', { deckName, contents, comment });
      applyPage(next);
      refreshList();
    },
    [applyPage, refreshList],
  );

  // jol creates the deck row on first save, so "New" persists an empty deck
  // under the first free "New Deck [n]" name and opens it (rename it inline
  // from the editor header).
  const newDeck = useCallback(() => {
    const taken = new Set(decks.map((d) => d.name));
    let name = 'New Deck';
    for (let i = 2; taken.has(name); i++) name = `New Deck ${i}`;
    return runRequest(saveDeck(name, '', ''), 'Failed to start new deck', () => setPanelKey('editor'));
  }, [decks, saveDeck]);

  // Rename = save the current contents under the new name, then drop the old
  // row (ignoring its now-empty page), and show the freshly-created deck.
  const renameDeck = useCallback(
    async (oldName: string, newName: string, contents: string, comment: string) => {
      const next = await api.post<DeckPageBean>('/decks/player', { deckName: newName, contents, comment });
      await api.del(`/decks/player/${encodeURIComponent(oldName)}`).catch(() => {});
      applyPage(next);
      refreshList();
    },
    [applyPage, refreshList],
  );

  const deleteDeck = useCallback(
    (deckName: string) =>
      runRequest(
        api.del<DeckPageBean>(`/decks/player/${encodeURIComponent(deckName)}`),
        'Failed to delete deck',
        (next) => {
          applyPage(next);
          refreshList();
          setPanelKey('list');
        },
      ),
    [applyPage, refreshList],
  );

  const importDeck = useCallback(
    async (name: string, entries: Array<{ cardId: string; count: number }>, comment: string | null) => {
      const next = await deckApi.importDeck(name, entries, comment);
      applyPage(next);
      refreshList();
      setShowImport(false);
      setPanelKey('editor');
    },
    [applyPage, refreshList],
  );

  if (!page) {
    return (
      <div className="flex flex-1 min-h-0 items-center justify-center bg-base">
        <Spinner />
      </div>
    );
  }

  const selectedId = page.deckId;
  const selectedName = page.selectedDeck?.deck.name ?? '';

  return (
    <div className="flex flex-col flex-1 min-h-0 p-4 bg-base text-ink">
      <MasterDetailView
        breakpoint="lg"
        columns="300px minmax(360px, 1fr) 300px"
        activeKey={selectedId == null && panelKey !== 'list' ? 'list' : panelKey}
        onActiveKeyChange={(k) => setPanelKey(k as 'list' | 'editor' | 'analytics')}
        panels={[
          {
            key: 'list',
            label: 'My Decks',
            content: (
              <DeckListPane
                decks={decks}
                tags={page.tags}
                selectedId={selectedId}
                formatFilter={formatFilter}
                onFormatFilterChange={setFormatFilter}
                onSelect={loadDeck}
                onNew={newDeck}
                onImport={() => setShowImport(true)}
              />
            ),
          },
          {
            key: 'editor',
            label: selectedName ? `Deck: ${selectedName}` : 'Deck Editor',
            content:
              selectedId == null ? (
                <Panel title="Deck Editor">
                  <EmptyState
                    icon={FolderOpen}
                    title="No deck selected"
                    description="Choose a deck from the list, or start a new one."
                  />
                </Panel>
              ) : (
                <DeckEditorPane
                  key={selectedId}
                  deckName={selectedName}
                  initialEntries={baseEntries}
                  initialComment={page.selectedDeck?.deck.comments ?? ''}
                  detailMap={detailMap}
                  formatValidity={page.formatValidity}
                  onAddCardDetail={addCardDetail}
                  onEntriesChange={setLiveEntries}
                  onSave={(contents, comment) => saveDeck(selectedName, contents, comment)}
                  onRename={(newName, contents, comment) => renameDeck(selectedName, newName, contents, comment)}
                  onDelete={() => deleteDeck(selectedName)}
                  onSearch={deckApi.cardAutocomplete}
                />
              ),
          },
          {
            key: 'analytics',
            label: 'Analytics',
            content:
              selectedId == null ? (
                <Panel title="Analytics">
                  <EmptyState
                    icon={FolderOpen}
                    title="No deck selected"
                    description="Select a deck to see its analytics."
                  />
                </Panel>
              ) : (
                <DeckAnalyticsPanel entries={analyticsEntries} detailMap={detailMap} />
              ),
          },
        ]}
      />

      {showImport && <DeckImportModal onImport={importDeck} onClose={() => setShowImport(false)} />}
    </div>
  );
}
