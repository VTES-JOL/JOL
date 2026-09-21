import { describe, expect, it } from 'vitest';
import { buildRingModel } from './ringModel';
import { TURN9_SEATING, TURN9_VIEWER, turn9Players } from './__fixtures__/ringFixtures';

describe('buildRingModel', () => {
  const players = turn9Players();

  it('orders by seating and anchors on the viewer', () => {
    const m = buildRingModel(players.slice().reverse(), TURN9_SEATING, { viewerName: TURN9_VIEWER });
    expect(m.seats.map((s) => s.name)).toEqual(TURN9_SEATING);
    expect(m.seats[m.anchorIndex].name).toBe(TURN9_VIEWER);
  });

  it('anchors on focus, then the edge holder, then seat 0 when there is no viewer', () => {
    expect(buildRingModel(players, TURN9_SEATING, { focusName: 'Ilya Rostova' }).anchorIndex).toBe(3);
    expect(buildRingModel(players, TURN9_SEATING).seats[buildRingModel(players, TURN9_SEATING).anchorIndex].name).toBe('Marcus Kane');
    const noEdge = players.map((p) => ({ ...p, edge: false }));
    expect(buildRingModel(noEdge, TURN9_SEATING).anchorIndex).toBe(0);
  });

  it('sorts ready cards vampires → allies → locations and splits torpor / uncontrolled', () => {
    const marcus = buildRingModel(players, TURN9_SEATING).seats[0];
    expect(marcus.ready.map((c) => c.kind)).toEqual(['vampire', 'vampire', 'vampire', 'vampire', 'ally', 'location']);
    expect(marcus.torpor).toHaveLength(1);
    expect(marcus.torpor[0].torpor).toBe(true);
    expect(marcus.uncontrolled).toHaveLength(4);
    expect(marcus.uncontrolled[2].hidden).toBe(true);
    expect(marcus.uncontrolled[2].name).toBe('');
  });

  it('flags ousted seats', () => {
    const out = players.map((p) => (p.name === 'The Baron' ? { ...p, pool: 0 } : p));
    expect(buildRingModel(out, TURN9_SEATING).seats[4].ousted).toBe(true);
  });
});
