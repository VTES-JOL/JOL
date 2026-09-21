import { describe, expect, it } from 'vitest';
import { BASE_SLOTS, pipGrid } from './pipLayout';

describe('pipGrid', () => {
  it('always shows at least five slots, in one full-size row', () => {
    for (const n of [0, 1, 3, 5]) {
      const g = pipGrid(n);
      expect(g.slots).toBe(BASE_SLOTS);
      expect(g.rows).toBe(1);
      expect(g.size).toBeGreaterThanOrEqual(6.5);
    }
  });

  it('grows the slot count with the counters, never below the counters', () => {
    for (const n of [6, 9, 15, 30]) expect(pipGrid(n).slots).toBe(n);
  });

  it('only ever shrinks the pips as counters grow', () => {
    let prev = Infinity;
    for (let n = 0; n <= 40; n++) {
      const s = pipGrid(n).size;
      expect(s).toBeLessThanOrEqual(prev + 1e-9);
      prev = s;
    }
  });

  it('keeps every pip inside the box', () => {
    for (let n = 0; n <= 40; n++) {
      const g = pipGrid(n);
      expect(g.cols * g.rows).toBeGreaterThanOrEqual(g.slots);
      expect(g.cols * g.size + (g.cols - 1)).toBeLessThanOrEqual(37 + 1e-6);
      expect(g.rows * g.size + (g.rows - 1)).toBeLessThanOrEqual(12 + 1e-6);
    }
  });
});
