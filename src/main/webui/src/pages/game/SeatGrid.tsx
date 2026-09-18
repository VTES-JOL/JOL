import { type ReactNode } from 'react';
import type { PlayerSnapshot } from '../../api/types';

// The opponent-seat layout, factored out of GamePage where the same
// SeatColumn.map lived three times. `renderSeat` carries the per-seat prop
// wiring (handlers, relation, pingable…) so this file is purely the grid.
//
//  - variant="autofill" — the 768–1023 band: cards size to a ~17rem min and
//    grow to fill the row, plain prey-first order.
//  - variant="counted"  — the wide layout: a player-count- and width-aware
//    fixed column count. At the narrow end of the wide range (`midWide`) a
//    4-opponent game folds to 2×2. #opponents scrolls at this breakpoint
//    (F3/D32), so whichever row lands first is the row that's guaranteed
//    visible without scrolling — that has to be prey/predator, the seats a
//    player actually acts on and defends against, not the cross-table seats.
//    Physical-table framing preserved by prominence rather than position:
//    prey (top-left) · predator (top-right) · cross-table (below, scrolls).

export type SeatGridVariant = 'autofill' | 'counted';

function countedLayout(seats: PlayerSnapshot[], midWide: boolean): { ordered: PlayerSnapshot[]; cols: number } {
  const twoWide = midWide && seats.length >= 4;
  const ordered = twoWide ? [seats[0], seats[seats.length - 1], ...seats.slice(1, -1)] : seats;
  const cols = twoWide
    ? 2
    : seats.length >= 5
      ? 3
      : seats.length === 4
        ? 4
        : Math.max(seats.length, 1);
  return { ordered, cols };
}

export function SeatGrid({
  seats,
  variant,
  midWide = false,
  renderSeat,
}: {
  /** Opponent seats, already rotated to the viewer's frame by `seatOrder`. */
  seats: PlayerSnapshot[];
  variant: SeatGridVariant;
  /** Only consulted for `variant="counted"` — the narrow end of the wide range. */
  midWide?: boolean;
  renderSeat: (seat: PlayerSnapshot) => ReactNode;
}) {
  if (variant === 'autofill') {
    return (
      <div className="grid gap-2 items-start [grid-template-columns:repeat(auto-fill,minmax(min(17rem,100%),1fr))]">
        {seats.map((seat) => (
          <div key={seat.name} className="min-w-0">{renderSeat(seat)}</div>
        ))}
      </div>
    );
  }

  const { ordered, cols } = countedLayout(seats, midWide);
  return (
    <div className="grid gap-2 items-start" style={{ gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))` }}>
      {ordered.map((seat) => (
        <div key={seat.name}>{renderSeat(seat)}</div>
      ))}
    </div>
  );
}
