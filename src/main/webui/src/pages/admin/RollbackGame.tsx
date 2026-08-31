import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { api } from '../../api/client';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { AdminSelect, toOptions, useAdminGames } from './adminControls';

export function RollbackGame() {
  const { games, gameOptions, gameId, setGameId } = useAdminGames();
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
    runRequest(api.post(`/admin-page/games/${encodeURIComponent(gameId)}/rollback`, { turn }), 'Failed to rollback game');
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Rollback Game</CardTitle>
      </CardHeader>
      <CardBody className="jt:flex jt:flex-col jt:gap-2 jt:items-start">
        <AdminSelect id="rollbackGamesList" label="Games" value={gameId} onChange={setGameId} options={gameOptions} />
        <AdminSelect
          id="rollbackTurnsList"
          label="Turns"
          value={turn}
          onChange={setTurn}
          options={toOptions(turns)}
        />
        <Button variant="secondary" size="sm" onClick={submit}>
          Rollback Game
        </Button>
      </CardBody>
    </Card>
  );
}
