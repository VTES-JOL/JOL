import type { ReactNode } from 'react';
import { Spinner } from '../../../components/ui/Spinner';
import { SortIndicator, useTableSort, type SortMode, type SortSpec } from './statsUtils';

export interface StatsColumn<Row> {
  /** Sort key + React key. Use a stable synthetic string for computed columns. */
  key: string;
  header: ReactNode;
  /** Omit to make the column non-sortable (renders no sort control). */
  sortMode?: SortMode;
  /** Extra control rendered on its own line under the header label (e.g. a threshold input). */
  headerAux?: ReactNode;
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
  /** Column the table sorts by on first render. */
  defaultSort?: SortSpec<Row>;
  /** Show a spinner instead of the (possibly stale/empty) table body. */
  loading?: boolean;
  /** Scroll container max height. Default `72vh`. */
  maxHeight?: string;
  /**
   * Freeze the first column while the grid scrolls sideways, so a row keeps its
   * label anchor. On by default — the first column is always the row identity
   * (Month / Player / Deck / Nation / Game). The wide grids (JolStats' 12
   * columns especially) are unusable on a phone without it.
   */
  stickyFirstColumn?: boolean;
}

const FILTER_INPUT =
  'mt-1 block w-full max-w-[10rem] rounded border border-line/60 bg-surface/70 px-1.5 py-0.5 text-xs font-normal text-ink outline-none focus:border-accent/60';

/**
 * The sortable, per-column-filterable stats grid every watch/stats/ tab was
 * hand-rolling. Sorting is `useTableSort` (per ds.js's numeric-aware compare);
 * this owns the surrounding markup, the filter wiring, the active-column sort
 * indicator, the loading state and the row count. `StatsDtoTable` is the
 * players/decks/nations preset built on top of it.
 */
export function SortableStatsTable<Row extends Record<string, unknown>>({
  rows,
  columns,
  rowKey,
  defaultSort,
  loading,
  maxHeight = '72vh',
  stickyFirstColumn = true,
}: Props<Row>) {
  // useTableSort copies before sorting, so passing the array through is safe.
  const { sorted, toggle, activeKey, direction } = useTableSort<Row>(rows as Row[], defaultSort);

  const filtered = sorted.filter((row) =>
    columns.every((col) => {
      if (!col.filter) return true;
      const hay = (col.filter.accessor ? col.filter.accessor(row) : String(row[col.key] ?? '')).toLowerCase();
      return hay.includes(col.filter.value.toLowerCase());
    }),
  );

  const anyFilterActive = columns.some((c) => c.filter && c.filter.value.trim() !== '');
  const countLabel = anyFilterActive
    ? `${filtered.length} of ${rows.length}`
    : `${rows.length} ${rows.length === 1 ? 'row' : 'rows'}`;

  return (
    <div className="flex flex-col min-h-0">
      <div className="overflow-auto pb-1" style={{ maxHeight }}>
        <table className="w-full text-sm border-separate border-spacing-0">
          <thead>
            <tr>
              {columns.map((col, ci) => {
                const sortState =
                  col.sortMode && activeKey === (col.key as keyof Row)
                    ? (direction ?? 'asc')
                    : 'none';
                const frozen = stickyFirstColumn && ci === 0;
                return (
                  <th
                    key={col.key}
                    className={`sticky top-0 z-10 bg-panel text-left font-semibold text-ink-muted px-2 py-1.5 border-b border-line align-bottom whitespace-nowrap${
                      frozen ? ' left-0 z-20 border-r border-line' : ''
                    }${col.thClassName ? ` ${col.thClassName}` : ''}`}
                  >
                    {col.sortMode ? (
                      <button
                        type="button"
                        className="group inline-flex items-center font-semibold text-ink-muted hover:text-ink"
                        onClick={() => toggle(col.key as keyof Row, col.sortMode === 'default' ? undefined : col.sortMode)}
                      >
                        {col.header}
                        <SortIndicator state={sortState} />
                      </button>
                    ) : (
                      col.header
                    )}
                    {col.filter && (
                      <input
                        type="text"
                        className={FILTER_INPUT}
                        placeholder={col.filter.placeholder ?? 'filter…'}
                        value={col.filter.value}
                        onChange={(e) => col.filter!.onChange(e.target.value)}
                      />
                    )}
                    {col.headerAux}
                  </th>
                );
              })}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={columns.length} className="px-2 py-10 text-center">
                  <Spinner />
                </td>
              </tr>
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="px-2 py-8 text-center text-sm text-ink-muted">
                  {rows.length === 0 ? 'No data for this filter range.' : 'No rows match your filter.'}
                </td>
              </tr>
            ) : (
              filtered.map((row, i) => (
                <tr key={rowKey(row, i)} className="group hover:bg-hover">
                  {columns.map((col, ci) => (
                    <td
                      key={col.key}
                      className={`px-2 py-1 border-b border-line/50 text-ink${
                        stickyFirstColumn && ci === 0
                          ? ' sticky left-0 z-10 border-r border-line bg-base group-hover:bg-hover'
                          : ''
                      }${col.tdClassName ? ` ${col.tdClassName}` : ''}`}
                    >
                      {col.render ? col.render(row) : String(row[col.key] ?? '')}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {!loading && rows.length > 0 && (
        <div className="shrink-0 px-2 pt-1 text-right text-xs text-ink-muted">{countLabel}</div>
      )}
    </div>
  );
}
