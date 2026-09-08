import type { PlayerSnapshot } from '../../api/types';

export interface SeatOrder {
  /** The viewer's own seat, or null for a spectator / judge who isn't seated. */
  me: PlayerSnapshot | null;
  /**
   * Opponent seats, rotated so play flows left → right: the seat immediately
   * after the viewer (their prey) is first, around the table, ending on the
   * seat before the viewer (their predator). Ousted seats are kept in place
   * (they render as a collapsed strip, not removed) so the row still reads as
   * the table's cycle unrolled. A spectator gets every seat in table order.
   */
  others: PlayerSnapshot[];
}

/**
 * Rotate the table to the viewer's frame of reference (D6: prey leftmost →
 * predator rightmost). Pure — unit-tested in seatOrder.test.ts.
 */
export function seatOrder(
  players: PlayerSnapshot[],
  seating: string[],
  viewerName: string | null,
): SeatOrder {
  const byName = new Map(players.map((p) => [p.name, p]));
  const i = viewerName ? seating.indexOf(viewerName) : -1;
  if (i < 0) {
    return { me: null, others: players };
  }
  const rotated = [...seating.slice(i + 1), ...seating.slice(0, i)];
  return {
    me: byName.get(viewerName!) ?? null,
    others: rotated.map((n) => byName.get(n)).filter((p): p is PlayerSnapshot => !!p),
  };
}

// 'table' = a live seat that is neither the viewer's prey nor predator — a
// quiet "across the table" tag (GamePage.relationFor decides it; relationOf
// below, the pre-D8 fallback, only ever returns prey/predator/null).
export type SeatRelation = 'prey' | 'predator' | 'table' | null;

/**
 * Fallback derivation of a seat's relation to the viewer, used only when the
 * server hasn't supplied `PlayerSnapshot.prey` / `.predator` (older responses
 * during the D8 rollout). The server is authoritative when present — it also
 * handles predator reassignment on withdrawal. Prey = first live seat after
 * the viewer, predator = first live seat before; ousted seats are skipped.
 */
export function relationOf(
  seatName: string,
  seating: string[],
  viewerName: string | null,
  isOusted: (name: string) => boolean,
): SeatRelation {
  const n = seating.length;
  const i = viewerName ? seating.indexOf(viewerName) : -1;
  if (i < 0 || n < 2) return null;

  // First live seat after the viewer is their prey.
  for (let step = 1; step < n; step++) {
    const name = seating[(i + step) % n];
    if (name === viewerName) break;
    if (!isOusted(name)) {
      if (name === seatName) return 'prey';
      break;
    }
  }
  // First live seat before the viewer is their predator.
  for (let step = 1; step < n; step++) {
    const name = seating[(i - step + n) % n];
    if (name === viewerName) break;
    if (!isOusted(name)) {
      if (name === seatName) return 'predator';
      break;
    }
  }
  return null;
}
