import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import { confirmDialog } from '../../components/dialog';
import { showError } from '../../components/toast';

export function RollbackGame({ games, onSaved }: { games: Record<string, string>; onSaved: () => void }) {
  const gameIds = Object.keys(games);
  const [gameId, setGameId] = useState(gameIds[0] ?? '');
  const [turns, setTurns] = useState<string[]>([]);
  const [turn, setTurn] = useState('');

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
    api
      .post(`/admin-page/games/${encodeURIComponent(gameId)}/rollback`, { turn })
      .then(onSaved)
      .catch((err) => {
        console.error('Failed to rollback game', err);
        showError('Failed to rollback game.');
      });
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
