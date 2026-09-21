import { useMemo } from 'react';
import type { CardView } from './cardView';
import { MiniCard, type MiniCardMeter } from './MiniCard';
import { RingHub } from './RingHub';
import { computePanelLayout, PANEL_HEAD, type PanelBox } from './panelLayout';
import type { MarkerView, RingModel, RingSeatModel } from './ringModel';
import { UncontrolledMarker } from './UncontrolledMarker';
import { ringGeometry } from './ringGeometry';

// Layout D: one rectangular panel per seat, placed round an ellipse in the same
// order and orientation as the wedge ring. Same props and interactions as
// RingBoard (a panel is a seat button; cards and markers are their own buttons),
// so the two are drop-in alternatives behind one viewport.

export interface SeatPanelsProps {
  model: RingModel;
  meter?: MiniCardMeter;
  hub?: string[];
  viewerName?: string | null;
  focusName?: string | null;
  onSeatClick?: (seat: RingSeatModel) => void;
  onCardClick?: (seat: RingSeatModel, card: CardView) => void;
  onMarkerClick?: (seat: RingSeatModel, marker: MarkerView) => void;
}

const anchor = (x: number, y: number) => ({ left: x, top: y, transform: 'translate(-50%, -50%)' }) as const;

function Panel({
  box,
  seat,
  self,
  focused,
  meter,
  onSeatClick,
  onCardClick,
  onMarkerClick,
}: { box: PanelBox; seat: RingSeatModel; self: boolean; focused: boolean } & Pick<SeatPanelsProps, 'meter' | 'onSeatClick' | 'onCardClick' | 'onMarkerClick'>) {
  const label = `${seat.name}${seat.ousted ? ', out of the game' : ''}: ${seat.ready.length} ready, ${seat.torpor.length} in torpor, ${seat.uncontrolled.length} uncontrolled`;
  const cards = new Map<string, CardView>([...seat.ready, ...seat.torpor].map((c) => [c.id, c]));
  const markers = new Map(seat.uncontrolled.map((m) => [m.id, m]));
  return (
    <div
      className={`absolute ${seat.ousted ? 'opacity-45' : ''}`}
      style={{ left: box.x, top: box.y, width: box.w, height: box.h }}
      data-seat={seat.name}
    >
      {/* the panel itself is the seat button; glyphs sit above it as siblings (no nested buttons) */}
      <button
        type="button"
        aria-label={label}
        title={label}
        disabled={!onSeatClick}
        onClick={onSeatClick ? () => onSeatClick(seat) : undefined}
        className="absolute inset-0 rounded-lg border p-0 disabled:cursor-default"
        style={{
          borderColor: focused ? 'var(--color-accent)' : self ? '#cf9f3f' : seat.color,
          borderWidth: focused ? 2.6 : self ? 1.8 : 1,
          background: `color-mix(in srgb, ${seat.color} ${seat.active ? 16 : 7}%, transparent)`,
          cursor: onSeatClick ? 'pointer' : undefined,
        }}
      />
      <div
        className="pointer-events-none absolute inset-x-0 top-0 flex items-center gap-1.5 px-3 text-[11px]"
        style={{ height: PANEL_HEAD }}
      >
        <i className="block h-2 w-2 rounded-full" style={{ background: seat.color }} />
        <b className="min-w-0 flex-1 truncate font-semibold" style={{ color: seat.color }}>
          {seat.name}
        </b>
        <span className="text-[9.5px] text-ink-muted">{seat.ousted ? seat.exitLabel : `${seat.ready.length} in play`}</span>
      </div>
      {box.rim.length > 0 && (
        <div
          className="pointer-events-none absolute inset-x-3 border-t border-dashed"
          style={{ top: box.rimTop - 4, borderColor: seat.color, opacity: 0.35 }}
        />
      )}
      {box.cards.map((p) => (
        <div key={p.id} className="absolute" style={anchor(p.x, p.y)}>
          <MiniCard view={cards.get(p.id)!} seatColor={seat.color} meter={meter} onClick={onCardClick && (() => onCardClick(seat, cards.get(p.id)!))} />
        </div>
      ))}
      {box.rim.map((p) => (
        <div key={`${p.kind}-${p.id}`} className="absolute" style={anchor(p.x, p.y)}>
          {p.kind === 'card' ? (
            <MiniCard view={cards.get(p.id)!} seatColor={seat.color} meter={meter} onClick={onCardClick && (() => onCardClick(seat, cards.get(p.id)!))} />
          ) : (
            <UncontrolledMarker marker={markers.get(p.id)!} seatColor={seat.color} onClick={onMarkerClick && (() => onMarkerClick(seat, markers.get(p.id)!))} />
          )}
        </div>
      ))}
    </div>
  );
}

export function SeatPanels({ model, meter = 'gauge', hub = [], viewerName = null, focusName = null, onSeatClick, onCardClick, onMarkerClick }: SeatPanelsProps) {
  const layout = useMemo(() => computePanelLayout(model.seats, model.anchorIndex), [model.seats, model.anchorIndex]);
  const hubGeometry = useMemo(() => ({ ...ringGeometry(820), cx: layout.cx, cy: layout.cy }), [layout.cx, layout.cy]);
  return (
    <div className="relative select-none" style={{ width: layout.width, height: layout.height }} data-testid="seat-panels">
      <RingHub geometry={hubGeometry} lines={hub} />
      {layout.panels.map((box, i) => (
        <Panel
          key={model.seats[i].name}
          box={box}
          seat={model.seats[i]}
          self={model.seats[i].name === viewerName}
          focused={model.seats[i].name === focusName}
          meter={meter}
          onSeatClick={onSeatClick}
          onCardClick={onCardClick}
          onMarkerClick={onMarkerClick}
        />
      ))}
    </div>
  );
}

