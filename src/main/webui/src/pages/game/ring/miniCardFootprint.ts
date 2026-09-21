// The ONLY layout contract between the overview glyphs (MiniCard,
// UncontrolledMarker) and the ring's packer. A glyph may change anything inside
// its box freely; if its box size, rotated size or overhang changes, change the
// numbers here and the layout follows — nothing in RingBoard/RingSeat needs to know.

export interface Size {
  w: number;
  h: number;
}

/** Design-size (scale 1) card face. */
export const MINI_CARD_W = 46;
export const MINI_CARD_H = 64;
/** Design-size uncontrolled marker diameter. */
export const MARKER_SIZE = 30;
/** Clear space the packer leaves around every glyph (covers the 2px contested halo). */
export const GLYPH_GAP = 5;

/** Bounding box a card occupies on screen: a locked card is turned 90°. */
export function miniCardFootprint(locked: boolean, scale = 1): Size {
  return locked
    ? { w: MINI_CARD_H * scale, h: MINI_CARD_W * scale }
    : { w: MINI_CARD_W * scale, h: MINI_CARD_H * scale };
}

export function markerFootprint(scale = 1): Size {
  return { w: MARKER_SIZE * scale, h: MARKER_SIZE * scale };
}

/**
 * Side of the square lattice cell one card needs, whichever way it is turned —
 * lets locking a card never move its neighbours.
 */
export function miniCardCell(scale = 1): number {
  return Math.max(MINI_CARD_W, MINI_CARD_H) * scale + GLYPH_GAP;
}
