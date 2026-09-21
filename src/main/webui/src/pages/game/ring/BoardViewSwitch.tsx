import type { RingLayoutKind } from './boardPrefs';
import type { BoardLevel } from './seatFocus';

// Segmented controls for the board: detail level (Table / Triad / Seat) and, at
// table level, the seat layout (Wedge / Panels). Triad is left out when the game
// has fewer than three live seats. Same controls for every audience.

const LEVEL_LABEL: Record<BoardLevel, string> = { table: 'Table', triad: 'Triad', seat: 'Seat' };
const LAYOUT_LABEL: Record<RingLayoutKind, string> = { wedge: 'Wedge', panels: 'Panels' };

export interface BoardViewSwitchProps {
  level: BoardLevel;
  levels: BoardLevel[];
  layout: RingLayoutKind;
  onLevel: (l: BoardLevel) => void;
  onLayout: (l: RingLayoutKind) => void;
}

function Segmented<T extends string>({
  label,
  value,
  options,
  names,
  onChange,
}: {
  label: string;
  value: T;
  options: T[];
  names: Record<T, string>;
  onChange: (v: T) => void;
}) {
  return (
    <div role="radiogroup" aria-label={label} className="inline-flex overflow-hidden rounded-md border border-line-accent">
      {options.map((o) => (
        <button
          key={o}
          type="button"
          role="radio"
          aria-checked={o === value}
          onClick={() => onChange(o)}
          className={`px-2.5 py-1 text-xs font-medium ${o === value ? 'bg-accent text-white' : 'bg-panel text-ink-secondary hover:bg-hover'}`}
        >
          {names[o]}
        </button>
      ))}
    </div>
  );
}

export function BoardViewSwitch({ level, levels, layout, onLevel, onLayout }: BoardViewSwitchProps) {
  return (
    <div className="flex flex-wrap items-center gap-2">
      <Segmented label="Detail level" value={level} options={levels} names={LEVEL_LABEL} onChange={onLevel} />
      {level === 'table' && (
        <Segmented label="Table layout" value={layout} options={['wedge', 'panels'] as RingLayoutKind[]} names={LAYOUT_LABEL} onChange={onLayout} />
      )}
    </div>
  );
}
