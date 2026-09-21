import { describe, expect, it } from 'vitest';
import { PIPS_PER_ROW, allyPipRows } from './pipLayout';

describe('allyPipRows', () => {
  it('shows nothing for no counters', () => {
    expect(allyPipRows(0).rows).toEqual([]);
  });

  it('is exactly one pip per counter — no minimum, no empty slots', () => {
    for (const n of [1, 2, 3, 5, 7, 10]) expect(allyPipRows(n).rows).toEqual([n]);
  });

  it('never loses or invents pips', () => {
    for (let n = 0; n <= 60; n++) expect(allyPipRows(n).rows.reduce((a, b) => a + b, 0)).toBe(n);
  });

  it('splits into balanced rows past the row limit, with shorter pips', () => {
    expect(allyPipRows(PIPS_PER_ROW + 1).rows).toEqual([6, 5]);
    expect(allyPipRows(20).rows).toEqual([10, 10]);
    expect(allyPipRows(14).height).toBeLessThan(allyPipRows(3).height);
    expect(allyPipRows(45).height).toBeLessThanOrEqual(allyPipRows(14).height);
  });
});
