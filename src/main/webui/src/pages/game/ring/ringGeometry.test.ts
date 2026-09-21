import { describe, expect, it } from 'vitest';
import { MAX_SIZE, MIN_SIZE, clampSize, pointInWedge, polar, ringGeometry, seatAngles } from './ringGeometry';

describe('ringGeometry', () => {
  it('clamps size and scales radii', () => {
    expect(clampSize(100)).toBe(MIN_SIZE);
    expect(clampSize(5000)).toBe(MAX_SIZE);
    const g = ringGeometry(1000);
    expect(g.playOut).toBeCloseTo((296 * 1000) / 820);
    expect(g.cx).toBe(500);
  });

  it('puts the anchor seat at the bottom and spaces seats evenly', () => {
    for (const n of [2, 3, 4, 5]) {
      for (let anchor = 0; anchor < n; anchor++) {
        const a = seatAngles(n, anchor);
        expect(a[anchor]).toBeCloseTo(90);
        const step = (((a[(anchor + 1) % n] - a[anchor]) % 360) + 360) % 360;
        expect(step).toBeCloseTo(360 / n, 5);
      }
    }
  });

  it('places the next seat (prey) to the left and the previous (predator) to the right of the bottom seat', () => {
    const g = ringGeometry(820);
    const a = seatAngles(5, 2);
    expect(polar(g, a[3], 300).x).toBeLessThan(g.cx);
    expect(polar(g, a[1], 300).x).toBeGreaterThan(g.cx);
  });

  it('tests wedge membership with a real margin', () => {
    const g = ringGeometry(820);
    const theta = 90;
    const mid = polar(g, theta, (g.inR + g.playOut) / 2);
    expect(pointInWedge(g, 5, theta, mid.x, mid.y, 2)).toBe(true);
    expect(pointInWedge(g, 5, theta, g.cx, g.cy, 0)).toBe(false); // hub
    const side = polar(g, theta + 60, (g.inR + g.playOut) / 2);
    expect(pointInWedge(g, 5, theta, side.x, side.y, 0)).toBe(false); // neighbouring wedge
  });
});
