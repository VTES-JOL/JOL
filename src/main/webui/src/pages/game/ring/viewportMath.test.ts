import { describe, expect, it } from 'vitest';
import { HOME, MAX_ZOOM, MIN_ZOOM, PAN_MARGIN, baseOffset, clampPan, panBy, wheelFactor, zoomAt, type ViewBox } from './viewportMath';

const box: ViewBox = { viewW: 1000, viewH: 800, contentW: 700, contentH: 700 };
/** Screen position of a content-local point. */
const screen = (v: { z: number; px: number; py: number }, u: number, w: number) => {
  const { bx, by } = baseOffset(box);
  return { x: v.px + v.z * (bx + u), y: v.py + v.z * (by + w) };
};

describe('viewportMath', () => {
  it('keeps the point under the cursor fixed while zooming', () => {
    const cx = 620, cy = 300;
    // The content point currently under the cursor at home:
    const { bx, by } = baseOffset(box);
    const u = cx - bx, w = cy - by;
    const v = zoomAt(HOME, box, cx, cy, 1.5);
    const s = screen(v, u, w);
    expect(s.x).toBeCloseTo(cx);
    expect(s.y).toBeCloseTo(cy);
    expect(v.z).toBeCloseTo(1.5);
  });

  it('clamps zoom to the allowed range', () => {
    let v = HOME;
    for (let i = 0; i < 40; i++) v = zoomAt(v, box, 500, 400, 1.5);
    expect(v.z).toBe(MAX_ZOOM);
    for (let i = 0; i < 80; i++) v = zoomAt(v, box, 500, 400, 0.5);
    expect(v.z).toBe(MIN_ZOOM);
  });

  it('never lets the content leave the view entirely', () => {
    const far = panBy(HOME, box, 99999, -99999);
    const { bx, by } = baseOffset(box);
    // content's left edge may not pass (viewW - margin), its bottom edge not above margin
    expect(far.px + far.z * bx).toBeLessThanOrEqual(box.viewW - PAN_MARGIN + 1e-6);
    expect(far.py + far.z * (by + box.contentH)).toBeGreaterThanOrEqual(PAN_MARGIN - 1e-6);
    const back = panBy(HOME, box, -99999, 99999);
    expect(back.px + back.z * (bx + box.contentW)).toBeGreaterThanOrEqual(PAN_MARGIN - 1e-6);
    expect(back.py + back.z * by).toBeLessThanOrEqual(box.viewH - PAN_MARGIN + 1e-6);
  });

  it('leaves a reachable pan untouched', () => {
    expect(clampPan({ z: 1, px: 20, py: -30 }, box)).toEqual({ z: 1, px: 20, py: -30 });
  });

  it('turns wheel deltas into symmetric zoom factors', () => {
    expect(wheelFactor(-100, 0) * wheelFactor(100, 0)).toBeCloseTo(1);
    expect(wheelFactor(-100, 0)).toBeGreaterThan(1);
    expect(wheelFactor(-3, 1)).toBeCloseTo(wheelFactor(-48, 0)); // line mode = 16px
  });
});
