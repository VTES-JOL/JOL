import type { ReactNode } from 'react';
import { Minus, Plus } from 'lucide-react';
import type { DeckEntry } from './deckKit';

/**
 * The +/count/− control cluster wrapping a card row's contents. Ported from
 * jol-quarkus; Tailwind `jt:` -prefixed. Handlers are optional — when absent
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
    'jt:w-9 jt:h-9 jt:sm:w-5 jt:sm:h-5 jt:flex jt:items-center jt:justify-center jt:rounded jt:transition-colors';

  return (
    <div className="jt:flex jt:items-center jt:px-4 jt:py-1.5 jt:gap-1 jt:hover:bg-hover/50 jt:transition-colors">
      {children}

      <div className="jt:flex jt:items-center jt:gap-0.5 jt:shrink-0">
        {!readOnly && (
          <button
            onClick={onDecrement}
            title={willRemove ? 'Remove card' : 'Decrease count'}
            className={[
              stepper,
              willRemove
                ? 'jt:text-blood-soft jt:hover:text-blood jt:hover:bg-blood/10'
                : 'jt:text-ink-secondary jt:hover:text-ink jt:hover:bg-hover',
            ].join(' ')}
          >
            <Minus className="jt:w-3 jt:h-3 jt:sm:w-2.5 jt:sm:h-2.5" />
          </button>
        )}
        <span className="jt:text-xs jt:text-ink jt:text-center jt:tabular-nums jt:w-5">{entry.count}</span>
        {!readOnly && (
          <button
            onClick={onIncrement}
            title="Increase count"
            className={`${stepper} jt:text-ink-secondary jt:hover:text-ink jt:hover:bg-hover`}
          >
            <Plus className="jt:w-3 jt:h-3 jt:sm:w-2.5 jt:sm:h-2.5" />
          </button>
        )}
      </div>
    </div>
  );
}
