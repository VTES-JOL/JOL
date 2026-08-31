import type { CardDetail } from '../../../api/types';
import type { DeckEntry } from '../deckKit';
import { SectionHeader } from './SectionHeader';

interface Props {
  entries: DeckEntry[];
  detailMap: Map<string, CardDetail>;
}

const MAX_BAR_PX = 44; // container is h-12 (48px); leave a little headroom

export function CryptCapacityCurve({ entries, detailMap }: Props) {
  const cryptEntries = entries.filter((e) => e.isCrypt);
  if (cryptEntries.length === 0) return null;

  const buckets = new Map<number, number>();
  let weightedSum = 0;
  let totalCards = 0;

  for (const entry of cryptEntries) {
    const cap = detailMap.get(entry.cardId)?.capacity;
    if (cap == null) continue;
    buckets.set(cap, (buckets.get(cap) ?? 0) + entry.count);
    weightedSum += cap * entry.count;
    totalCards += entry.count;
  }

  if (buckets.size === 0) return null;

  const avg = totalCards > 0 ? (weightedSum / totalCards).toFixed(1) : '—';
  const minCap = Math.min(...buckets.keys());
  const maxCap = Math.max(...buckets.keys());
  const barMax = Math.max(...buckets.values());
  const caps = Array.from({ length: maxCap - minCap + 1 }, (_, i) => minCap + i);

  return (
    <div className="border-b border-line/50">
      <SectionHeader title="Crypt Capacity" subtitle={`avg ${avg} · range ${minCap}–${maxCap}`} />
      <div className="px-3 py-3">
        <div className="flex items-end gap-0.5 h-12">
          {caps.map((cap) => {
            const count = buckets.get(cap) ?? 0;
            const barPx = count > 0 ? Math.max(Math.round((count / barMax) * MAX_BAR_PX), 4) : 2;
            return (
              <div
                key={cap}
                className={`flex-1 rounded-t transition-all ${count > 0 ? 'bg-accent/70' : 'bg-hover/40'}`}
                style={{ height: barPx }}
                title={`Cap ${cap}: ${count} card${count !== 1 ? 's' : ''}`}
              />
            );
          })}
        </div>
        <div className="flex gap-0.5 mt-1">
          {caps.map((cap) => (
            <div key={cap} className="flex-1 text-center text-[11px] text-ink-muted tabular-nums">
              {cap}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
