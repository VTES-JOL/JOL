import { useMemo } from 'react';
import { RingBoard, type RingBoardProps } from './RingBoard';
import { RingViewport } from './RingViewport';
import { SeatPanels } from './SeatPanels';
import { computePanelLayout } from './panelLayout';
import { clampSize } from './ringGeometry';
import type { RingLayoutKind } from './boardPrefs';
import { useContainerSize } from './useContainerSize';
import type { ViewState } from './viewportMath';

// The table overview: Wedge (C) or Panels (D) inside one zoom / pan viewport that
// fills its container. Both layouts share the viewport, props and interactions.
// The wedge ring is sized to the container's shorter side; the panel board has a
// natural size and is scaled down to fit (never up) so 100% always shows it all.

export interface TableViewProps extends Omit<RingBoardProps, 'size'> {
  layout?: RingLayoutKind;
  /** Px kept clear around the board at 100%. */
  gutter?: number;
  initialView?: ViewState;
  onViewChange?: (v: ViewState) => void;
}

export function TableView({ layout = 'wedge', gutter = 16, initialView, onViewChange, ...board }: TableViewProps) {
  const { ref, w, h } = useContainerSize<HTMLDivElement>();
  const panel = useMemo(
    () => (layout === 'panels' ? computePanelLayout(board.model.seats, board.model.anchorIndex) : null),
    [layout, board.model.seats, board.model.anchorIndex],
  );

  let content = null;
  if (w > 0 && h > 0) {
    if (panel) {
      const fit = Math.min(1, (w - gutter) / panel.width, (h - gutter) / panel.height);
      const cw = panel.width * fit;
      const ch = panel.height * fit;
      content = (
        <RingViewport width={w} height={h} contentW={cw} contentH={ch} initialView={initialView} onViewChange={onViewChange}>
          <div style={{ width: cw, height: ch }}>
            <div style={{ width: panel.width, height: panel.height, transform: `scale(${fit})`, transformOrigin: '0 0' }}>
              <SeatPanels {...board} />
            </div>
          </div>
        </RingViewport>
      );
    } else {
      const size = clampSize(Math.min(w, h) - gutter);
      content = (
        <RingViewport width={w} height={h} contentW={size} contentH={size} initialView={initialView} onViewChange={onViewChange}>
          <RingBoard {...board} size={size} />
        </RingViewport>
      );
    }
  }
  // Inline styles, not utility classes: the box must measure correctly even where
  // the Tailwind utilities layer is unavailable (Storybook's vitest run).
  return (
    <div ref={ref} style={{ width: '100%', height: '100%', minWidth: 0, minHeight: 0 }}>
      {content}
    </div>
  );
}
