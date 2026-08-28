import type { CardDetail } from '../../../api/types';
import type { DeckEntry } from '../../../components/ui/deckKit';
import { BarRow } from './BarRow';
import { SectionHeader } from './SectionHeader';

interface Props {
  entries: DeckEntry[];
  detailMap: Map<string, CardDetail>;
}

interface CostRow {
  label: string;
  count: number;
  color: string;
}

export function LibraryCostSection({ entries, detailMap }: Props) {
  const libEntries = entries.filter((e) => !e.isCrypt);
  if (libEntries.length === 0) return null;

  let free = 0;
  const poolBuckets = new Map<number | 'x', number>();
  const bloodBuckets = new Map<number | 'x', number>();

  for (const entry of libEntries) {
    const d = detailMap.get(entry.cardId);
    if (!d) continue;
    const hasPool = d.poolCost != null;
    const hasBlood = d.bloodCost != null;
    if (!hasPool && !hasBlood) {
      free += entry.count;
      continue;
    }
    if (hasPool) {
      const key = d.poolCost === -1 ? 'x' : (d.poolCost as number);
      poolBuckets.set(key, (poolBuckets.get(key) ?? 0) + entry.count);
    }
    if (hasBlood) {
      const key = d.bloodCost === -1 ? 'x' : (d.bloodCost as number);
      bloodBuckets.set(key, (bloodBuckets.get(key) ?? 0) + entry.count);
    }
  }

  function sortedRows(map: Map<number | 'x', number>, prefix: string, color: string): CostRow[] {
    return [...map.entries()]
      .sort(([a], [b]) => {
        if (a === 'x') return 1;
        if (b === 'x') return -1;
        return (a as number) - (b as number);
      })
      .map(([key, count]) => ({ label: `${prefix} ${key === 'x' ? 'X' : key}`, count, color }));
  }

  const rows: CostRow[] = [
    ...(free > 0 ? [{ label: 'Free', count: free, color: 'jt:bg-online/70' }] : []),
    ...sortedRows(poolBuckets, 'Pool', 'jt:bg-accent/70'),
    ...sortedRows(bloodBuckets, 'Blood', 'jt:bg-blood-soft/80'),
  ];

  if (rows.length === 0) return null;
  const max = Math.max(...rows.map((r) => r.count));

  return (
    <div className="jt:border-b jt:border-line/50">
      <SectionHeader title="Library Costs" />
      <div className="jt:py-1">
        {rows.map((row) => (
          <BarRow key={row.label} label={row.label} count={row.count} max={max} color={row.color} />
        ))}
      </div>
    </div>
  );
}
