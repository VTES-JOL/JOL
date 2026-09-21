import type { RingSeatModel } from './ringModel';

// Who is a seat's prey / predator, and which detail levels make sense. Pure.

export type BoardLevel = 'table' | 'triad' | 'seat';

export interface Neighbours {
  prey: RingSeatModel | null;
  predator: RingSeatModel | null;
}

const isLive = (s: RingSeatModel) => !s.ousted;

/**
 * Prey = the next live seat after `name` in table order, predator = the previous
 * live one. The server's own fields win when present (they also handle predator
 * reassignment on withdrawal); otherwise this derives them from seat order.
 */
export function neighbours(seats: RingSeatModel[], name: string): Neighbours {
  const i = seats.findIndex((s) => s.name === name);
  if (i < 0) return { prey: null, predator: null };
  const byName = (n: string | null) => (n ? (seats.find((s) => s.name === n) ?? null) : null);
  const walk = (dir: 1 | -1) => {
    for (let step = 1; step < seats.length; step++) {
      const s = seats[(i + dir * step + seats.length * 2) % seats.length];
      if (isLive(s)) return s;
    }
    return null;
  };
  const me = seats[i];
  return {
    prey: byName(me.prey) ?? walk(1),
    predator: byName(me.predator) ?? walk(-1),
  };
}

export const liveCount = (seats: RingSeatModel[]): number => seats.filter(isLive).length;

/** Triad needs a distinct prey and predator, so it is skipped for two-player (or two-left) games. */
export function availableLevels(seats: RingSeatModel[]): BoardLevel[] {
  return liveCount(seats) >= 3 ? ['table', 'triad', 'seat'] : ['table', 'seat'];
}

export const coerceLevel = (level: BoardLevel, seats: RingSeatModel[]): BoardLevel =>
  availableLevels(seats).includes(level) ? level : level === 'triad' ? 'seat' : 'table';

/** Default focus: the viewer's own seat, else the edge holder, else the first live seat. */
export function defaultFocus(seats: RingSeatModel[], viewerName?: string | null): string | null {
  const own = seats.find((s) => s.name === viewerName);
  return (own ?? seats.find((s) => s.edge) ?? seats.find(isLive) ?? seats[0])?.name ?? null;
}

/** Keep a stored focus only if that seat still exists. */
export const resolveFocus = (focus: string | null | undefined, seats: RingSeatModel[], viewerName?: string | null): string | null =>
  focus && seats.some((s) => s.name === focus) ? focus : defaultFocus(seats, viewerName);
