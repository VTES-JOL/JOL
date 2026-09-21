import { useMemo, useRef } from 'react';
import type { PlayerSnapshot } from '../../../api/types';
import { BoardViewSwitch } from './BoardViewSwitch';
import type { RingLayoutKind } from './boardPrefs';
import type { CardView } from './cardView';
import { SeatDetailView } from './detail/SeatDetailView';
import { TriadView } from './detail/TriadView';
import { SeatLegend } from './SeatLegend';
import { TableView } from './TableView';
import { buildRingModel, type MarkerView, type RingSeatModel } from './ringModel';
import type { BoardLevel } from './seatFocus';
import { useBoardView } from './useBoardView';
import { useContainerSize } from './useContainerSize';
import type { ViewState } from './viewportMath';

// The whole board for one viewer: a level switch (Table / Triad / Seat), the
// legend, and the level's view. Player, judge and spectator all get the same
// controls; a judge / spectator simply has no own seat, so focus starts on the
// edge holder. Fills its container.

export interface BoardViewProps {
  players: PlayerSnapshot[];
  seating: string[];
  viewerName?: string | null;
  gameId?: string | null;
  profileLayout?: RingLayoutKind | null;
  defaultLevel?: BoardLevel;
  hub?: string[];
  showLegend?: boolean;
  /** Judge view: list readable hand cards in the legend and seat headers. */
  showHands?: boolean;
  onCardClick?: (seat: RingSeatModel, card: CardView) => void;
  onMarkerClick?: (seat: RingSeatModel, marker: MarkerView) => void;
  onCounter?: (seat: RingSeatModel, card: CardView, step: number) => void;
}

export function BoardView({
  players,
  seating,
  viewerName = null,
  gameId,
  profileLayout,
  defaultLevel,
  hub,
  showLegend = true,
  showHands = false,
  onCardClick,
  onMarkerClick,
  onCounter,
}: BoardViewProps) {
  // Anchor the ring on the viewer / edge holder only — never on the focus, so
  // clicking a seat highlights it without spinning the whole ring under the cursor.
  const model = useMemo(() => buildRingModel(players, seating, { viewerName }), [players, seating, viewerName]);
  const { ref, w } = useContainerSize<HTMLDivElement>();
  const bv = useBoardView({ gameId, seats: model.seats, viewerName, defaultLevel, profileLayout, width: w });

  // Zoom / pan survives switching level or layout and coming back.
  const views = useRef<Record<string, ViewState | undefined>>({});
  const viewKey = bv.layout;

  const common = { viewerName, showHands, onCardClick, onMarkerClick, onCounter };
  // One click on a seat opens it (Triad, or Seat when there is no triad); the legend only re-focuses.
  const onSeatClick = (seat: RingSeatModel) => bv.open(seat.name);

  let body = null;
  if (bv.level === 'table') {
    body = (
      <TableView
        key={viewKey}
        layout={bv.layout}
        model={model}
        viewerName={viewerName}
        focusName={bv.focusName}
        hub={hub}
        showNames
        onSeatClick={onSeatClick}
        onCardClick={onCardClick}
        onMarkerClick={onMarkerClick}
        initialView={views.current[viewKey]}
        onViewChange={(v) => {
          views.current[viewKey] = v;
        }}
      />
    );
  } else if (bv.level === 'triad' && bv.focusSeat) {
    body = <TriadView focus={bv.focusSeat} neighbours={bv.neighbours} onFocus={bv.focus} {...common} />;
  } else if (bv.focusSeat) {
    body = <SeatDetailView seat={bv.focusSeat} neighbours={bv.neighbours} onStep={bv.step} {...common} />;
  }

  return (
    <div ref={ref} style={{ display: 'flex', flexDirection: 'column', gap: 8, width: '100%', height: '100%', minHeight: 0 }}>
      <BoardViewSwitch level={bv.level} levels={bv.levels} layout={bv.layout} onLevel={bv.setLevel} onLayout={bv.setLayout} />
      <div style={{ display: 'flex', gap: 12, flex: '1 1 0', minHeight: 0 }}>
        {showLegend && <SeatLegend seats={model.seats} viewerName={viewerName} showHands={showHands} onSeatClick={(s) => bv.focus(s.name)} />}
        <div style={{ flex: 1, minWidth: 0, minHeight: 0 }}>{body}</div>
      </div>
    </div>
  );
}
