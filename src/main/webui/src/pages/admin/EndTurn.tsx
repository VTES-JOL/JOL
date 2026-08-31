import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { api } from '../../api/client';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { AdminSelect, useAdminGames } from './adminControls';

export function EndTurn() {
  const { games, gameOptions, gameId, setGameId } = useAdminGames();

  const submit = async () => {
    if (!gameId) return;
    if (!(await confirmDialog(`Are you sure you want to end turn for ${games[gameId]}`))) return;
    runRequest(api.post(`/admin-page/games/${encodeURIComponent(gameId)}/end-turn`), 'Failed to end turn');
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>End Turn</CardTitle>
      </CardHeader>
      <CardBody className="jt:flex jt:flex-col jt:gap-2 jt:items-start">
        <AdminSelect id="endTurnList" label="Games" value={gameId} onChange={setGameId} options={gameOptions} />
        <Button variant="secondary" size="sm" onClick={submit}>
          End Turn
        </Button>
      </CardBody>
    </Card>
  );
}
