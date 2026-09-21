// Pip grid for counters with no maximum (an ally's life). It always shows at
// least `BASE_SLOTS` slots; beyond that the slot count follows the counters and
// the pips just get smaller, adding rows when one row would be too tight.
// Pure and unit-tested; MiniCard only draws what this returns.

export const BASE_SLOTS = 5;

export interface PipGrid {
  /** Slots drawn (filled + empty). */
  slots: number;
  cols: number;
  rows: number;
  /** Pip side, px. */
  size: number;
}

/**
 * Choose rows / columns so pips are as large as possible inside `width` × `height`
 * (px, gap included), capped at `maxSize`.
 */
export function pipGrid(counters: number, width = 37, height = 12, gap = 1, maxSize = 7): PipGrid {
  const slots = Math.max(BASE_SLOTS, Math.max(0, counters));
  let best: PipGrid = { slots, cols: slots, rows: 1, size: 1 };
  for (let rows = 1; rows <= slots; rows++) {
    const cols = Math.ceil(slots / rows);
    const size = Math.min(maxSize, (width - gap * (cols - 1)) / cols, (height - gap * (rows - 1)) / rows);
    if (size > best.size) best = { slots, cols, rows, size };
  }
  return { ...best, size: Math.max(1, Math.floor(best.size * 10) / 10) };
}
