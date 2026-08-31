import { useQuery } from '@tanstack/react-query';
import { Download } from 'lucide-react';
import { api } from '../../api/client';
import type { GameHistory } from '../../api/types';
import { runRequest } from '../../api/mutate';
import { Panel } from '../../components/ui/Panel';
import { Button } from '../../components/ui/Button';

const DATE_FORMAT = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'UTC',
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

function downloadCsv(data: string, filename: string) {
  const blob = new Blob([data], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

const TH = 'sticky top-0 bg-panel text-left font-semibold text-ink-muted px-3 py-1.5 border-b border-line';
const TD = 'px-3 py-1 text-ink align-top';

export function PastGamesTab() {
  const { data: history = [] } = useQuery({
    queryKey: ['watch', 'history'],
    queryFn: () => api.get<GameHistory[]>('/watch/history'),
  });

  const exportCsv = () => {
    runRequest(api.getText('/admin/export/games.csv'), 'Failed to export past games', (data) => downloadCsv(data, 'past-games.csv'));
  };

  return (
    <Panel
      title="Past Games"
      right={
        <Button variant="secondary" size="sm" icon={<Download size={14} />} onClick={exportCsv}>
          Export CSV
        </Button>
      }
    >
      <div className="flex-1 min-h-0 overflow-auto">
        <table className="w-full text-sm">
          <thead>
            <tr>
              <th className={TH}>Game</th>
              <th className={TH}>Started</th>
              <th className={TH}>Ended</th>
              <th className={TH} colSpan={3}>
                Results
              </th>
            </tr>
          </thead>
          <tbody>
            {history.flatMap((g) =>
              g.results.map((r, i) => (
                <tr
                  key={`${g.name}-${r.playerName}`}
                  className={i === 0 ? 'border-t-2 border-line' : 'border-t border-line/40'}
                >
                  {i === 0 && (
                    <>
                      <td className={TD} rowSpan={g.results.length}>
                        {g.name}
                      </td>
                      <td className={TD} rowSpan={g.results.length}>
                        {DATE_FORMAT.format(new Date(g.started))} UTC
                      </td>
                      <td className={TD} rowSpan={g.results.length}>
                        {DATE_FORMAT.format(new Date(g.ended))} UTC
                      </td>
                    </>
                  )}
                  <td className={`${TD} py-1`}>{r.playerName}</td>
                  <td className={`${TD} py-1`}>
                    {r.deckName.length > 50 ? `${r.deckName.slice(0, 50)}...` : r.deckName}
                  </td>
                  <td className={`${TD} py-1`}>
                    {r.victoryPoints} VP
                    {r.gameWin ? ', 1 GW' : ''}
                  </td>
                </tr>
              )),
            )}
          </tbody>
        </table>
      </div>
    </Panel>
  );
}
