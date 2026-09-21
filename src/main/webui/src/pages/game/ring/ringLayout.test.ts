import { describe, expect, it } from 'vitest';
import { miniCardFootprint, markerFootprint } from './miniCardFootprint';
import { MAX_SIZE, MIN_SIZE, pointInWedge, seatAngles } from './ringGeometry';
import { computeRingLayout } from './ringLayout';
import { buildRingModel, type RingSeatModel } from './ringModel';
import { turn9Table } from './__fixtures__/ringFixtures';
import { cardView } from './cardView';
import { minion } from '../__fixtures__/gameFixtures';

type Box = { x: number; y: number; w: number; h: number };
const overlaps = (a: Box, b: Box) =>
  Math.abs(a.x - b.x) < (a.w + b.w) / 2 && Math.abs(a.y - b.y) < (a.h + b.h) / 2;

function seatWith(base: RingSeatModel, readyCount: number, lockedEvery = 3): RingSeatModel {
  return {
    ...base,
    ready: Array.from({ length: readyCount }, (_, i) =>
      cardView(minion(`V${i}`, { id: `${base.name}-${i}`, locked: i % lockedEvery === 0, counters: 2, capacity: 5 })),
    ),
  };
}

describe('computeRingLayout', () => {
  for (const n of [2, 3, 4, 5] as const) {
    for (const size of [MIN_SIZE, 820, MAX_SIZE]) {
      it(`keeps every card inside its wedge without overlaps — ${n} players @ ${size}`, () => {
        const { players, seating } = turn9Table(n);
        const model = buildRingModel(players, seating, { viewerName: seating[0] });
        // A busy board: every seat has 8 ready cards, alternating locked / unlocked.
        const seats = model.seats.map((s) => seatWith(s, 8));
        const layout = computeRingLayout(seats, size, model.anchorIndex);
        const thetas = seatAngles(n, model.anchorIndex);

        expect(layout.scale).toBeGreaterThanOrEqual(0.52);
        layout.seats.forEach((sl, si) => {
          expect(sl.overflow).toBe(0);
          expect(sl.ready).toHaveLength(8);
          const boxes = sl.ready.map((p) => {
            const fp = miniCardFootprint(p.rot === 90, layout.scale);
            return { x: p.x, y: p.y, ...fp };
          });
          boxes.forEach((b) => {
            const corners: [number, number][] = [
              [b.x - b.w / 2, b.y - b.h / 2], [b.x + b.w / 2, b.y - b.h / 2],
              [b.x + b.w / 2, b.y + b.h / 2], [b.x - b.w / 2, b.y + b.h / 2],
            ];
            corners.forEach(([x, y]) => expect(pointInWedge(layout.geometry, n, thetas[si], x, y, 0)).toBe(true));
          });
          boxes.forEach((a, i) => boxes.slice(i + 1).forEach((b) => expect(overlaps(a, b)).toBe(false)));
        });
      });
    }
  }

  it('lays the turn-9 board out at a usable scale and separates rim items', () => {
    const { players, seating } = turn9Table(5);
    const model = buildRingModel(players, seating, { viewerName: 'Lysette Marchetti' });
    const layout = computeRingLayout(model.seats, 820, model.anchorIndex);
    expect(layout.scale).toBeGreaterThanOrEqual(0.7);
    layout.seats.forEach((sl, i) => {
      expect(sl.overflow).toBe(0);
      expect(sl.rim).toHaveLength(model.seats[i].torpor.length + model.seats[i].uncontrolled.length);
      const boxes = sl.rim.map((r) => ({
        x: r.x, y: r.y, ...(r.kind === 'card' ? miniCardFootprint(false, layout.scale) : markerFootprint(layout.scale)),
      }));
      boxes.forEach((a, k) => boxes.slice(k + 1).forEach((b) => expect(overlaps(a, b)).toBe(false)));
    });
  });

  it('reports overflow instead of overlapping when a seat is impossibly full', () => {
    const { players, seating } = turn9Table(5);
    const model = buildRingModel(players, seating);
    const seats = model.seats.map((s, i) => (i === 0 ? seatWith(s, 60) : s));
    const layout = computeRingLayout(seats, 820, model.anchorIndex);
    expect(layout.scale).toBe(0.52);
    expect(layout.seats[0].overflow).toBeGreaterThan(0);
    expect(layout.seats[0].ready.length + layout.seats[0].overflow).toBe(60);
  });

  it('is deterministic', () => {
    const { players, seating } = turn9Table(5);
    const model = buildRingModel(players, seating);
    expect(computeRingLayout(model.seats, 820, 1)).toEqual(computeRingLayout(model.seats, 820, 1));
  });
});
