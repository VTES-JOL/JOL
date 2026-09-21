// Pip rows for counters with no maximum (an ally's life). An ally has no
// capacity, so for display its capacity IS its counters: exactly one pip per
// counter, all filled, equally sized and spanning the card's width like a
// vampire's row. Only when there are many are they split over several rows so
// each pip stays visible. Pure and unit-tested; MiniCard just draws the rows.

/** Most pips in one row before a second row starts. */
export const PIPS_PER_ROW = 10;
const ROW_HEIGHT = [8, 5, 3.5] as const;

export interface PipRows {
  /** Pips in each row, top to bottom; balanced so rows are near-equal. Empty for 0 counters. */
  rows: number[];
  /** Pip height, px — shrinks as rows are added. */
  height: number;
}

export function allyPipRows(counters: number): PipRows {
  const n = Math.max(0, Math.floor(counters));
  if (n === 0) return { rows: [], height: ROW_HEIGHT[0] };
  const rowCount = Math.ceil(n / PIPS_PER_ROW);
  const base = Math.floor(n / rowCount);
  const extra = n % rowCount;
  const rows = Array.from({ length: rowCount }, (_, i) => base + (i < extra ? 1 : 0));
  return { rows, height: ROW_HEIGHT[Math.min(rowCount, ROW_HEIGHT.length) - 1] };
}
