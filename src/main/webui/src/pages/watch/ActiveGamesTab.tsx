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

const TH = 'jt:sticky jt:top-0 jt:bg-panel jt:text-left jt:font-semibold jt:text-ink-muted jt:px-3 jt:py-1.5 jt:border-b jt:border-line';
const TD = 'jt:px-3 jt:py-1 jt:border-b jt:border-line/50 jt:text-ink';

export function ActiveGamesTab() {
  const { data: games = [] } = useQuery({
    queryKey: ['watch', 'active'],
    queryFn: () => api.get<GameSummary[]>('/watch/active'),
    refetchInterval: ACTIVE_GAMES_POLL_MS,
  });

  return (
    <Panel title="Active Games">
      <div className="jt:flex-1 jt:min-h-0 jt:overflow-auto">
        <table className="jt:w-full jt:text-sm">
          <thead>
            <tr>
              <th className={TH}>Game</th>
              <th className={TH}>Current Turn</th>
              <th className={TH}>Updated</th>
            </tr>
          </thead>
          <tbody>
            {games.map((g) => (
              <tr key={g.gameId} className="jt:hover:bg-hover">
                <td className={TD}>
                  <Link to={pathForGame(g.gameId)} className="jt:text-accent jt:underline">
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
