import type { ReactNode } from 'react';
import { Minus, Plus } from 'lucide-react';
import { baseOffset, type ViewState } from './viewportMath';
import { useRingViewport } from './useRingViewport';

// A clipped viewport around the ring: wheel zooms toward the cursor, right-drag
// pans, +/− / Reset in the corner. Layout and pan / zoom are independent — the
// ring is packed once (RingBoard memoises) and only a CSS transform changes here.

export interface RingViewportProps {
  width: number;
  height: number;
  /** Size of the content (the ring diameter). */
  contentW: number;
  contentH: number;
  children: ReactNode;
  /** Let a parent keep zoom / pan across remounts (switching layout or level). */
  /** Extra controls shown under the zoom controls (e.g. the card-meter switch). */
  controls?: ReactNode;
  initialView?: ViewState;
  onViewChange?: (v: ViewState) => void;
}

export function RingViewport({ width, height, contentW, contentH, children, controls, initialView, onViewChange }: RingViewportProps) {
  const box = { viewW: width, viewH: height, contentW, contentH };
  const vp = useRingViewport(box, { initial: initialView, onChange: onViewChange });
  const { bx, by } = baseOffset(box);
  const { z, px, py } = vp.view;
  const btn =
    'inline-flex h-6 min-w-6 items-center justify-center rounded border border-line-accent bg-panel px-1.5 text-xs text-ink hover:bg-hover disabled:opacity-40';

  return (
    <div
      ref={vp.elRef}
      role="region"
      aria-label="Table ring. Scroll or press plus and minus to zoom, right-drag or arrow keys to pan, 0 to reset."
      tabIndex={0}
      data-testid="ring-viewport"
      className={`relative select-none overflow-hidden rounded-md border border-line bg-base focus-visible:outline-2 focus-visible:outline-accent ${vp.panning ? 'cursor-grabbing' : ''}`}
      style={{ width, height }}
      {...vp.handlers}
    >
      <div className="absolute left-0 top-0 origin-top-left" style={{ transform: `translate(${px}px, ${py}px) scale(${z})` }} data-testid="ring-world">
        <div className="absolute" style={{ left: bx, top: by }}>
          {children}
        </div>
      </div>

      <div className="absolute right-2 top-2 flex flex-col items-end gap-1" onPointerDown={(e) => e.stopPropagation()}>
        <div className="flex items-center gap-1">
        <button type="button" className={btn} aria-label="Zoom out" onClick={vp.zoomOut} disabled={vp.atMin}>
          <Minus size={12} />
        </button>
        <span className="w-10 text-center font-mono text-[11px] text-ink-secondary" aria-live="polite" data-testid="ring-zoom">
          {Math.round(z * 100)}%
        </span>
        <button type="button" className={btn} aria-label="Zoom in" onClick={vp.zoomIn} disabled={vp.atMax}>
          <Plus size={12} />
        </button>
        <button type="button" className={btn} onClick={vp.reset} disabled={vp.isHome}>
          Reset
        </button>
        </div>
        {controls}
      </div>
      <span className="pointer-events-none absolute bottom-2 left-2 text-[10px] text-ink-muted">Scroll to zoom · right-drag to pan</span>
    </div>
  );
}
