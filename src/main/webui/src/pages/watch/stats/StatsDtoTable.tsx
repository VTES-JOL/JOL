import type { ReactNode } from 'react';
import type { StatsDto } from '../../../api/types';
import { SortableStatsTable, type StatsColumn } from './SortableStatsTable';

interface Row extends StatsDto, Record<string, unknown> {
  key: string;
}

// Shared row shape/sort logic behind /stats/players, /stats/decks, /stats/nations
// (ds.js's createStats()) — the only real differences between those three tabs
// are the name column's header/rendering, the threshold input, and whether the
// two "opponent" columns are shown (players only). Thin preset over
// SortableStatsTable.
export function StatsDtoTable({
  data,
  extended,
  nameHeader,
  renderName,
  threshold,
  onThresholdChange,
  nameFilter,
  onNameFilterChange,
  filterValue = (key) => key,
}: {
  data: Record<string, StatsDto>;
  extended: boolean;
  nameHeader: string;
  renderName: (key: string) => ReactNode;
  threshold: string;
  onThresholdChange: (v: string) => void;
  nameFilter: string;
  onNameFilterChange: (v: string) => void;
  /** What the name filter matches against — defaults to the raw key, but e.g.
   * nation stats filter against the displayed country name, not its code. */
  filterValue?: (key: string) => string;
}) {
  const rows: Row[] = Object.entries(data).map(([key, dto]) => ({ key, ...dto }));

  const columns: StatsColumn<Row>[] = [
    {
      key: 'key',
      header: nameHeader,
      sortMode: 'default',
      render: (r) => renderName(r.key),
      filter: { value: nameFilter, onChange: onNameFilterChange, accessor: (r) => filterValue(r.key) },
    },
    {
      key: 'allGames',
      sortMode: 'default',
      header: (
        <>
          Number of Games
          <input
            type="number"
            min={0}
            className="form-control form-control-sm d-inline-block ms-1"
            value={threshold}
            onChange={(e) => onThresholdChange(e.target.value)}
            style={{ width: 60 }}
          />
        </>
      ),
    },
    { key: 'gwCount', header: 'GW Total ', sortMode: 'default' },
    { key: 'vpCount', header: 'VP Total ', sortMode: 'default' },
    { key: 'winRate', header: '% Win Rate ', sortMode: 'percent' },
    { key: 'avgVp', header: 'Average VP ', sortMode: 'default' },
    { key: 'highestVp', header: 'Highest VP ', sortMode: 'default' },
    ...(extended
      ? ([
          { key: 'uniqueOpponents', header: 'Unique Opponents ', sortMode: 'default' },
          { key: 'mostPlayedOpponent', header: 'Most played Opponent ', sortMode: 'default' },
        ] as StatsColumn<Row>[])
      : []),
    { key: 'winStreak', header: 'Highest Win Streak ', sortMode: 'default' },
  ];

  return <SortableStatsTable<Row> rows={rows} columns={columns} rowKey={(r) => r.key} />;
}
