import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Gamepad2 } from 'lucide-react';
import { api } from '../api/client';
import type { GameStatusBean } from '../api/types';
import { GameList } from './lobby/GameList';
import { GameCreateForm } from './lobby/GameCreateForm';
import { GameDetail } from './lobby/GameDetail';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { MasterDetailView } from '../components/ui/MasterDetailView';
import { useInvalidate } from '../api/useInvalidate';

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
  const [view, setView] = useState<View>(null);

  const { data: games } = useQuery({
    queryKey: GAMES_QUERY_KEY,
    queryFn: () => api.get<GameStatusBean[]>('/lobby/player/games'),
  });

  const refresh = useInvalidate(GAMES_QUERY_KEY);

  if (!games) {
    return (
      <div className="jt-scope jt:flex jt:flex-1 jt:min-h-0 jt:items-center jt:justify-center jt:bg-base">
        <Spinner />
      </div>
    );
  }

  const selectedGame = view?.mode === 'detail' ? games.find((g) => g.name === view.gameName) : null;
  const showDetailPane = view?.mode === 'create' || !!selectedGame;

  let detail;
  if (view?.mode === 'create') {
    detail = (
      <GameCreateForm
        onCancel={() => setView(null)}
        onCreated={(gameName) => {
          refresh();
          setView({ mode: 'detail', gameName });
        }}
      />
    );
  } else if (selectedGame) {
    detail = <GameDetail game={selectedGame} onClose={() => setView(null)} onChanged={refresh} />;
  } else {
    detail = <EmptyState icon={Gamepad2} title="Select a game or create a new one" />;
  }

  return (
    <div className="jt-scope jt:flex jt:flex-col jt:flex-1 jt:min-h-0 jt:p-4 jt:bg-base jt:text-ink">
      <MasterDetailView
        breakpoint="lg"
        columns="340px minmax(360px, 1fr)"
        activeKey={showDetailPane ? 'detail' : 'list'}
        onActiveKeyChange={(k) => {
          if (k === 'list') setView(null);
        }}
        panels={[
          {
            key: 'list',
            label: 'Games',
            content: (
              <GameList
                games={games}
                selectedName={selectedGame?.name ?? null}
                onSelect={(game) => setView({ mode: 'detail', gameName: game.name })}
                onNew={() => setView({ mode: 'create' })}
              />
            ),
          },
          { key: 'detail', label: 'Details', content: detail },
        ]}
      />
    </div>
  );
}
