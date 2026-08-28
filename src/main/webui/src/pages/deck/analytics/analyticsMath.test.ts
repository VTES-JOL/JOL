import { describe, expect, it } from 'vitest';
import { comb, openingHandProb } from './analyticsMath';

describe('comb', () => {
  it('C(n,0) = C(n,n) = 1', () => {
    expect(comb(7, 0)).toBe(1);
    expect(comb(7, 7)).toBe(1);
  });
  it('C(5,2) = 10, C(12,4) = 495', () => {
    expect(comb(5, 2)).toBe(10);
    expect(comb(12, 4)).toBe(495);
  });
  it('out-of-range k → 0', () => {
    expect(comb(4, 5)).toBe(0);
    expect(comb(4, -1)).toBe(0);
  });
});

describe('openingHandProb', () => {
  it('0 copies → 0', () => {
    expect(openingHandProb(12, 0)).toBe(0);
  });
  it('crypt not bigger than the draw → certain', () => {
    expect(openingHandProb(4, 1)).toBe(1);
    expect(openingHandProb(3, 1)).toBe(1);
  });
  it('matches the hypergeometric value', () => {
    // N=12, K=3, n=4  →  1 - C(9,4)/C(12,4) = 1 - 126/495
    expect(openingHandProb(12, 3)).toBeCloseTo(1 - 126 / 495, 6);
    // N=12, K=6  →  1 - C(6,4)/C(12,4) = 1 - 15/495
    expect(openingHandProb(12, 6)).toBeCloseTo(1 - 15 / 495, 6);
  });
  it('more copies → higher probability', () => {
    expect(openingHandProb(12, 4)).toBeGreaterThan(openingHandProb(12, 2));
  });
});
