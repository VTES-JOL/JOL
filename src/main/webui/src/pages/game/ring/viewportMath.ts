// Pure zoom / pan maths for the ring viewport. Content is centred in the view at
// zoom 1; the world is drawn as translate(px, py) scale(z) from the view's
// top-left. Everything here is unit-tested; the hook only wires events to it.

export interface ViewState {
  z: number;
  px: number;
  py: number;
}

export interface ViewBox {
  viewW: number;
  viewH: number;
  contentW: number;
  contentH: number;
}

export const MIN_ZOOM = 0.6;
export const MAX_ZOOM = 3.2;
/** At least this many px of the content must stay on screen while panning. */
export const PAN_MARGIN = 80;
export const HOME: ViewState = { z: 1, px: 0, py: 0 };

export const clampZoom = (z: number): number => Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, z));

/** Offset that centres the content in the view at zoom 1. */
export const baseOffset = (b: ViewBox) => ({ bx: (b.viewW - b.contentW) / 2, by: (b.viewH - b.contentH) / 2 });

export function clampPan(v: ViewState, b: ViewBox): ViewState {
  const { bx, by } = baseOffset(b);
  const range = (base: number, content: number, view: number, p: number) => {
    const lo = PAN_MARGIN - v.z * (base + content);
    const hi = view - PAN_MARGIN - v.z * base;
    return Math.min(Math.max(p, Math.min(lo, hi)), Math.max(lo, hi));
  };
  return {
    z: v.z,
    px: range(bx, b.contentW, b.viewW, v.px),
    py: range(by, b.contentH, b.viewH, v.py),
  };
}

/** Zoom by `factor` keeping the view point (cx, cy) fixed on screen. */
export function zoomAt(v: ViewState, b: ViewBox, cx: number, cy: number, factor: number): ViewState {
  const z = clampZoom(v.z * factor);
  const ratio = z / v.z;
  return clampPan({ z, px: cx - (cx - v.px) * ratio, py: cy - (cy - v.py) * ratio }, b);
}

export const panBy = (v: ViewState, b: ViewBox, dx: number, dy: number): ViewState =>
  clampPan({ ...v, px: v.px + dx, py: v.py + dy }, b);

/** Wheel delta → zoom factor. Lines / pages are normalised to pixels first. */
export function wheelFactor(deltaY: number, deltaMode: number): number {
  const px = deltaMode === 1 ? deltaY * 16 : deltaMode === 2 ? deltaY * 400 : deltaY;
  return Math.exp(-px * 0.0018);
}
