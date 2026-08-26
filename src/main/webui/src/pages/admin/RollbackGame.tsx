import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import { confirmDialog } from '../../components/dialog';
import { runRequest } from '../../api/mutate';

export function RollbackGame() {
  const { data: games = {} } = useQuery({
    queryKey: ['admin-page', 'games'],
    queryFn: () => api.get<Record<string, string>>('/admin-page/games'),
  });
  const gameIds = Object.keys(games);
  const [gameId, setGameId] = useState('');
  const [turns, setTurns] = useState<string[]>([]);
  const [turn, setTurn] = useState('');

  useEffect(() => {
    if (!gameId && gameIds.length > 0) setGameId(gameIds[0]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [gameIds.length]);

  useEffect(() => {
    if (!gameId) {
      setTurns([]);
      return;
    }
    api
      .get<string[]>(`/game/${encodeURIComponent(gameId)}/turns`)
      .then((t) => {
        setTurns(t);
        setTurn(t[0] ?? '');
      })
      .catch((err) => console.error('Failed to load game turns', err));
  }, [gameId]);

  const submit = async () => {
    if (!gameId || !turn) return;
    if (!(await confirmDialog(`Are you sure you want to rollback to turn ${turn} for ${games[gameId]}`))) return;
    runRequest(api.post(`/admin-page/games/${encodeURIComponent(gameId)}/rollback`, { turn }), 'Failed to rollback game');
  };

  return (
    <Card className="mt-2">
      <CardHeader>
        <CardTitle>Rollback Game</CardTitle>
      </CardHeader>
      <div className="card-body">
        <label htmlFor="rollbackGamesList" className="form-label">
          Games
        </label>
        <select
          id="rollbackGamesList"
          className="form-select"
          value={gameId}
          onChange={(e) => setGameId(e.target.value)}
        >
          {gameIds.map((id) => (
            <option key={id} value={id}>
              {games[id]}
            </option>
          ))}
        </select>
        <label htmlFor="rollbackTurnsList" className="form-label">
          Turns
        </label>
        <select id="rollbackTurnsList" className="form-select" value={turn} onChange={(e) => setTurn(e.target.value)}>
          {turns.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <button onClick={submit} className="btn btn-outline-secondary btn-sm mt-2">
          Rollback Game
        </button>
      </div>
    </Card>
  );
}
