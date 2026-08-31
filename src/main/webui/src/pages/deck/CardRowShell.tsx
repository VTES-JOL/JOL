import type { ReactNode } from 'react';
import { Minus, Plus } from 'lucide-react';
import type { DeckEntry } from './deckKit';

/**
 * The +/count/− control cluster wrapping a card row's contents. Ported from
 * jol-quarkus; Tailwind Tailwind-based. Handlers are optional — when absent
 * the row renders read-only (Phase 2 skeleton).
 */
interface Props {
  entry: DeckEntry;
  onIncrement?: () => void;
  onDecrement?: () => void;
  children: ReactNode;
}

export function CardRowShell({ entry, onIncrement, onDecrement, children }: Props) {
  const willRemove = entry.count === 1;
  const readOnly = !onIncrement && !onDecrement;

  // Steppers are comfortably tappable on touch widths and tighten up from `sm`.
  const stepper =
    'w-9 h-9 sm:w-5 sm:h-5 flex items-center justify-center rounded transition-colors';

  return (
    <div className="flex items-center px-4 py-1.5 gap-1 hover:bg-hover/50 transition-colors">
      {children}

      <div className="flex items-center gap-0.5 shrink-0">
        {!readOnly && (
          <button
            onClick={onDecrement}
            title={willRemove ? 'Remove card' : 'Decrease count'}
            className={[
              stepper,
              willRemove
                ? 'text-blood-soft hover:text-blood hover:bg-blood/10'
                : 'text-ink-secondary hover:text-ink hover:bg-hover',
            ].join(' ')}
          >
            <Minus className="w-3 h-3 sm:w-2.5 sm:h-2.5" />
          </button>
        )}
        <span className="text-xs text-ink text-center tabular-nums w-5">{entry.count}</span>
        {!readOnly && (
          <button
            onClick={onIncrement}
            title="Increase count"
            className={`${stepper} text-ink-secondary hover:text-ink hover:bg-hover`}
          >
            <Plus className="w-3 h-3 sm:w-2.5 sm:h-2.5" />
          </button>
        )}
      </div>
    </div>
  );
}
