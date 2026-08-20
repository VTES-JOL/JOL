import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';

export function ReplacePlayer({
  games,
  substitutes,
  onSaved,
}: {
  games: Record<string, string>;
  substitutes: string[];
  onSaved: () => void;
}) {
  const gameIds = Object.keys(games);
  const [gameId, setGameId] = useState(gameIds[0] ?? '');
  const [players, setPlayers] = useState<string[]>([]);
  const [existingPlayer, setExistingPlayer] = useState('');
  const [newPlayer, setNewPlayer] = useState('');

  useEffect(() => {
    if (!newPlayer && substitutes.length > 0) setNewPlayer(substitutes[0]);
  }, [substitutes, newPlayer]);

  useEffect(() => {
    if (!gameId) {
      setPlayers([]);
      return;
    }
    api
      .get<string[]>(`/game/${encodeURIComponent(gameId)}/players`)
      .then((p) => {
        setPlayers(p);
        setExistingPlayer(p[0] ?? '');
      })
      .catch((err) => console.error('Failed to load game players', err));
  }, [gameId]);

  const submit = () => {
    if (!gameId || !existingPlayer || !newPlayer) return;
    api
      .put(`/admin-page/games/${encodeURIComponent(gameId)}/replace-player`, { existingPlayer, newPlayer })
      .then(onSaved)
      .catch((err) => console.error('Failed to replace player', err));
  };

  return (
    <Card className="mt-2">
      <CardHeader>
        <CardTitle>Replace Player</CardTitle>
      </CardHeader>
      <div className="card-body">
        <label htmlFor="adminGameList" className="form-label">
          Games
        </label>
        <select id="adminGameList" className="form-select" value={gameId} onChange={(e) => setGameId(e.target.value)}>
          {gameIds.map((id) => (
            <option key={id} value={id}>
              {games[id]}
            </option>
          ))}
        </select>
        <label htmlFor="adminReplacePlayerList" className="form-label">
          Player to replace:
        </label>
        <select
          id="adminReplacePlayerList"
          className="form-select"
          value={existingPlayer}
          onChange={(e) => setExistingPlayer(e.target.value)}
        >
          {players.map((p) => (
            <option key={p} value={p}>
              {p}
            </option>
          ))}
        </select>
        <label htmlFor="adminReplacementList" className="form-label">
          Substitute
        </label>
        <select
          id="adminReplacementList"
          className="form-select"
          value={newPlayer}
          onChange={(e) => setNewPlayer(e.target.value)}
        >
          {substitutes.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
        <button onClick={submit} className="btn btn-outline-secondary btn-sm mt-2">
          Replace player
        </button>
      </div>
    </Card>
  );
}
