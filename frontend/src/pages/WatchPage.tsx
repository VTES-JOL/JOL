import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { AllGames } from '../api/types';
import { ActiveGamesTab } from './watch/ActiveGamesTab';
import { PastGamesTab } from './watch/PastGamesTab';
import { StatsTab } from './watch/StatsTab';
import { showError } from '../components/toast';

type MainTab = 'active' | 'past' | 'stats';

export function WatchPage() {
  const [data, setData] = useState<AllGames | null>(null);
  const [tab, setTab] = useState<MainTab>('active');

  useEffect(() => {
    api
      .get<AllGames>('/watch')
      .then(setData)
      .catch((err) => {
        console.error('Failed to load watch page', err);
        showError('Failed to load watch page.');
      });
  }, []);

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
