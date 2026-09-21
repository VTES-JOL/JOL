// Pure ring geometry. The ring is authored at DESIGN_SIZE (820px) and every
// radius scales by k = size / DESIGN_SIZE; card glyphs are NOT scaled by k —
// they shrink only through the packer's global scale, so they stay legible.

export const DESIGN_SIZE = 820;
export const MIN_SIZE = 560;
export const MAX_SIZE = 1000;
/** Angular gap kept on each side of a wedge, degrees. */
export const GAP_DEG = 1.6;

export interface RingGeometry {
  size: number;
  k: number;
  cx: number;
  cy: number;
  /** Radii, px at this size. */
  hubR: number;
  inR: number;
  playOut: number;
  rimR: number;
  outR: number;
}

export interface Point {
  x: number;
  y: number;
}

export const clampSize = (size: number): number => Math.max(MIN_SIZE, Math.min(MAX_SIZE, size || DESIGN_SIZE));

export function ringGeometry(rawSize: number): RingGeometry {
  const size = clampSize(rawSize);
  const k = size / DESIGN_SIZE;
  return {
    size,
    k,
    cx: size / 2,
    cy: size / 2,
    hubR: 64 * k,
    inR: 84 * k,
    playOut: 296 * k,
    rimR: 350 * k,
    outR: 398 * k,
  };
}

export const rad = (deg: number): number => (deg * Math.PI) / 180;
export const deg = (r: number): number => (r * 180) / Math.PI;

export function polar(g: Pick<RingGeometry, 'cx' | 'cy'>, angleDeg: number, r: number): Point {
  return { x: g.cx + r * Math.cos(rad(angleDeg)), y: g.cy + r * Math.sin(rad(angleDeg)) };
}

/** Half the angular width of one seat's wedge. */
export const halfWedge = (seatCount: number): number => 180 / Math.max(1, seatCount);

/**
 * Centre angle of each seat, degrees (0° = right, 90° = bottom, y down).
 * Seats run clockwise in table order; `anchorIndex` is rotated to the bottom.
 * With seating order prey = next, that puts the anchor's prey on the left and
 * its predator on the right.
 */
export function seatAngles(seatCount: number, anchorIndex = 0): number[] {
  const n = Math.max(1, seatCount);
  const step = 360 / n;
  const anchor = anchorIndex >= 0 && anchorIndex < n ? anchorIndex : 0;
  const offset = 90 - (-90 + anchor * step);
  return Array.from({ length: n }, (_, i) => -90 + i * step + offset);
}

const f1 = (n: number): number => Math.round(n * 10) / 10;

/** SVG path for an annular sector between angles a0..a1 and radii r0..r1. */
export function sectorPath(g: Pick<RingGeometry, 'cx' | 'cy'>, a0: number, a1: number, r0: number, r1: number): string {
  const large = a1 - a0 > 180 ? 1 : 0;
  const p1 = polar(g, a0, r1);
  const p2 = polar(g, a1, r1);
  const p3 = polar(g, a1, r0);
  const p4 = polar(g, a0, r0);
  return (
    `M ${f1(p1.x)} ${f1(p1.y)} A ${f1(r1)} ${f1(r1)} 0 ${large} 1 ${f1(p2.x)} ${f1(p2.y)}` +
    ` L ${f1(p3.x)} ${f1(p3.y)} A ${f1(r0)} ${f1(r0)} 0 ${large} 0 ${f1(p4.x)} ${f1(p4.y)} Z`
  );
}

/** Open arc path (the divider between the play area and the rim lane). */
export function arcPath(g: Pick<RingGeometry, 'cx' | 'cy'>, a0: number, a1: number, r: number): string {
  const p0 = polar(g, a0, r);
  const p1 = polar(g, a1, r);
  return `M ${f1(p0.x)} ${f1(p0.y)} A ${f1(r)} ${f1(r)} 0 ${a1 - a0 > 180 ? 1 : 0} 1 ${f1(p1.x)} ${f1(p1.y)}`;
}

/**
 * Is (x, y) inside the play annulus of the wedge centred on `theta`, keeping at
 * least `margin` px from every edge? The angular margin is converted from px at
 * the point's own radius, so the test is a true distance, not a fixed angle.
 */
export function pointInWedge(
  g: RingGeometry,
  seatCount: number,
  theta: number,
  x: number,
  y: number,
  margin: number,
): boolean {
  const dx = x - g.cx;
  const dy = y - g.cy;
  const r = Math.hypot(dx, dy);
  if (r < g.inR + margin || r > g.playOut - margin) return false;
  let d = deg(Math.atan2(dy, dx)) - theta;
  while (d > 180) d -= 360;
  while (d < -180) d += 360;
  return Math.abs(d) <= halfWedge(seatCount) - GAP_DEG - deg(margin / Math.max(r, 1));
}
