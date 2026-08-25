import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../../api/client';
import type { PlayerRoundSummary } from '../../api/types';
import { alertDialog, confirmDialog } from '../../components/dialog';
import { showError } from '../../components/toast';
import { RecreateTableModal } from './RecreateTableModal';

type Summary = Record<number, Record<number, PlayerRoundSummary[]>>;

// Pool/VP/GW change as each table's games progress, but there's no WS signal
// for in-game action (this page doesn't join any game's room) — poll so
// results stay current while a round is in progress.
const ROUND_SUMMARY_POLL_MS = 20_000;

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
    api
      .post<boolean>(`/tournament/${encodeURIComponent(tournamentName)}/round/${round}/table/${table}/close`)
      .then((ok) => {
        if (ok) refresh();
        else alertDialog('Could not close table — game may already be closed.');
      })
      .catch((err) => {
        console.error('Failed to close table', err);
        showError('Failed to close table.');
      });
  };

  return (
    <div className="flex-fill overflow-auto mt-2 min-h-0">
      {Object.entries(summary).map(([round, tables]) => (
        <div key={round} className="mb-3">
          <span className="h4">Round {round}</span>
          <div className="row g-2 mt-1">
            {Object.entries(tables).map(([table, players]) => {
              const allDone = players.every((p) => p.pool <= 0);
              return (
                <div key={table} className="col-lg-3 col-md-4 col-6">
                  <div className="card p-2">
                    <div className="fw-semibold mb-1">Table {table}</div>
                    <table className="table table-sm table-bordered mb-0">
                      <thead>
                        <tr>
                          <th>Player</th>
                          <th>Pool</th>
                          <th>VP</th>
                          <th>GW</th>
                        </tr>
                      </thead>
                      <tbody>
                        {players.map((p) => (
                          <tr key={p.name}>
                            <td>{p.name}</td>
                            <td>{p.pool}</td>
                            <td>{p.vp}</td>
                            <td>{p.gw && <span className="badge text-bg-success">GW</span>}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {allDone && (
                      <button
                        className="btn btn-sm btn-outline-danger mt-2 w-100"
                        onClick={() => closeTable(Number(round), Number(table))}
                      >
                        Close Table
                      </button>
                    )}
                    <button
                      className="btn btn-sm btn-outline-danger mt-2 w-100"
                      onClick={() => setRecreateTarget({ round: Number(round), table: Number(table) })}
                    >
                      Recreate Table
                    </button>
                  </div>
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
