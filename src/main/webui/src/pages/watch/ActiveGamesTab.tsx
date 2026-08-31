import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../api/client';
import type { GameSummary } from '../../api/types';
import { pathForGame } from '../../routes';
import { Panel } from '../../components/ui/Panel';

const TIMESTAMP_FORMAT = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'UTC',
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
});

// Active games' turn/timestamp fields update on every in-game action, but
// this page never joins any individual game's WS room (it's watching many
// games at once, not one) — so those per-game pushes never reach it. The
// ['watch','active'] invalidate (JolAdmin.createGame/startGame/endGame) only
// covers games appearing/closing. Poll to pick up in-game turn progress for
// games already in the list.
const ACTIVE_GAMES_POLL_MS = 20_000;

const TH = 'sticky top-0 bg-panel text-left font-semibold text-ink-muted px-3 py-1.5 border-b border-line';
const TD = 'px-3 py-1 border-b border-line/50 text-ink';

export function ActiveGamesTab() {
  const { data: games = [] } = useQuery({
    queryKey: ['watch', 'active'],
    queryFn: () => api.get<GameSummary[]>('/watch/active'),
    refetchInterval: ACTIVE_GAMES_POLL_MS,
  });

  return (
    <Panel title="Active Games">
      <div className="flex-1 min-h-0 overflow-auto">
        <table className="w-full text-sm">
          <thead>
            <tr>
              <th className={TH}>Game</th>
              <th className={TH}>Current Turn</th>
              <th className={TH}>Updated</th>
            </tr>
          </thead>
          <tbody>
            {games.map((g) => (
              <tr key={g.gameId} className="hover:bg-hover">
                <td className={TD}>
                  <Link to={pathForGame(g.gameId)} className="text-accent underline">
                    {g.gameName}
                  </Link>
                </td>
                <td className={TD}>{g.turn}</td>
                <td className={TD}>{TIMESTAMP_FORMAT.format(new Date(g.timestamp))} UTC</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Panel>
  );
}
