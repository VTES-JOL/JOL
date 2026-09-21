import { markerFootprint, miniCardFootprint } from './miniCardFootprint';
import {
  GAP_DEG,
  deg,
  halfWedge,
  polar,
  rad,
  ringGeometry,
  seatAngles,
  type Point,
  type RingGeometry,
} from './ringGeometry';
import { packSeats } from './ringPacker';
import type { RingSeatModel } from './ringModel';

// Turns seat models into positions: the pure result RingBoard / RingSeat draw.
// Memoise on (seats, size, anchorIndex) — pan / zoom never changes it.

export interface PlacedCard {
  id: string;
  x: number;
  y: number;
  /** 0, or 90 for a locked card. */
  rot: 0 | 90;
}

export interface PlacedRim {
  kind: 'card' | 'marker';
  id: string;
  x: number;
  y: number;
}

export interface SeatLayout {
  seatIndex: number;
  /** Centre angle and wedge edges, degrees. */
  theta: number;
  a0: number;
  a1: number;
  ready: PlacedCard[];
  /** Ready cards that did not fit at the smallest scale. */
  overflow: number;
  /** Torpor cards then uncontrolled markers, spread along the outer rim. */
  rim: PlacedRim[];
}

export interface RingLayout {
  geometry: RingGeometry;
  /** Global glyph scale chosen by the packer (0.52 – 1). */
  scale: number;
  seats: SeatLayout[];
}

const RIM_GAP = 7;

/** Spread rim items along the outer arc, centred on the seat, by tangential extent. */
function layoutRim(g: RingGeometry, theta: number, scale: number, seat: RingSeatModel): PlacedRim[] {
  const tangent = theta + 90;
  const items = [
    ...seat.torpor.map((c) => ({ kind: 'card' as const, id: c.id, size: miniCardFootprint(false, scale) })),
    ...seat.uncontrolled.map((u) => ({ kind: 'marker' as const, id: u.id, size: markerFootprint(scale) })),
  ];
  const extent = (s: { w: number; h: number }) =>
    Math.abs(s.w * Math.cos(rad(tangent))) + Math.abs(s.h * Math.sin(rad(tangent)));
  const gap = RIM_GAP * scale;
  const total = items.reduce((t, it) => t + extent(it.size), 0) + gap * Math.max(0, items.length - 1);
  let a = theta - deg(total / g.rimR) / 2;
  return items.map((it) => {
    const e = extent(it.size);
    const p: Point = polar(g, a + deg(e / g.rimR) / 2, g.rimR);
    a += deg((e + gap) / g.rimR);
    return { kind: it.kind, id: it.id, x: p.x, y: p.y };
  });
}

export function computeRingLayout(seats: RingSeatModel[], size: number, anchorIndex = 0): RingLayout {
  const geometry = ringGeometry(size);
  const n = seats.length;
  const thetas = seatAngles(n, anchorIndex);
  const half = halfWedge(n);
  const pack = packSeats(geometry, thetas, seats.map((s) => s.ready.length));

  return {
    geometry,
    scale: pack.scale,
    seats: seats.map((seat, i) => {
      const packed = pack.seats[i];
      const mid = polar(geometry, thetas[i], (geometry.inR + geometry.playOut) / 2);
      return {
        seatIndex: seat.seatIndex,
        theta: thetas[i],
        a0: thetas[i] - half + GAP_DEG,
        a1: thetas[i] + half - GAP_DEG,
        ready: seat.ready.slice(0, packed.cells.length).map((c, j) => ({
          id: c.id,
          x: (packed.cells[j] ?? mid).x,
          y: (packed.cells[j] ?? mid).y,
          rot: c.locked ? 90 : 0,
        })),
        overflow: packed.overflow,
        rim: layoutRim(geometry, thetas[i], pack.scale, seat),
      };
    }),
  };
}
