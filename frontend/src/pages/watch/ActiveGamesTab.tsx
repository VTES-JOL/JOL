import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../api/client';
import type { GameSummary } from '../../api/types';
import { pathForGame } from '../../routes';

const TIMESTAMP_FORMAT = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'UTC',
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
});

// Active games' turn/timestamp fields update on every in-game action, but
// this page never joins any individual game's WS room (it's watching many
// games at once, not one) — so those per-game pushes never reach it. The
// ['watch','active'] invalidate (JolAdmin.createGame/startGame/endGame) only
// covers games appearing/closing. Poll to pick up in-game turn progress for
// games already in the list.
const ACTIVE_GAMES_POLL_MS = 20_000;

export function ActiveGamesTab() {
  const { data: games = [] } = useQuery({
    queryKey: ['watch', 'active'],
    queryFn: () => api.get<GameSummary[]>('/watch/active'),
    refetchInterval: ACTIVE_GAMES_POLL_MS,
  });

  return (
    <div className="card shadow flex-fill d-flex flex-column min-h-0">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">Active Games</span>
      </div>
      <div className="flex-fill min-h-0" style={{ overflowY: 'auto', overflowX: 'clip' }}>
        <table className="table table-sm table-hover mb-0">
          <thead>
            <tr>
              <th>Game</th>
              <th>Current Turn</th>
              <th>Updated</th>
            </tr>
          </thead>
          <tbody>
            {games.map((g) => (
              <tr key={g.gameId}>
                <td>
                  <Link to={pathForGame(g.gameId)}>{g.gameName}</Link>
                </td>
                <td>{g.turn}</td>
                <td>{TIMESTAMP_FORMAT.format(new Date(g.timestamp))} UTC</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
