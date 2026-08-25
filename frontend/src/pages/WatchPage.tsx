import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { AllGames } from '../api/types';
import { ActiveGamesTab } from './watch/ActiveGamesTab';
import { PastGamesTab } from './watch/PastGamesTab';
import { StatsTab } from './watch/StatsTab';

type MainTab = 'active' | 'past' | 'stats';

// Active games' turn/timestamp fields update on every in-game action, but
// this page never joins any individual game's WS room (it's watching many
// games at once, not one) — so those per-game pushes never reach it. The
// ['watch'] invalidate (JolAdmin.createGame/startGame/endGame) only covers
// games appearing/closing. Poll to pick up in-game turn progress for games
// already in the list.
const ACTIVE_GAMES_POLL_MS = 20_000;

export function WatchPage() {
  const [tab, setTab] = useState<MainTab>('active');

  const { data } = useQuery({
    queryKey: ['watch'],
    queryFn: () => api.get<AllGames>('/watch'),
    refetchInterval: ACTIVE_GAMES_POLL_MS,
  });

  return (
    <div className="p-3 flex-fill d-flex flex-column min-h-0">
      <div className="card shadow flex-fill d-flex flex-column min-h-0">
        <div className="card-header bg-body-secondary p-0">
          <ul className="nav nav-tabs card-header-tabs ms-0 border-0">
            <li className="nav-item">
              <button className={`nav-link px-3 py-2 ${tab === 'active' ? 'active' : ''}`} onClick={() => setTab('active')}>
                Active Games
              </button>
            </li>
            <li className="nav-item">
              <button className={`nav-link px-3 py-2 ${tab === 'past' ? 'active' : ''}`} onClick={() => setTab('past')}>
                Past Games
              </button>
            </li>
            <li className="nav-item">
              <button className={`nav-link px-3 py-2 ${tab === 'stats' ? 'active' : ''}`} onClick={() => setTab('stats')}>
                Statistics
              </button>
            </li>
          </ul>
        </div>
        <div className="tab-content-fill flex-fill d-flex flex-column min-h-0">
          {data && tab === 'active' && <ActiveGamesTab games={data.games} />}
          {data && tab === 'past' && <PastGamesTab history={data.history} />}
          {tab === 'stats' && <StatsTab />}
        </div>
      </div>
    </div>
  );
}
