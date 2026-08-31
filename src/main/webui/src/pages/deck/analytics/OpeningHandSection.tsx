import type { DeckEntry } from '../deckKit';
import { SectionHeader } from './SectionHeader';
import { openingHandProb } from './analyticsMath';

function probColor(pct: number) {
  if (pct >= 70) return { text: 'jt:text-online', bar: 'jt:bg-online' };
  if (pct >= 40) return { text: 'jt:text-away', bar: 'jt:bg-away' };
  return { text: 'jt:text-blood-soft', bar: 'jt:bg-blood-soft' };
}

interface Props {
  entries: DeckEntry[];
}

export function OpeningHandSection({ entries }: Props) {
  const cryptEntries = entries.filter((e) => e.isCrypt);
  const cryptTotal = cryptEntries.reduce((s, e) => s + e.count, 0);
  const handEntries = cryptEntries.filter((e) => e.count > 1);

  if (handEntries.length === 0) return null;
  if (!cryptEntries.some((e) => e.count >= 3)) return null;

  const rows = handEntries
    .map((e) => ({ ...e, prob: openingHandProb(cryptTotal, e.count) }))
    .sort((a, b) => b.prob - a.prob);

  return (
    <div className="jt:border-b jt:border-line/50">
      <SectionHeader title="Opening Hand" subtitle={`P(≥1 copy drawn) · ${cryptTotal} crypt · 4 cards`} />
      {rows.map((row) => {
        const pct = Math.round(row.prob * 100);
        const colors = probColor(pct);
        return (
          <div key={row.cardId} className="jt:px-3 jt:py-2 jt:border-b jt:border-line/40 jt:last:border-0">
            <div className="jt:flex jt:items-baseline jt:justify-between jt:gap-1 jt:mb-1.5">
              <span className="jt:text-xs jt:text-ink-secondary jt:truncate jt:min-w-0 jt:leading-none">
                {row.name}
              </span>
              <div className="jt:flex jt:items-baseline jt:gap-1.5 jt:shrink-0">
                <span className="jt:text-[11px] jt:text-ink-muted jt:tabular-nums">×{row.count}</span>
                <span className={`jt:text-xs jt:font-semibold jt:tabular-nums ${colors.text}`}>{pct}%</span>
              </div>
            </div>
            <div className="jt:h-1 jt:rounded-full jt:bg-hover jt:overflow-hidden">
              <div className={`jt:h-full jt:rounded-full jt:transition-all ${colors.bar}`} style={{ width: `${pct}%` }} />
            </div>
          </div>
        );
      })}
      <div className="jt:px-3 jt:py-1.5">
        <p className="jt:text-[11px] jt:text-ink-muted jt:tabular-nums">
          {cryptEntries.length} unique vampire{cryptEntries.length !== 1 ? 's' : ''}
        </p>
      </div>
    </div>
  );
}
