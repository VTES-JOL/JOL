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

  const cell = 'px-2 py-1 border-b border-line/50 align-top';

  return (
    <Card>
      <CardHeader>
        <CardTitle>Idle Games</CardTitle>
      </CardHeader>
      <div className="overflow-auto max-h-[70dvh]">
        <table className="w-full text-sm">
          <thead className="sticky top-0 bg-panel">
            <tr className="text-left text-ink-muted">
              <th className="px-2 py-1.5 font-semibold border-b border-line">Name</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Last Update</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Player</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Timestamp</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Action</th>
            </tr>
          </thead>
          <tbody>
            {idleGames.map((g) => {
              const players = Object.entries(g.idlePlayers);
              return players.map(([player, timestamp], i) => (
                <tr key={`${g.gameId}-${player}`} className="hover:bg-hover">
                  {i === 0 && (
                    <td className={cell} rowSpan={players.length}>
                      <Link to={pathForGame(g.gameId)} className="text-accent underline">
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
