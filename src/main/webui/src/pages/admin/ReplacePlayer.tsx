import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { api } from '../../api/client';
import { runRequest } from '../../api/mutate';
import { AdminSelect, toOptions, useAdminGames } from './adminControls';

export function ReplacePlayer() {
  const { gameOptions, gameId, setGameId } = useAdminGames();
  const { data: substitutes = [] } = useQuery({
    queryKey: ['admin-page', 'substitutes'],
    queryFn: () => api.get<string[]>('/admin-page/substitutes'),
  });
  const [players, setPlayers] = useState<string[]>([]);
  const [existingPlayer, setExistingPlayer] = useState('');
  const [pickedSubstitute, setPickedSubstitute] = useState('');
  const newPlayer = pickedSubstitute || substitutes[0] || '';

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
    runRequest(
      api.put(`/admin-page/games/${encodeURIComponent(gameId)}/replace-player`, { existingPlayer, newPlayer }),
      'Failed to replace player',
    );
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Replace Player</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-2 items-start">
        <AdminSelect id="adminGameList" label="Games" value={gameId} onChange={setGameId} options={gameOptions} />
        <AdminSelect
          id="adminReplacePlayerList"
          label="Player to replace:"
          value={existingPlayer}
          onChange={setExistingPlayer}
          options={toOptions(players)}
        />
        <AdminSelect
          id="adminReplacementList"
          label="Substitute"
          value={newPlayer}
          onChange={setPickedSubstitute}
          options={toOptions(substitutes)}
        />
        <Button variant="secondary" size="sm" onClick={submit}>
          Replace player
        </Button>
      </CardBody>
    </Card>
  );
}
