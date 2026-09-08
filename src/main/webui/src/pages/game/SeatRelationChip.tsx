import { ChevronDown, ChevronUp } from 'lucide-react';
import type { SeatRelation } from './seatOrder';

// The "▼ Your prey" / "▲ Your predator" tag above an opponent's seat. Prey is
// green (calm, downstream) — deliberately NOT the accent violet, which the
// active-seat border also uses (ui-design: your prey is usually the active
// seat, so violet-on-violet was ambiguous). Predator is gold: the seat that
// acts on you.
export function SeatRelationChip({ relation }: { relation: SeatRelation }) {
  if (!relation) return null;
  if (relation === 'table') {
    return (
      <span className="inline-flex items-center rounded-full px-2 py-0.5 text-[0.65rem] font-semibold uppercase tracking-wide text-ink-muted">
        Across the table
      </span>
    );
  }
  const isPrey = relation === 'prey';
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[0.65rem] font-semibold uppercase tracking-wide ${
        isPrey ? 'bg-online/15 text-online' : 'bg-gold/15 text-gold'
      }`}
    >
      {isPrey ? <ChevronDown size={11} /> : <ChevronUp size={11} />}
      {isPrey ? 'Your prey' : 'Your predator'}
    </span>
  );
}
