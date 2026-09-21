import { describe, expect, it } from 'vitest';
import { buildRingModel } from './ringModel';
import { computePanelLayout, panelColumns, type PanelBox } from './panelLayout';
import { turn9Table } from './__fixtures__/ringFixtures';
import { cardView } from './cardView';
import { minion } from '../__fixtures__/gameFixtures';
import { defaultLayoutForWidth, resolveLayout } from './boardPrefs';

const overlap = (a: PanelBox, b: PanelBox) => a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h;

describe('computePanelLayout', () => {
  for (const n of [2, 3, 4, 5] as const) {
    it(`never overlaps panels and keeps everything inside the stage — ${n} players`, () => {
      const { players, seating } = turn9Table(n);
      const m = buildRingModel(players, seating, { viewerName: seating[0] });
      const l = computePanelLayout(m.seats, m.anchorIndex);
      l.panels.forEach((p, i) => {
        expect(p.x).toBeGreaterThanOrEqual(0);
        expect(p.y).toBeGreaterThanOrEqual(0);
        expect(p.x + p.w).toBeLessThanOrEqual(l.width);
        expect(p.y + p.h).toBeLessThanOrEqual(l.height);
        l.panels.slice(i + 1).forEach((q) => expect(overlap(p, q)).toBe(false));
      });
    });
  }

  it('keeps every card and rim item inside its panel, one grid cell each', () => {
    const { players, seating } = turn9Table(5);
    const m = buildRingModel(players, seating);
    const l = computePanelLayout(m.seats, m.anchorIndex);
    l.panels.forEach((p, i) => {
      expect(p.cards).toHaveLength(m.seats[i].ready.length);
      expect(p.rim).toHaveLength(m.seats[i].torpor.length + m.seats[i].uncontrolled.length);
      [...p.cards, ...p.rim].forEach((c) => {
        expect(c.x).toBeGreaterThan(0);
        expect(c.x).toBeLessThan(p.w);
        expect(c.y).toBeGreaterThan(0);
        expect(c.y).toBeLessThan(p.h);
      });
      const cells = new Set(p.cards.map((c) => `${c.x},${c.y}`));
      expect(cells.size).toBe(p.cards.length);
    });
  });

  it('puts the anchor at the bottom and its prey on the left', () => {
    const { players, seating } = turn9Table(5);
    const m = buildRingModel(players, seating, { viewerName: 'Lysette Marchetti' });
    const l = computePanelLayout(m.seats, m.anchorIndex);
    const mid = (p: PanelBox) => ({ x: p.x + p.w / 2, y: p.y + p.h / 2 });
    const me = mid(l.panels[m.anchorIndex]);
    const prey = mid(l.panels[(m.anchorIndex + 1) % 5]);
    const pred = mid(l.panels[(m.anchorIndex + 4) % 5]);
    expect(me.y).toBeGreaterThan(l.cy);
    expect(prey.x).toBeLessThan(l.cx);
    expect(pred.x).toBeGreaterThan(l.cx);
  });

  it('grows panels for a busy seat instead of shrinking or overflowing', () => {
    const { players, seating } = turn9Table(5);
    const m = buildRingModel(players, seating);
    const seats = m.seats.map((s, i) =>
      i === 0 ? { ...s, ready: Array.from({ length: 30 }, (_, k) => cardView(minion(`V${k}`, { id: `x${k}` }))) } : s,
    );
    const l = computePanelLayout(seats, 0);
    expect(panelColumns(30)).toBe(6);
    expect(l.panels[0].cards).toHaveLength(30);
    expect(l.panels[0].h).toBeGreaterThan(l.panels[1].h);
  });
});

describe('layout preference resolution', () => {
  it('defaults by width', () => {
    expect(defaultLayoutForWidth(1024)).toBe('wedge');
    expect(defaultLayoutForWidth(1023)).toBe('panels');
    expect(defaultLayoutForWidth(390)).toBe('panels');
  });
  it('prefers the session toggle, then the profile, then width', () => {
    expect(resolveLayout('panels', 'wedge', 1920)).toBe('panels');
    expect(resolveLayout(null, 'panels', 1920)).toBe('panels');
    expect(resolveLayout(undefined, null, 1920)).toBe('wedge');
    expect(resolveLayout(undefined, undefined, 800)).toBe('panels');
  });
});
