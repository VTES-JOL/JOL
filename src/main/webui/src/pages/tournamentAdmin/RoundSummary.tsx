import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../../api/client';
import type { PlayerRoundSummary } from '../../api/types';
import { alertDialog, confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { RecreateTableModal } from './RecreateTableModal';

type Summary = Record<number, Record<number, PlayerRoundSummary[]>>;

// Pool/VP/GW change as each table's games progress, but there's no WS signal
// for in-game action (this page doesn't join any game's room) — poll so
// results stay current while a round is in progress.
const ROUND_SUMMARY_POLL_MS = 20_000;

const TH = 'text-left font-semibold border-b border-line px-2 py-1';
const TD = 'border-b border-line/50 px-2 py-1';

export function RoundSummary({ tournamentName }: { tournamentName: string }) {
  const queryClient = useQueryClient();
  const [recreateTarget, setRecreateTarget] = useState<{ round: number; table: number } | null>(null);

  const queryKey = ['tournament', tournamentName, 'round-summary'];
  const { data: summary = {} } = useQuery<Summary>({
    queryKey,
    queryFn: () => api.get<Summary>(`/tournament/${encodeURIComponent(tournamentName)}/round-summary`),
    refetchInterval: ROUND_SUMMARY_POLL_MS,
  });

  const refresh = () => queryClient.invalidateQueries({ queryKey });

  const closeTable = async (round: number, table: number) => {
    if (!(await confirmDialog('Close table and record VP/GW results?'))) return;
    runRequest(
      api.post<boolean>(`/tournament/${encodeURIComponent(tournamentName)}/round/${round}/table/${table}/close`),
      'Failed to close table',
      (ok) => {
        if (ok) refresh();
        else alertDialog('Could not close table — game may already be closed.');
      },
    );
  };

  return (
    <div className="flex-1 min-h-0 overflow-auto mt-2">
      {Object.entries(summary).map(([round, tables]) => (
        <div key={round} className="mb-3">
          <span className="text-lg font-semibold">Round {round}</span>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-2 mt-1">
            {Object.entries(tables).map(([table, players]) => {
              const allDone = players.every((p) => p.pool <= 0);
              return (
                <div key={table} className="rounded border border-line-accent bg-surface/85 p-2">
                  <div className="font-semibold mb-1">Table {table}</div>
                  <table className="w-full text-sm">
                    <thead>
                      <tr>
                        <th className={TH}>Player</th>
                        <th className={TH}>Pool</th>
                        <th className={TH}>VP</th>
                        <th className={TH}>GW</th>
                      </tr>
                    </thead>
                    <tbody>
                      {players.map((p) => (
                        <tr key={p.name}>
                          <td className={TD}>{p.name}</td>
                          <td className={TD}>{p.pool}</td>
                          <td className={TD}>{p.vp}</td>
                          <td className={TD}>{p.gw && <Badge variant="online">GW</Badge>}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {allDone && (
                    <Button variant="danger" size="sm" className="mt-2 w-full" onClick={() => closeTable(Number(round), Number(table))}>
                      Close Table
                    </Button>
                  )}
                  <Button
                    variant="danger"
                    size="sm"
                    className="mt-2 w-full"
                    onClick={() => setRecreateTarget({ round: Number(round), table: Number(table) })}
                  >
                    Recreate Table
                  </Button>
                </div>
              );
            })}
          </div>
        </div>
      ))}
      {recreateTarget && (
        <RecreateTableModal
          tournamentName={tournamentName}
          round={recreateTarget.round}
          table={recreateTarget.table}
          onClose={() => setRecreateTarget(null)}
          onRecreated={() => {
            setRecreateTarget(null);
            refresh();
          }}
        />
      )}
    </div>
  );
}
