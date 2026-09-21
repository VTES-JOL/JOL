import { MARKER_SIZE, MINI_CARD_H, MINI_CARD_W, miniCardCell } from './miniCardFootprint';
import { rad, seatAngles } from './ringGeometry';
import type { RingSeatModel } from './ringModel';

// Layout D: each seat is a rectangular panel placed round an ellipse (same seat
// order and orientation as the wedge ring: anchor at the bottom, prey left,
// predator right). Cards sit in a plain grid — no packer — and the panel grows
// with what it holds, so nothing ever overflows or shrinks. Pure and tested.

export const PANEL_HEAD = 26;
const PAD = 12;
const RIM_GAP = 6;
const MARGIN = 12;

export interface PanelCard {
  id: string;
  /** Panel-local centre of the card's grid cell. */
  x: number;
  y: number;
  rot: 0 | 90;
}
export interface PanelRim {
  kind: 'card' | 'marker';
  id: string;
  x: number;
  y: number;
}
export interface PanelBox {
  seatIndex: number;
  /** Top-left, in stage coordinates. */
  x: number;
  y: number;
  w: number;
  h: number;
  cols: number;
  /** Panel-local y where the rim strip (torpor / uncontrolled) starts. */
  rimTop: number;
  cards: PanelCard[];
  rim: PanelRim[];
}
export interface PanelLayout {
  /** Natural size of the whole board, px. */
  width: number;
  height: number;
  /** Centre (hub) in stage coordinates. */
  cx: number;
  cy: number;
  panels: PanelBox[];
}

/** Columns per panel: grows for very busy boards so panels stay roughly square. */
export function panelColumns(maxReady: number): number {
  return maxReady > 15 ? 6 : maxReady > 8 ? 5 : 4;
}

function layoutPanel(seat: RingSeatModel, cols: number, w: number): Omit<PanelBox, 'x' | 'y' | 'seatIndex'> {
  const cell = miniCardCell(1);
  const rows = Math.max(1, Math.ceil(seat.ready.length / cols));
  const cards: PanelCard[] = seat.ready.map((c, i) => ({
    id: c.id,
    x: PAD + (i % cols) * cell + cell / 2,
    y: PANEL_HEAD + Math.floor(i / cols) * cell + cell / 2,
    rot: c.locked ? 90 : 0,
  }));

  // Rim strip: torpor cards then uncontrolled markers, flowing left → right, wrapping.
  const items = [
    ...seat.torpor.map((c) => ({ kind: 'card' as const, id: c.id, w: MINI_CARD_W, h: MINI_CARD_H })),
    ...seat.uncontrolled.map((m) => ({ kind: 'marker' as const, id: m.id, w: MARKER_SIZE + 2, h: MARKER_SIZE + 2 })),
  ];
  const inner = w - 2 * PAD;
  const rimTop = PANEL_HEAD + rows * cell + 8;
  const rim: PanelRim[] = [];
  let x = 0, rowTop = 0, rowH = 0;
  items.forEach((it) => {
    if (x > 0 && x + it.w > inner) {
      rowTop += rowH + RIM_GAP;
      x = 0;
      rowH = 0;
    }
    rowH = Math.max(rowH, it.h);
    rim.push({ kind: it.kind, id: it.id, x: PAD + x + it.w / 2, y: rimTop + rowTop + it.h / 2 });
    x += it.w + RIM_GAP;
  });
  // Vertically centre the smaller items of each rim row on that row.
  const rimH = items.length ? rowTop + rowH : 0;
  const h = rimTop + rimH + (items.length ? PAD : 0) + (items.length ? 0 : -8 + PAD);
  return { w, h, cols, rimTop, cards, rim };
}

export function computePanelLayout(seats: RingSeatModel[], anchorIndex = 0): PanelLayout {
  const n = seats.length;
  const maxReady = Math.max(0, ...seats.map((s) => s.ready.length));
  const cols = panelColumns(maxReady);
  const w = cols * miniCardCell(1) + 2 * PAD;
  const boxes = seats.map((s) => layoutPanel(s, cols, w));
  const maxH = Math.max(0, ...boxes.map((b) => b.h));

  const Rx = 1.15 * w;
  const Ry = 1.1 * maxH;
  const thetas = seatAngles(n, anchorIndex);
  const centres = thetas.map((th) => ({ x: Rx * Math.cos(rad(th)), y: Ry * Math.sin(rad(th)) }));

  const minX = Math.min(...centres.map((c, i) => c.x - boxes[i].w / 2), -64);
  const maxX = Math.max(...centres.map((c, i) => c.x + boxes[i].w / 2), 64);
  const minY = Math.min(...centres.map((c, i) => c.y - boxes[i].h / 2), -64);
  const maxY = Math.max(...centres.map((c, i) => c.y + boxes[i].h / 2), 64);
  const ox = MARGIN - minX;
  const oy = MARGIN - minY;

  return {
    width: Math.ceil(maxX - minX + 2 * MARGIN),
    height: Math.ceil(maxY - minY + 2 * MARGIN),
    cx: ox,
    cy: oy,
    panels: seats.map((s, i) => ({
      seatIndex: s.seatIndex,
      x: centres[i].x - boxes[i].w / 2 + ox,
      y: centres[i].y - boxes[i].h / 2 + oy,
      ...boxes[i],
    })),
  };
}

