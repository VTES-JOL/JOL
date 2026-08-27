import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { GameStatusBean } from '../api/types';
import { GameList } from './lobby/GameList';
import { GameCreateForm } from './lobby/GameCreateForm';
import { GameDetail } from './lobby/GameDetail';
import { PageLoading } from '../components/PageLoading';
import { EmptyState } from '../components/EmptyState';
import { SplitLayout } from '../components/SplitLayout';

type View = { mode: 'create' } | { mode: 'detail'; gameName: string } | null;

const GAMES_QUERY_KEY = ['lobby', 'games'];

// Each widget below fetches its own slice (see lobby/*.tsx) — registering a
// deck in GameDetail no longer forces GameCreateForm's reference data to
// refetch, and vice versa. See LobbyResource for the backend side of this
// same split (was previously one combined LobbyPageBean).
//
//  - no manual useEffect(refresh, []) — useQuery owns the initial fetch,
//    caching, and re-fetch-on-error/refocus policy
//  - no manual WS subscription here — useQueryInvalidation, mounted once
//    near the app root, invalidates ['lobby'] for us whenever the backend
//    pushes {"type":"invalidate","key":["lobby"]}
//  - mutations invalidate the games query instead of applying a returned
//    page object — this page no longer owns the games list, the query cache does
export function LobbyPage() {
  const queryClient = useQueryClient();
  const [view, setView] = useState<View>(null);

  const { data: games } = useQuery({
    queryKey: GAMES_QUERY_KEY,
    queryFn: () => api.get<GameStatusBean[]>('/lobby/player/games'),
  });

  const refresh = () => queryClient.invalidateQueries({ queryKey: GAMES_QUERY_KEY });

  const selectGame = (game: GameStatusBean) => setView({ mode: 'detail', gameName: game.name });

  if (!games) return <PageLoading />;

  const selectedGame = view?.mode === 'detail' ? games.find((g) => g.name === view.gameName) : null;

  return (
    <SplitLayout
      stackBelowLg
      left={
        <GameList
          games={games}
          selectedName={selectedGame?.name ?? null}
          onSelect={selectGame}
          onNew={() => setView({ mode: 'create' })}
        />
      }
      right={
        <>
          {view?.mode === 'create' && (
            <GameCreateForm
              onCancel={() => setView(null)}
              onCreated={(gameName) => {
                refresh();
                setView({ mode: 'detail', gameName });
              }}
            />
          )}
          {selectedGame && <GameDetail game={selectedGame} onClose={() => setView(null)} onChanged={refresh} />}
          {!view && <EmptyState icon="bi-controller" message="Select a game or create a new one" />}
        </>
      }
    />
  );
}
