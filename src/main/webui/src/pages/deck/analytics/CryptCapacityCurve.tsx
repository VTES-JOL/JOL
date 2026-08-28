import type { CardDetail } from '../../../api/types';
import type { DeckEntry } from '../../../components/ui/deckKit';
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
    <div className="jt:border-b jt:border-line/50">
      <SectionHeader title="Crypt Capacity" subtitle={`avg ${avg} · range ${minCap}–${maxCap}`} />
      <div className="jt:px-3 jt:py-3">
        <div className="jt:flex jt:items-end jt:gap-0.5 jt:h-12">
          {caps.map((cap) => {
            const count = buckets.get(cap) ?? 0;
            const barPx = count > 0 ? Math.max(Math.round((count / barMax) * MAX_BAR_PX), 4) : 2;
            return (
              <div
                key={cap}
                className={`jt:flex-1 jt:rounded-t jt:transition-all ${count > 0 ? 'jt:bg-accent/70' : 'jt:bg-hover/40'}`}
                style={{ height: barPx }}
                title={`Cap ${cap}: ${count} card${count !== 1 ? 's' : ''}`}
              />
            );
          })}
        </div>
        <div className="jt:flex jt:gap-0.5 jt:mt-1">
          {caps.map((cap) => (
            <div key={cap} className="jt:flex-1 jt:text-center jt:text-[11px] jt:text-ink-muted jt:tabular-nums">
              {cap}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
