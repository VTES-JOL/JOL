import { Link } from 'react-router-dom';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import type { IdleGame } from '../../api/types';
import { pathForGame } from '../../routes';
import { confirmDialog } from '../../components/dialog';
import { runRequest } from '../../api/mutate';
import { adminTimestamp } from './adminFormatting';

export function IdleGames({ idleGames, onSaved }: { idleGames: IdleGame[]; onSaved: () => void }) {
  const closeGame = async (gameId: string) => {
    if (!(await confirmDialog('Are you sure you want to end this game?'))) return;
    runRequest(api.del(`/admin-page/games/${encodeURIComponent(gameId)}`), 'Failed to end game', onSaved);
  };

  return (
    <Card className="mt-2">
      <CardHeader>
        <CardTitle>Idle Games</CardTitle>
      </CardHeader>
      <div className="scrollable mhd-70">
        <table className="table table-sm table-hover mb-0">
          <thead>
            <tr>
              <th>Name</th>
              <th>Last Update</th>
              <th>Player</th>
              <th>Timestamp</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {idleGames.map((g) => {
              const players = Object.entries(g.idlePlayers);
              return players.map(([player, timestamp], i) => (
                <tr key={`${g.gameId}-${player}`}>
                  {i === 0 && (
                    <td rowSpan={players.length}>
                      <Link to={pathForGame(g.gameId)}>{g.gameName}</Link>
                    </td>
                  )}
                  {i === 0 && <td rowSpan={players.length}>{adminTimestamp(g.gameTimestamp)}</td>}
                  <td>{player}</td>
                  <td>{adminTimestamp(timestamp)}</td>
                  {i === 0 && (
                    <td rowSpan={players.length}>
                      <button className="btn btn-outline-secondary btn-sm" onClick={() => closeGame(g.gameId)}>
                        Close
                      </button>
                    </td>
                  )}
                </tr>
              ));
            })}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
