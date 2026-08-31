import type { ReactNode } from 'react';
import { SortIcon, useTableSort } from './statsUtils';

type SortMode = 'default' | 'percent' | 'duration' | 'boolean';

export interface StatsColumn<Row> {
  /** Sort key + React key. Use a stable synthetic string for computed columns. */
  key: string;
  header: ReactNode;
  /** Omit to make the column non-sortable (renders no sort control). */
  sortMode?: SortMode;
  /** Cell content. Defaults to `String(row[key])`. */
  render?: (row: Row) => ReactNode;
  /** Adds a text filter input to the header; rows match case-insensitively. */
  filter?: {
    value: string;
    onChange: (value: string) => void;
    /** What to match against. Defaults to `String(row[key])`. */
    accessor?: (row: Row) => string;
    placeholder?: string;
  };
  thClassName?: string;
  tdClassName?: string;
}

interface Props<Row extends Record<string, unknown>> {
  rows: readonly Row[];
  columns: ReadonlyArray<StatsColumn<Row>>;
  /** Stable key per row. */
  rowKey: (row: Row, index: number) => string | number;
  /** Scroll container max height. Default `78vh`. */
  maxHeight?: string;
}

/**
 * The sortable, per-column-filterable stats grid every watch/stats/ tab was
 * hand-rolling: a scroll container + sticky `<thead>` + `<SortIcon>` per
 * column + a `<tbody>` map, plus the `.filter(row => …includes…)` chain for
 * columns with a text box in their header. Sorting is `useTableSort` (per
 * ds.js's numeric-aware compare); this only owns the surrounding markup and
 * the filter wiring. `StatsDtoTable` is the players/decks/nations preset
 * built on top of it.
 */
export function SortableStatsTable<Row extends Record<string, unknown>>({
  rows,
  columns,
  rowKey,
  maxHeight = '78vh',
}: Props<Row>) {
  // useTableSort copies before sorting, so passing the array through is safe.
  const { sorted, toggle } = useTableSort<Row>(rows as Row[]);

  const filtered = sorted.filter((row) =>
    columns.every((col) => {
      if (!col.filter) return true;
      const hay = (col.filter.accessor ? col.filter.accessor(row) : String(row[col.key] ?? '')).toLowerCase();
      return hay.includes(col.filter.value.toLowerCase());
    }),
  );

  return (
    <div className="overflow-auto pb-3" style={{ maxHeight }}>
      <table className="table table-bordered table-sm mb-0">
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.key} className={`sticky-top bg-body${col.thClassName ? ` ${col.thClassName}` : ''}`}>
                {col.header}
                {col.filter && (
                  <input
                    type="text"
                    className="form-control form-control-sm d-inline-block w-auto ms-1"
                    placeholder={col.filter.placeholder}
                    value={col.filter.value}
                    onChange={(e) => col.filter!.onChange(e.target.value)}
                  />
                )}
                {col.sortMode && (
                  <SortIcon
                    column={col.key as keyof Row}
                    onSort={toggle}
                    mode={col.sortMode === 'default' ? undefined : col.sortMode}
                  />
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {filtered.map((row, i) => (
            <tr key={rowKey(row, i)} className="border-top">
              {columns.map((col) => (
                <td key={col.key} className={col.tdClassName}>
                  {col.render ? col.render(row) : String(row[col.key] ?? '')}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
