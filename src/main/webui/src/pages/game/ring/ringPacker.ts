import { miniCardCell } from './miniCardFootprint';
import { deg, pointInWedge, polar, type Point, type RingGeometry } from './ringGeometry';

// Fits square lattice cells fully inside a seat's wedge. Pure, deterministic and
// the expensive part of the ring — callers memoise on (seats, size, anchor).

/** Global glyph scales tried, largest first. One scale for every seat so cards stay comparable. */
export const PACK_SCALES = [1, 0.94, 0.88, 0.82, 0.76, 0.7, 0.64, 0.58, 0.52] as const;
/** Clearance kept between a cell and the wedge edges, px. */
const EDGE_MARGIN = 2;
/** Lattice search extent (cells either side of the anchor) and sub-cell offsets tried. */
const SPAN = 10;
const OFFSETS = 3;

export interface FitResult {
  /** Up to `count` cell centres, nearest the wedge's mid-line first. */
  cells: Point[];
  /** True when every requested cell fit. */
  complete: boolean;
}

function cellInside(g: RingGeometry, seatCount: number, theta: number, cx: number, cy: number, cell: number): boolean {
  const h = cell / 2;
  // Corners, edge midpoints — the same eight probes for every lattice cell.
  const probes: [number, number][] = [[-h, -h], [h, -h], [h, h], [-h, h], [0, -h], [h, 0], [0, h], [-h, 0]];
  return probes.every(([dx, dy]) => pointInWedge(g, seatCount, theta, cx + dx, cy + dy, EDGE_MARGIN));
}

export function fitCells(
  g: RingGeometry,
  seatCount: number,
  theta: number,
  count: number,
  scale: number,
): FitResult {
  if (count <= 0) return { cells: [], complete: true };
  const cell = miniCardCell(scale);
  const anchor = polar(g, theta, (g.inR + g.playOut) / 2);

  let best: { cells: Point[]; score: number } | null = null;
  for (let a = 0; a < OFFSETS; a++) {
    for (let b = 0; b < OFFSETS; b++) {
      const ox = (a * cell) / OFFSETS;
      const oy = (b * cell) / OFFSETS;
      const found: (Point & { d: number })[] = [];
      for (let i = -SPAN; i <= SPAN; i++) {
        for (let j = -SPAN; j <= SPAN; j++) {
          const x = anchor.x + ox + i * cell;
          const y = anchor.y + oy + j * cell;
          if (cellInside(g, seatCount, theta, x, y, cell)) {
            found.push({ x, y, d: Math.hypot(x - anchor.x, y - anchor.y) });
          }
        }
      }
      found.sort((p, q) => p.d - q.d);
      const pick = found.slice(0, count);
      const score = pick.reduce((t, c) => t + c.d, 0);
      // Prefer more cells, then the tightest cluster around the mid-line.
      if (!best || pick.length > best.cells.length || (pick.length === best.cells.length && score < best.score)) {
        best = { cells: pick.map(({ x, y }) => ({ x, y })), score };
      }
    }
  }
  const cells = best?.cells ?? [];
  return { cells, complete: cells.length >= count };
}

/** Order cells for assignment: inner radial band first, then by angle within a band. */
export function orderCells(g: RingGeometry, cells: Point[], scale: number): Point[] {
  const cell = miniCardCell(scale);
  const band = (p: Point) => Math.round(Math.hypot(p.x - g.cx, p.y - g.cy) / (cell * 0.8));
  const ang = (p: Point) => deg(Math.atan2(p.y - g.cy, p.x - g.cx));
  return cells.slice().sort((p, q) => band(p) - band(q) || ang(p) - ang(q));
}

export interface PackedSeat {
  cells: Point[];
  /** Cards that did not fit even at the smallest scale (renderer shows "+N"). */
  overflow: number;
}

export interface PackResult {
  scale: number;
  seats: PackedSeat[];
}

/** Largest global scale at which every seat fits; else the smallest, with overflow reported. */
export function packSeats(g: RingGeometry, thetas: number[], counts: number[]): PackResult {
  const n = thetas.length;
  for (const scale of PACK_SCALES) {
    const trial = thetas.map((th, i) => fitCells(g, n, th, counts[i], scale));
    if (trial.every((t) => t.complete)) {
      return {
        scale,
        seats: trial.map((t) => ({ cells: orderCells(g, t.cells, scale), overflow: 0 })),
      };
    }
  }
  const scale = PACK_SCALES[PACK_SCALES.length - 1];
  return {
    scale,
    seats: thetas.map((th, i) => {
      const t = fitCells(g, n, th, counts[i], scale);
      return { cells: orderCells(g, t.cells, scale), overflow: Math.max(0, counts[i] - t.cells.length) };
    }),
  };
}
