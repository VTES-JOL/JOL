import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { TournamentPlayer, TournamentRegistration } from '../../api/types';
import { RoundColumn } from './RoundColumn';
import type { DragPayload } from './dragDrop';
import { ImportTablesModal } from './ImportTablesModal';

interface RoundsState {
  pools: Record<number, string[]>;
  tables: Record<number, string[][]>;
}

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

export function DraftTableManager({
  tournamentName,
  roundsConfig,
  onCreatedTables,
}: {
  tournamentName: string;
  roundsConfig: boolean;
  onCreatedTables: () => void;
}) {
  const [roundNumbers, setRoundNumbers] = useState<number[]>([]);
  const [playerVekn, setPlayerVekn] = useState<Record<string, string>>({});
  const [state, setState] = useState<RoundsState>({ pools: {}, tables: {} });
  const [showImport, setShowImport] = useState(false);
  const [createError, setCreateError] = useState('');

  const load = () => {
    Promise.all([
      api.get<number[]>(`/tournament/${encodeURIComponent(tournamentName)}/rounds-count`),
      api.get<TournamentRegistration[]>(`/tournament/${encodeURIComponent(tournamentName)}/players`),
      roundsConfig
        ? api.get<Record<number, Record<number, TournamentPlayer[]>>>(
            `/tournament/${encodeURIComponent(tournamentName)}/rounds`,
          )
        : Promise.resolve(null),
    ])
      .then(([rounds, registrations, existingRounds]) => {
        setRoundNumbers(rounds);
        const vekn: Record<string, string> = {};
        registrations.forEach((r) => (vekn[r.player] = r.vekn ?? ''));
        setPlayerVekn(vekn);

        const fullRoster = registrations.map((r) => r.player);
        const pools: Record<number, string[]> = {};
        const tables: Record<number, string[][]> = {};
        rounds.forEach((round) => {
          const existingTables = existingRounds?.[round];
          const tableList = existingTables
            ? Object.keys(existingTables)
                .sort((a, b) => Number(a) - Number(b))
                .map((t) => existingTables[Number(t)].map((p) => p.name))
            : [];
          tables[round] = tableList;
          const seated = new Set(tableList.flat());
          pools[round] = fullRoster.filter((p) => !seated.has(p));
        });
        setState({ pools, tables });
      })
      .catch((err) => console.error('Failed to load tournament tables', err));
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(load, [tournamentName]);

  const move = (round: number, payload: DragPayload, to: 'pool' | number) => {
    if (payload.from === to) return;
    setState((prev) => {
      const pool = prev.pools[round] ?? [];
      const tables = prev.tables[round] ?? [];
      const poolAfterRemove = payload.from === 'pool' ? pool.filter((p) => p !== payload.player) : pool;
      const tablesAfterRemove =
        typeof payload.from === 'number'
          ? tables.map((t, i) => (i === payload.from ? t.filter((p) => p !== payload.player) : t))
          : tables;
      const poolAfterAdd = to === 'pool' ? [...poolAfterRemove, payload.player] : poolAfterRemove;
      const tablesAfterAdd =
        typeof to === 'number' ? tablesAfterRemove.map((t, i) => (i === to ? [...t, payload.player] : t)) : tablesAfterRemove;
      return {
        pools: { ...prev.pools, [round]: poolAfterAdd },
        tables: { ...prev.tables, [round]: tablesAfterAdd },
      };
    });
  };

  const createTable = (round: number) => {
    setState((prev) => ({ ...prev, tables: { ...prev.tables, [round]: [...(prev.tables[round] ?? []), []] } }));
  };

  const removeTable = (round: number, tableIndex: number) => {
    setState((prev) => {
      const tables = prev.tables[round] ?? [];
      const removed = tables[tableIndex] ?? [];
      return {
        pools: { ...prev.pools, [round]: [...(prev.pools[round] ?? []), ...removed] },
        tables: { ...prev.tables, [round]: tables.filter((_, i) => i !== tableIndex) },
      };
    });
  };

  const sortPoolByVekn = (round: number) => {
    setState((prev) => ({
      ...prev,
      pools: {
        ...prev.pools,
        [round]: [...(prev.pools[round] ?? [])].sort(
          (a, b) => Number(playerVekn[a] ?? 0) - Number(playerVekn[b] ?? 0),
        ),
      },
    }));
  };

  const sortPoolByName = (round: number) => {
    setState((prev) => ({
      ...prev,
      pools: { ...prev.pools, [round]: [...(prev.pools[round] ?? [])].sort((a, b) => a.localeCompare(b)) },
    }));
  };

  const saveTables = () => {
    if (!confirm('Are you sure you want to SAVE the Tournament Tables?')) return;
    const body: Record<number, Record<number, string[]>> = {};
    roundNumbers.forEach((round) => {
      const tables = state.tables[round] ?? [];
      const tableMap: Record<number, string[]> = {};
      tables.forEach((players, i) => {
        tableMap[i + 1] = players;
      });
      body[round] = tableMap;
    });
    api
      .put(`/tournament/${encodeURIComponent(tournamentName)}/rounds`, body)
      .then(load)
      .catch((err) => console.error('Failed to save tournament tables', err));
  };

  const downloadTables = () => {
    api
      .getText(`/tournament/${encodeURIComponent(tournamentName)}/rounds/csv`)
      .then((data) => downloadCsv(data, 'rounds.csv'))
      .catch((err) => console.error('Failed to export tournament tables', err));
  };

  const createTables = () => {
    if (!confirm('Are you sure you want to create the Tournament Tables?')) return;
    setCreateError('');
    api
      .post(`/tournament/${encodeURIComponent(tournamentName)}/tables`)
      .then(onCreatedTables)
      .catch((err) => setCreateError(`Failed to create tables: ${err.message}`));
  };

  return (
    <>
      <div className="d-flex gap-1 flex-wrap">
        <button onClick={saveTables} className="btn btn-outline-secondary btn-sm">
          Save Tables
        </button>
        <button onClick={downloadTables} className="btn btn-outline-secondary btn-sm">
          Download
        </button>
        <button onClick={load} className="btn btn-outline-secondary btn-sm">
          Show Tables
        </button>
        <button onClick={() => setShowImport(true)} className="btn btn-outline-primary btn-sm">
          Import
        </button>
        <button onClick={createTables} className="btn btn-outline-success btn-sm">
          Create Rounds
        </button>
      </div>
      {createError && <div className="alert alert-danger mt-2">{createError}</div>}
      <div className="flex-fill overflow-auto mt-2 min-h-0">
        {roundNumbers.map((round) => (
          <RoundColumn
            key={round}
            round={round}
            pool={state.pools[round] ?? []}
            tables={state.tables[round] ?? []}
            playerVekn={playerVekn}
            onMove={move}
            onCreateTable={createTable}
            onRemoveTable={removeTable}
            onSortPoolByVekn={sortPoolByVekn}
            onSortPoolByName={sortPoolByName}
          />
        ))}
      </div>
      {showImport && (
        <ImportTablesModal
          tournamentName={tournamentName}
          onClose={() => setShowImport(false)}
          onImported={() => {
            setShowImport(false);
            load();
          }}
        />
      )}
    </>
  );
}
