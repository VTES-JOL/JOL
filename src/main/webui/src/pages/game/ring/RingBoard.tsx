import { useMemo } from 'react';
import type { CardView } from './cardView';
import type { MiniCardMeter } from './MiniCard';
import { RingHub } from './RingHub';
import { RingSeat } from './RingSeat';
import { RingWedges } from './RingWedges';
import { computeRingLayout } from './ringLayout';
import type { MarkerView, RingModel, RingSeatModel } from './ringModel';

// Layout C: the wedge ring. Predator to the right, prey to the left, the anchor
// seat (the viewer, else the focused seat) at the bottom. Static at this stage —
// zoom / pan (RingViewport) and the Panels layout arrive in later phases; this
// component only draws one ring at `size` px square.

export interface RingBoardProps {
  model: RingModel;
  /** Ring diameter in px; clamped to 560–1000. */
  size?: number;
  meter?: MiniCardMeter;
  /** Centre text, top line first (e.g. "Turn 9", "Minion Phase", "Predator right · Prey left"). */
  hub?: string[];
  /** Outline this seat in gold as the viewer's own. Omit for judges / spectators. */
  viewerName?: string | null;
  /** Seat to outline as focused (Triad / Seat levels show this seat). */
  focusName?: string | null;
  showNames?: boolean;
  onSeatClick?: (seat: RingSeatModel) => void;
  onCardClick?: (seat: RingSeatModel, card: CardView) => void;
  onMarkerClick?: (seat: RingSeatModel, marker: MarkerView) => void;
}

export function RingBoard({
  model,
  size = 820,
  meter = 'gauge',
  hub = [],
  viewerName = null,
  focusName = null,
  showNames = false,
  onSeatClick,
  onCardClick,
  onMarkerClick,
}: RingBoardProps) {
  // The packer is the expensive part: it only reruns when the seats, size or
  // anchor change — never for pan / zoom, which only transform this element.
  const layout = useMemo(() => computeRingLayout(model.seats, size, model.anchorIndex), [model.seats, size, model.anchorIndex]);
  const g = layout.geometry;
  const viewerIndex = model.seats.findIndex((s) => s.name === viewerName);

  return (
    <div className="relative select-none" style={{ width: g.size, height: g.size }} data-testid="ring-board">
      <RingWedges
        geometry={g}
        layout={layout}
        seats={model.seats}
        viewerIndex={viewerIndex}
        focusIndex={model.seats.findIndex((s) => s.name === focusName)}
        showNames={showNames}
        onSeatClick={onSeatClick}
      />
      <RingHub geometry={g} lines={hub} />
      {model.seats.map((seat, i) => (
        <RingSeat
          key={seat.name}
          geometry={g}
          seat={seat}
          layout={layout.seats[i]}
          scale={layout.scale}
          meter={meter}
          onCardClick={onCardClick}
          onMarkerClick={onMarkerClick}
        />
      ))}
    </div>
  );
}
