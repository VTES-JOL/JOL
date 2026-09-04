import { memo, type MouseEvent } from 'react';
import { Square } from 'lucide-react';
import type { CardSnapshot } from '../../api/types';

// Mirrors card-hidden.jsp — no card identity crosses the wire for these (see
// GameSnapshotFactory / CardVisibility), so there's nothing to render beyond a
// placeholder and the counter badge. Two shapes:
//   - a hidden hand/library card -> the classic asterisks
//   - a face-down card in a visible region -> a card back, so it reads as a
//     real card someone deliberately turned over, not a pile you can't see into
// Any (rare) visible children of a face-down card are rendered by the caller
// (Card.tsx), not here. memo'd: all props are ref-stable across an unrelated
// snapshot refetch (structural sharing).
export const CardHidden = memo(function CardHidden({
  card,
  region,
  coordinate,
  onClick,
  onContextMenu,
}: {
  card: CardSnapshot;
  region: string;
  coordinate?: string;
  onClick?: (e: MouseEvent) => void;
  onContextMenu?: (e: MouseEvent) => void;
}) {
  const regionStyle = region === 'REMOVED_FROM_GAME' ? 'opacity-50' : '';
  return (
    <li
      className={`flex justify-between items-center p-1 ${regionStyle}`}
      onClick={onClick}
      onContextMenu={onContextMenu}
      style={onClick ? { cursor: 'pointer' } : undefined}
    >
      <div className="mx-1 me-auto w-full">
        <div className="flex justify-between items-center w-full">
          <span className="flex items-center gap-1">
            {coordinate && <span className="text-ink-muted text-xs tabular-nums select-all shrink-0">{coordinate}</span>}
            {card.faceDown ? (
              <span className="flex items-center gap-1 text-ink-muted italic">
                <Square size={13} className="fill-current" />
                face-down card
              </span>
            ) : (
              <span>*********</span>
            )}
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
