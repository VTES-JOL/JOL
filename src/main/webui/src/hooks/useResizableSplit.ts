import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * A vertical (top/bottom) resizable split. Returns the top pane's size as a
 * percentage plus the props for a drag handle sitting between the two panes.
 *
 * The value is clamped to [min, max] and persisted under `storageKey`
 * (per-game on the table — `jol-split:<gameId>`), matching the client-owned
 * GameView model: nothing here round-trips to the server. A private window or
 * cleared storage just falls back to `initial`.
 *
 * Usage:
 *   const { topPercent, dividerProps, containerRef } = useResizableSplit(key);
 *   <div ref={containerRef} class="flex flex-col">
 *     <div style={{ flexBasis: `${topPercent}%` }} class="min-h-0" />
 *     <div {...dividerProps} />
 *     <div class="flex-1 min-h-0" />
 */
export function useResizableSplit(
  storageKey: string,
  { initial = 50, min = 25, max = 75 }: { initial?: number; min?: number; max?: number } = {},
) {
  const clamp = useCallback((v: number) => Math.min(max, Math.max(min, v)), [min, max]);

  const [topPercent, setTopPercent] = useState<number>(() => {
    try {
      const raw = localStorage.getItem(storageKey);
      const n = raw == null ? NaN : Number(raw);
      return Number.isFinite(n) ? clamp(n) : initial;
    } catch {
      return initial;
    }
  });

  // Persist (best-effort). Kept out of the drag path — only the settled value.
  useEffect(() => {
    try {
      localStorage.setItem(storageKey, String(Math.round(topPercent)));
    } catch {
      /* private window / storage disabled — the split just won't persist */
    }
  }, [storageKey, topPercent]);

  const containerRef = useRef<HTMLDivElement>(null);
  const dragging = useRef(false);

  const onPointerMove = useCallback(
    (e: PointerEvent) => {
      if (!dragging.current) return;
      const el = containerRef.current;
      if (!el) return;
      const rect = el.getBoundingClientRect();
      if (rect.height === 0) return;
      setTopPercent(clamp(((e.clientY - rect.top) / rect.height) * 100));
    },
    [clamp],
  );

  // Named function expression so the pointerup cleanup can reference itself
  // without capturing the still-initialising outer const.
  const endDrag = useCallback(function endDrag() {
    dragging.current = false;
    document.body.style.removeProperty('cursor');
    document.body.style.removeProperty('user-select');
    window.removeEventListener('pointermove', onPointerMove);
    window.removeEventListener('pointerup', endDrag);
  }, [onPointerMove]);

  useEffect(() => endDrag, [endDrag]);

  const onPointerDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      dragging.current = true;
      document.body.style.cursor = 'row-resize';
      document.body.style.userSelect = 'none';
      window.addEventListener('pointermove', onPointerMove);
      window.addEventListener('pointerup', endDrag);
    },
    [onPointerMove, endDrag],
  );

  const onKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === 'ArrowUp') setTopPercent((p) => clamp(p - 2));
      else if (e.key === 'ArrowDown') setTopPercent((p) => clamp(p + 2));
      else if (e.key === 'Home') setTopPercent(clamp(initial));
      else return;
      e.preventDefault();
    },
    [clamp, initial],
  );

  const reset = useCallback(() => setTopPercent(clamp(initial)), [clamp, initial]);

  return {
    topPercent,
    reset,
    containerRef,
    dividerProps: {
      role: 'separator' as const,
      'aria-orientation': 'horizontal' as const,
      'aria-valuenow': Math.round(topPercent),
      'aria-valuemin': min,
      'aria-valuemax': max,
      tabIndex: 0,
      onPointerDown,
      onKeyDown,
      onDoubleClick: reset,
    },
  };
}
