import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { GameStatusBean, LobbyPage as LobbyPageData } from '../api/types';
import { GameList } from './lobby/GameList';
import { GameCreateForm } from './lobby/GameCreateForm';
import { GameDetail } from './lobby/GameDetail';
import { PageLoading } from '../components/PageLoading';
import { EmptyState } from '../components/EmptyState';

type View = { mode: 'create' } | { mode: 'detail'; gameName: string } | null;

const LOBBY_QUERY_KEY = ['lobby'];

// Prototype of the TanStack Query approach (see LobbyResource.getLobbyAndInvalidate
// and ws/useQueryInvalidation.ts on the backend/bridge side). Compared to
// every other page in this app:
//  - no manual useEffect(refresh, []) — useQuery owns the initial fetch,
//    caching, and re-fetch-on-error/refocus policy
//  - no useJolSocket('main:games', refresh) here — useQueryInvalidation,
//    mounted once near the app root, invalidates ['lobby'] for us whenever
//    the backend pushes {"type":"invalidate","key":["lobby"]}
//  - mutation responses write straight into the cache via setQueryData
//    instead of local setState, so this component no longer owns the data
//    at all — the query cache does
export function LobbyPage() {
  const queryClient = useQueryClient();
  const [view, setView] = useState<View>(null);

  const { data } = useQuery({
    queryKey: LOBBY_QUERY_KEY,
    queryFn: () => api.get<LobbyPageData>('/lobby/player/games'),
  });

  const applyUpdate = (updated: LobbyPageData) => queryClient.setQueryData(LOBBY_QUERY_KEY, updated);

  const selectGame = (game: GameStatusBean) => setView({ mode: 'detail', gameName: game.name });

  if (!data) return <PageLoading />;

  const selectedGame = view?.mode === 'detail' ? data.games.find((g) => g.name === view.gameName) : null;

  return (
    <div className="row g-2 flex-fill align-items-stretch min-h-0 p-3">
      <div className="col-lg-4 d-flex flex-column">
        <GameList
          games={data.games}
          selectedName={selectedGame?.name ?? null}
          onSelect={selectGame}
          onNew={() => setView({ mode: 'create' })}
        />
      </div>
      <div className="col-lg-8 d-flex flex-column">
        {view?.mode === 'create' && (
          <GameCreateForm
            players={data.players}
            gameFormats={data.gameFormats}
            onCancel={() => setView(null)}
            onCreated={(updated, gameName) => {
              applyUpdate(updated);
              setView({ mode: 'detail', gameName });
            }}
          />
        )}
        {selectedGame && (
          <GameDetail
            game={selectedGame}
            players={data.players}
            decks={data.decks}
            message={data.message}
            onClose={() => setView(null)}
            onChanged={applyUpdate}
          />
        )}
        {!view && <EmptyState icon="bi-controller" message="Select a game or create a new one" />}
      </div>
    </div>
  );
}
