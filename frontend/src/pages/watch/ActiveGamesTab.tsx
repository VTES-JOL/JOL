import { Link } from 'react-router-dom';
import type { GameSummary } from '../../api/types';
import { pathForGame } from '../../routes';

const TIMESTAMP_FORMAT = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'UTC',
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
});

export function ActiveGamesTab({ games }: { games: GameSummary[] }) {
  return (
    <div className="card shadow flex-fill d-flex flex-column">
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
