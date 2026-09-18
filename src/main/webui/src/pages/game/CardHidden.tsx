import { memo, type KeyboardEvent, type MouseEvent } from 'react';
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
  // F10: same keyboard-activation fake as CardSimple — callers key an action
  // menu's position off e.clientX/clientY.
  const onKeyDown = onClick
    ? (e: KeyboardEvent<HTMLLIElement>) => {
        if (e.key !== 'Enter' && e.key !== ' ') return;
        e.preventDefault();
        const r = e.currentTarget.getBoundingClientRect();
        onClick({
          clientX: r.left + r.width / 2,
          clientY: r.top + r.height / 2,
          stopPropagation: () => {},
          preventDefault: () => {},
        } as unknown as MouseEvent);
      }
    : undefined;
  return (
    <li
      className={`flex justify-between items-center p-1 ${regionStyle} ${
        onClick ? 'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent' : ''
      }`}
      onClick={onClick}
      onContextMenu={onContextMenu}
      onKeyDown={onKeyDown}
      style={onClick ? { cursor: 'pointer' } : undefined}
      {...(onClick ? { role: 'button' as const, tabIndex: 0 } : {})}
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
            <span className="inline-flex items-center rounded-full bg-blood text-white px-2 py-0.5 text-xs shadow-sm">
              {card.counters}
            </span>
          )}
        </div>
      </div>
    </li>
  );
});
