import { memo } from 'react';
import type { CardView } from './cardView';
import { MiniCard, type MiniCardMeter } from './MiniCard';
import type { RingGeometry } from './ringGeometry';
import { polar } from './ringGeometry';
import type { SeatLayout } from './ringLayout';
import type { MarkerView, RingSeatModel } from './ringModel';
import { UncontrolledMarker } from './UncontrolledMarker';

// One seat's glyphs: ready cards inside the wedge, torpor cards and uncontrolled
// markers on the rim, and a "+N" chip if the packer ran out of room. Placement is
// entirely from the pure layout; this component only turns numbers into elements.

export interface RingSeatProps {
  geometry: RingGeometry;
  seat: RingSeatModel;
  layout: SeatLayout;
  scale: number;
  meter: MiniCardMeter;
  onCardClick?: (seat: RingSeatModel, card: CardView) => void;
  onMarkerClick?: (seat: RingSeatModel, marker: MarkerView) => void;
}

const anchor = (x: number, y: number) => ({ left: x, top: y, transform: 'translate(-50%, -50%)' }) as const;

export const RingSeat = memo(function RingSeat({
  geometry,
  seat,
  layout,
  scale,
  meter,
  onCardClick,
  onMarkerClick,
}: RingSeatProps) {
  const cards = new Map<string, CardView>([...seat.ready, ...seat.torpor].map((c) => [c.id, c]));
  const markers = new Map(seat.uncontrolled.map((m) => [m.id, m]));
  const dim = seat.ousted ? 'opacity-40' : '';
  const chip = polar(geometry, layout.theta, geometry.playOut - 12 * geometry.k);

  return (
    <div className={`pointer-events-none absolute inset-0 ${dim}`} data-seat={seat.name}>
      {layout.ready.map((p) => {
        const view = cards.get(p.id)!;
        return (
          <div key={p.id} className="pointer-events-auto absolute" style={anchor(p.x, p.y)}>
            <MiniCard view={view} seatColor={seat.color} meter={meter} scale={scale} onClick={onCardClick && (() => onCardClick(seat, view))} />
          </div>
        );
      })}
      {layout.rim.map((p) => {
        const inner =
          p.kind === 'card' ? (
            <MiniCard
              view={cards.get(p.id)!}
              seatColor={seat.color}
              meter={meter}
              scale={scale}
              onClick={onCardClick && (() => onCardClick(seat, cards.get(p.id)!))}
            />
          ) : (
            <UncontrolledMarker
              marker={markers.get(p.id)!}
              seatColor={seat.color}
              scale={scale}
              onClick={onMarkerClick && (() => onMarkerClick(seat, markers.get(p.id)!))}
            />
          );
        return (
          <div key={`${p.kind}-${p.id}`} className="pointer-events-auto absolute" style={anchor(p.x, p.y)}>
            {inner}
          </div>
        );
      })}
      {layout.overflow > 0 && (
        <span
          className="absolute rounded-full bg-blood px-1.5 py-0.5 text-[9px] font-bold text-white"
          style={anchor(chip.x, chip.y)}
          title={`${layout.overflow} more ready cards not shown`}
        >
          +{layout.overflow}
        </span>
      )}
    </div>
  );
});
