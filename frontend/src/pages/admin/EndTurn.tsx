import { useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import { confirmDialog } from '../../components/dialog';
import { showError } from '../../components/toast';

export function EndTurn({ games, onSaved }: { games: Record<string, string>; onSaved: () => void }) {
  const gameIds = Object.keys(games);
  const [gameId, setGameId] = useState(gameIds[0] ?? '');

  const submit = async () => {
    if (!gameId) return;
    if (!(await confirmDialog(`Are you sure you want to end turn for ${games[gameId]}`))) return;
    api
      .post(`/admin-page/games/${encodeURIComponent(gameId)}/end-turn`)
      .then(onSaved)
      .catch((err) => {
        console.error('Failed to end turn', err);
        showError('Failed to end turn.');
      });
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
