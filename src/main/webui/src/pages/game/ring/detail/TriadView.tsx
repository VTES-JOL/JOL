import type { RingSeatModel } from '../ringModel';
import type { Neighbours } from '../seatFocus';
import { SeatDetail, type SeatDetailProps } from './SeatDetail';

// The focused seat centre-stage with its prey on the left and its predator on the
// right (the same direction as the ring). Each column scrolls on its own; clicking
// a neighbour's header re-centres the triad on it.

export interface TriadViewProps extends Pick<SeatDetailProps, 'viewerName' | 'showHands' | 'onCardClick' | 'onCounter' | 'onMarkerClick'> {
  focus: RingSeatModel;
  neighbours: Neighbours;
  onFocus: (name: string) => void;
}

function Column({ seat, role, main, onFocus, ...rest }: { seat: RingSeatModel | null; role: string; main?: boolean; onFocus: (n: string) => void } & Omit<SeatDetailProps, 'seat' | 'size' | 'role'>) {
  if (!seat) return <div className="rounded-md border border-dashed border-line p-3 text-xs text-ink-muted">No {role.toLowerCase()}</div>;
  return (
    <div
      className={`min-h-0 overflow-y-auto rounded-md border bg-panel p-3 ${main ? 'border-accent' : 'border-line'}`}
      data-testid={`triad-${role.toLowerCase()}`}
    >
      <SeatDetail
        seat={seat}
        size="triad"
        role={main ? 'Focus' : role}
        headerExtra={
          main ? null : (
            <button
              type="button"
              className="rounded border border-line-accent px-1.5 py-0.5 text-[10px] text-ink-secondary hover:bg-hover"
              aria-label={`Focus ${seat.name}`}
              onClick={() => onFocus(seat.name)}
            >
              Focus
            </button>
          )
        }
        {...rest}
      />
    </div>
  );
}

export function TriadView({ focus, neighbours, onFocus, ...rest }: TriadViewProps) {
  return (
    <div
      className="grid h-full min-h-0 gap-3"
      style={{ display: 'grid', gridTemplateColumns: 'minmax(0,1fr) minmax(0,1.4fr) minmax(0,1fr)', height: '100%', gap: 12 }}
    >
      <Column seat={neighbours.prey} role="Prey" onFocus={onFocus} {...rest} />
      <Column seat={focus} role="Focus" main onFocus={onFocus} {...rest} />
      <Column seat={neighbours.predator} role="Predator" onFocus={onFocus} {...rest} />
    </div>
  );
}
