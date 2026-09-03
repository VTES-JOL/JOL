import { useMemo, useState } from 'react';
import { ArrowDown, ArrowUp, ChevronsUpDown } from 'lucide-react';

// Mirrors ds.js's sortTable()/sortPercentageTable(): numeric-aware string
// compare, or percent-string compare, toggling independently per column
// (clicking column B doesn't reset column A's remembered direction).
function compareValues(a: unknown, b: unknown, ascending: boolean): number {
  const aNum = Number(a);
  const bNum = Number(b);
  const bothNumeric = a !== '' && b !== '' && !Number.isNaN(aNum) && !Number.isNaN(bNum);
  if (bothNumeric) return ascending ? aNum - bNum : bNum - aNum;
  const as = String(a).toLowerCase();
  const bs = String(b).toLowerCase();
  if (as === bs) return 0;
  const cmp = as > bs ? 1 : -1;
  return ascending ? cmp : -cmp;
}

function comparePercent(a: unknown, b: unknown, ascending: boolean): number {
  const av = parseFloat(String(a).replace('%', '')) || 0;
  const bv = parseFloat(String(b).replace('%', '')) || 0;
  return ascending ? av - bv : bv - av;
}

export function parseDurationSeconds(duration: string): number {
  const match = duration.match(/(?:(\d+)d)?\s*(?:(\d+)h)?\s*(?:(\d+)m)?\s*(?:(\d+)s)?/);
  if (!match) return 0;
  return (
    Number(match[1] || 0) * 86400 + Number(match[2] || 0) * 3600 + Number(match[3] || 0) * 60 + Number(match[4] || 0)
  );
}

export type SortMode = 'default' | 'percent' | 'duration' | 'boolean';

export interface SortSpec<T> {
  key: keyof T;
  mode?: SortMode;
  /** Initial direction for this column when it first becomes active. Default ascending. */
  ascending?: boolean;
}

export function useTableSort<T extends Record<string, unknown>>(rows: T[], initial?: SortSpec<T>) {
  const [directions, setDirections] = useState<Partial<Record<keyof T, boolean>>>(
    initial ? { [initial.key]: initial.ascending ?? true } as Partial<Record<keyof T, boolean>> : {},
  );
  const [active, setActive] = useState<{ key: keyof T; mode: SortMode } | null>(
    initial ? { key: initial.key, mode: initial.mode ?? 'default' } : null,
  );

  const toggle = (key: keyof T, mode: SortMode = 'default') => {
    setDirections((prev) => ({ ...prev, [key]: !(prev[key] ?? true) }));
    setActive({ key, mode });
  };

  const sorted = useMemo(() => {
    if (!active) return rows;
    const ascending = directions[active.key] ?? true;
    const copy = [...rows];
    if (active.mode === 'percent') {
      copy.sort((a, b) => comparePercent(a[active.key], b[active.key], ascending));
    } else if (active.mode === 'duration') {
      copy.sort((a, b) =>
        ascending
          ? parseDurationSeconds(String(a[active.key])) - parseDurationSeconds(String(b[active.key]))
          : parseDurationSeconds(String(b[active.key])) - parseDurationSeconds(String(a[active.key])),
      );
    } else if (active.mode === 'boolean') {
      copy.sort((a, b) => {
        const av = Number(Boolean(a[active.key]));
        const bv = Number(Boolean(b[active.key]));
        return ascending ? av - bv : bv - av;
      });
    } else {
      copy.sort((a, b) => compareValues(a[active.key], b[active.key], ascending));
    }
    return copy;
  }, [rows, active, directions]);

  const direction: 'asc' | 'desc' | null = active
    ? (directions[active.key] ?? true)
      ? 'asc'
      : 'desc'
    : null;

  return { sorted, toggle, activeKey: (active?.key ?? null) as keyof T | null, direction };
}

/** Column-header sort affordance: a direction arrow on the active column, a dim hint otherwise. */
export function SortIndicator({ state }: { state: 'asc' | 'desc' | 'none' }) {
  if (state === 'asc') return <ArrowUp size={13} className="inline ml-1 align-middle text-accent" />;
  if (state === 'desc') return <ArrowDown size={13} className="inline ml-1 align-middle text-accent" />;
  return <ChevronsUpDown size={13} className="inline ml-1 align-middle text-ink-muted/60 group-hover:text-ink-muted" />;
}
