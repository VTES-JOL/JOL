import type { ReactNode } from 'react';
import type { StatsDto } from '../../../api/types';
import { SortIcon, useTableSort } from '../statsUtils';

interface Row extends Record<string, unknown> {
  key: string;
  allGames: string;
  gwCount: string;
  vpCount: string;
  winRate: string;
  avgVp: string;
  highestVp: string;
  uniqueOpponents: string;
  mostPlayedOpponent: string;
  winStreak: string;
}

// Shared row shape/sort logic behind /stats/players, /stats/decks, /stats/nations
// (ds.js's createStats()) — the only real differences between those three tabs
// are the name column's header/rendering, the threshold input, and whether the
// two "opponent" columns are shown (players only).
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
  const { sorted, toggle } = useTableSort(rows);
  const filtered = sorted.filter((r) => filterValue(r.key).toLowerCase().includes(nameFilter.toLowerCase()));

  return (
    <div className="overflow-auto pb-3" style={{ maxHeight: '78vh' }}>
      <table className="table table-bordered table-sm mb-0">
        <thead>
          <tr>
            <th className="sticky-top bg-body">
              {nameHeader}
              <input
                type="text"
                className="form-control form-control-sm d-inline-block w-auto ms-1"
                value={nameFilter}
                onChange={(e) => onNameFilterChange(e.target.value)}
              />
              <SortIcon column="key" onSort={toggle} />
            </th>
            <th className="sticky-top bg-body">
              Number of Games
              <input
                type="number"
                min={0}
                className="form-control form-control-sm d-inline-block ms-1"
                value={threshold}
                onChange={(e) => onThresholdChange(e.target.value)}
                style={{ width: 60 }}
              />
              <SortIcon column="allGames" onSort={toggle} />
            </th>
            <th className="sticky-top bg-body">
              GW Total <SortIcon column="gwCount" onSort={toggle} />
            </th>
            <th className="sticky-top bg-body">
              VP Total <SortIcon column="vpCount" onSort={toggle} />
            </th>
            <th className="sticky-top bg-body">
              % Win Rate <SortIcon column="winRate" onSort={toggle} mode="percent" />
            </th>
            <th className="sticky-top bg-body">
              Average VP <SortIcon column="avgVp" onSort={toggle} />
            </th>
            <th className="sticky-top bg-body">
              Highest VP <SortIcon column="highestVp" onSort={toggle} />
            </th>
            {extended && (
              <th className="sticky-top bg-body">
                Unique Opponents <SortIcon column="uniqueOpponents" onSort={toggle} />
              </th>
            )}
            {extended && (
              <th className="sticky-top bg-body">
                Most played Opponent <SortIcon column="mostPlayedOpponent" onSort={toggle} />
              </th>
            )}
            <th className="sticky-top bg-body">
              Highest Win Streak <SortIcon column="winStreak" onSort={toggle} />
            </th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((r) => (
            <tr key={r.key} className="border-top">
              <td>{renderName(r.key)}</td>
              <td>{r.allGames}</td>
              <td>{r.gwCount}</td>
              <td>{r.vpCount}</td>
              <td>{r.winRate}</td>
              <td>{r.avgVp}</td>
              <td>{r.highestVp}</td>
              {extended && <td>{r.uniqueOpponents}</td>}
              {extended && <td>{r.mostPlayedOpponent}</td>}
              <td>{r.winStreak}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
