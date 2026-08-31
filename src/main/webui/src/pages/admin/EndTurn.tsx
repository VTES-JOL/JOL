import { Card, CardHeader, CardTitle } from '../../components/Card';
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
    <Card className="mt-2">
      <CardHeader>
        <CardTitle>End Turn</CardTitle>
      </CardHeader>
      <div className="card-body">
        <AdminSelect id="endTurnList" label="Games" value={gameId} onChange={setGameId} options={gameOptions} />
        <button onClick={submit} className="btn btn-outline-secondary btn-sm mt-2">
          End Turn
        </button>
      </div>
    </Card>
  );
}
