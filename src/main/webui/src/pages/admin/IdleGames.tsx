import { Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { api } from '../../api/client';
import type { IdleGame } from '../../api/types';
import { pathForGame } from '../../routes';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { adminTimestamp } from './adminFormatting';

export function IdleGames() {
  const queryClient = useQueryClient();
  const { data: idleGames = [] } = useQuery({
    queryKey: ['admin-page', 'idle-games'],
    queryFn: () => api.get<IdleGame[]>('/admin-page/idle-games'),
  });

  const closeGame = async (gameId: string) => {
    if (!(await confirmDialog('Are you sure you want to end this game?'))) return;
    runRequest(api.del(`/admin-page/games/${encodeURIComponent(gameId)}`), 'Failed to end game', () => {
      queryClient.invalidateQueries({ queryKey: ['admin-page', 'games'] });
      queryClient.invalidateQueries({ queryKey: ['admin-page', 'idle-games'] });
    });
  };

  const cell = 'jt:px-2 jt:py-1 jt:border-b jt:border-line/50 jt:align-top';

  return (
    <Card>
      <CardHeader>
        <CardTitle>Idle Games</CardTitle>
      </CardHeader>
      <div className="jt:overflow-auto jt:max-h-[70dvh]">
        <table className="jt:w-full jt:text-sm">
          <thead className="jt:sticky jt:top-0 jt:bg-panel">
            <tr className="jt:text-left jt:text-ink-muted">
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Name</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Last Update</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Player</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Timestamp</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Action</th>
            </tr>
          </thead>
          <tbody>
            {idleGames.map((g) => {
              const players = Object.entries(g.idlePlayers);
              return players.map(([player, timestamp], i) => (
                <tr key={`${g.gameId}-${player}`} className="jt:hover:bg-hover">
                  {i === 0 && (
                    <td className={cell} rowSpan={players.length}>
                      <Link to={pathForGame(g.gameId)} className="jt:text-accent jt:underline">
                        {g.gameName}
                      </Link>
                    </td>
                  )}
                  {i === 0 && (
                    <td className={cell} rowSpan={players.length}>
                      {adminTimestamp(g.gameTimestamp)}
                    </td>
                  )}
                  <td className={cell}>{player}</td>
                  <td className={cell}>{adminTimestamp(timestamp)}</td>
                  {i === 0 && (
                    <td className={cell} rowSpan={players.length}>
                      <Button variant="secondary" size="sm" onClick={() => closeGame(g.gameId)}>
                        Close
                      </Button>
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
