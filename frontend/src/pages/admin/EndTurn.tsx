import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import { confirmDialog } from '../../components/dialog';
import { runRequest } from '../../api/mutate';

export function EndTurn() {
  const { data: games = {} } = useQuery({
    queryKey: ['admin-page', 'games'],
    queryFn: () => api.get<Record<string, string>>('/admin-page/games'),
  });
  const gameIds = Object.keys(games);
  const [gameId, setGameId] = useState('');

  useEffect(() => {
    if (!gameId && gameIds.length > 0) setGameId(gameIds[0]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [gameIds.length]);

  const submit = async () => {
    if (!gameId) return;
    if (!(await confirmDialog(`Are you sure you want to end turn for ${games[gameId]}`))) return;
    runRequest(api.post(`/admin-page/games/${encodeURIComponent(gameId)}/end-turn`), 'Failed to end turn');
  };

  return (
    <Card className="mt-2">
      <CardHeader>
        <CardTitle>End Turn</CardTitle>
      </CardHeader>
      <div className="card-body">
        <label htmlFor="endTurnList" className="form-label">
          Games
        </label>
        <select id="endTurnList" className="form-select" value={gameId} onChange={(e) => setGameId(e.target.value)}>
          {gameIds.map((id) => (
            <option key={id} value={id}>
              {games[id]}
            </option>
          ))}
        </select>
        <button onClick={submit} className="btn btn-outline-secondary btn-sm mt-2">
          End Turn
        </button>
      </div>
    </Card>
  );
}
