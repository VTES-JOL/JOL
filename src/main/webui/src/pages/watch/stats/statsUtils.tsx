import { useMemo, useState } from 'react';
import { ChevronsUpDown } from 'lucide-react';

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

type SortMode = 'default' | 'percent' | 'duration' | 'boolean';

export function useTableSort<T extends Record<string, unknown>>(rows: T[]) {
  const [directions, setDirections] = useState<Partial<Record<keyof T, boolean>>>({});
  const [active, setActive] = useState<{ key: keyof T; mode: SortMode } | null>(null);

  const toggle = (key: keyof T, mode: SortMode = 'default') => {
    setDirections((prev) => ({ ...prev, [key]: !prev[key] }));
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

  return { sorted, toggle };
}

export function SortIcon<T>({ column, onSort, mode }: { column: keyof T; onSort: (key: keyof T, mode?: SortMode) => void; mode?: SortMode }) {
  return (
    <ChevronsUpDown
      size={13}
      role="button"
      className="jt:inline jt:ml-1 jt:text-ink-muted jt:hover:text-ink jt:cursor-pointer jt:align-middle"
      onClick={() => onSort(column, mode)}
    />
  );
}
