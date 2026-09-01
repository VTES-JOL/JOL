import { memo } from 'react';
import type { CardSnapshot } from '../../api/types';

// Mirrors card-hidden.jsp — no card identity crosses the wire for these (see
// GameSnapshotFactory), so there's nothing to render beyond a placeholder and
// the counter badge. memo'd: all props are ref-stable across an unrelated
// snapshot refetch (structural sharing), no callbacks.
export const CardHidden = memo(function CardHidden({ card, region, coordinate }: { card: CardSnapshot; region: string; coordinate?: string }) {
  const regionStyle = region === 'REMOVED_FROM_GAME' ? 'opacity-50' : '';
  return (
    <li className={`flex justify-between items-center p-1 ${regionStyle}`}>
      <div className="mx-1 me-auto w-full">
        <div className="flex justify-between items-center w-full">
          <span className="flex items-center gap-1">
            {coordinate && <span className="text-ink-muted text-xs tabular-nums select-all shrink-0">{coordinate}</span>}
            <span>*********</span>
          </span>
          {card.counters > 0 && (
            <span className="inline-flex items-center rounded-full bg-blood text-surface px-2 py-0.5 text-xs shadow-sm">
              {card.counters}
            </span>
          )}
        </div>
      </div>
    </li>
  );
});
