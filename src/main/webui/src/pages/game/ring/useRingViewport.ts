import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState, type PointerEvent, type KeyboardEvent } from 'react';
import { HOME, MAX_ZOOM, MIN_ZOOM, clampPan, panBy, wheelFactor, zoomAt, type ViewBox, type ViewState } from './viewportMath';

// Zoom / pan behaviour for a clipped viewport: wheel (and trackpad pinch) zooms
// about the cursor, RIGHT-button drag pans, +/-/0/arrow keys when focused, and
// button helpers for the toolbar. The wheel listener is attached natively and
// non-passive — React's onWheel is passive, so preventDefault would be ignored
// and the page would scroll behind the ring.

const STEP = 1.25;
const KEY_PAN = 60;

export function useRingViewport(box: ViewBox, held?: { initial?: ViewState; onChange?: (v: ViewState) => void }) {
  const [raw, setRaw] = useState<ViewState>(held?.initial ?? HOME);
  const onChange = held?.onChange;
  useEffect(() => {
    onChange?.(raw);
  }, [raw, onChange]);
  const [panning, setPanning] = useState(false);
  const elRef = useRef<HTMLDivElement>(null);
  const boxRef = useRef(box);
  useLayoutEffect(() => {
    boxRef.current = box;
  });
  const drag = useRef<{ x: number; y: number } | null>(null);

  // Re-clamp on render so a resize / new ring size can never strand the content.
  const view = useMemo(() => clampPan(raw, box), [raw, box.viewW, box.viewH, box.contentW, box.contentH]); // eslint-disable-line react-hooks/exhaustive-deps

  const centre = () => ({ cx: boxRef.current.viewW / 2, cy: boxRef.current.viewH / 2 });
  const zoomBy = useCallback((factor: number, cx?: number, cy?: number) => {
    const c = centre();
    setRaw((v) => zoomAt(v, boxRef.current, cx ?? c.cx, cy ?? c.cy, factor));
  }, []);
  const reset = useCallback(() => setRaw(HOME), []);

  useEffect(() => {
    const el = elRef.current;
    if (!el) return;
    const onWheel = (e: WheelEvent) => {
      e.preventDefault();
      const r = el.getBoundingClientRect();
      const f = wheelFactor(e.deltaY, e.deltaMode);
      setRaw((v) => zoomAt(v, boxRef.current, e.clientX - r.left, e.clientY - r.top, f));
    };
    el.addEventListener('wheel', onWheel, { passive: false });
    return () => el.removeEventListener('wheel', onWheel);
  }, []);

  const onPointerDown = (e: PointerEvent<HTMLDivElement>) => {
    if (e.button !== 2) return;
    e.preventDefault();
    drag.current = { x: e.clientX, y: e.clientY };
    e.currentTarget.setPointerCapture?.(e.pointerId);
    setPanning(true);
  };
  const onPointerMove = (e: PointerEvent<HTMLDivElement>) => {
    const d = drag.current;
    if (!d) return;
    if (!(e.buttons & 2)) {
      drag.current = null;
      setPanning(false);
      return;
    }
    const dx = e.clientX - d.x;
    const dy = e.clientY - d.y;
    drag.current = { x: e.clientX, y: e.clientY };
    setRaw((v) => panBy(v, boxRef.current, dx, dy));
  };
  const endDrag = (e: PointerEvent<HTMLDivElement>) => {
    if (!drag.current) return;
    drag.current = null;
    e.currentTarget.releasePointerCapture?.(e.pointerId);
    setPanning(false);
  };
  const onKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.target !== e.currentTarget) return; // don't steal keys from a focused wedge / card
    const pan = (dx: number, dy: number) => setRaw((v) => panBy(v, boxRef.current, dx, dy));
    switch (e.key) {
      case '+': case '=': zoomBy(STEP); break;
      case '-': case '_': zoomBy(1 / STEP); break;
      case '0': reset(); break;
      case 'ArrowLeft': pan(KEY_PAN, 0); break;
      case 'ArrowRight': pan(-KEY_PAN, 0); break;
      case 'ArrowUp': pan(0, KEY_PAN); break;
      case 'ArrowDown': pan(0, -KEY_PAN); break;
      default: return;
    }
    e.preventDefault();
  };

  return {
    view,
    panning,
    atMin: view.z <= MIN_ZOOM + 1e-6,
    atMax: view.z >= MAX_ZOOM - 1e-6,
    isHome: view.z === 1 && view.px === 0 && view.py === 0,
    zoomIn: () => zoomBy(STEP),
    zoomOut: () => zoomBy(1 / STEP),
    reset,
    elRef,
    handlers: { onPointerDown, onPointerMove, onPointerUp: endDrag, onPointerCancel: endDrag, onKeyDown, onContextMenu: (e: { preventDefault(): void }) => e.preventDefault() },
  };
}
