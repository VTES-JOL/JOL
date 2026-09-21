import { ChevronLeft, ChevronRight } from 'lucide-react';
import type { RingSeatModel } from '../ringModel';
import type { Neighbours } from '../seatFocus';
import { SeatDetail, type SeatDetailProps } from './SeatDetail';

// One seat at full size, with prev / next to walk round the table: prey on the
// left, predator on the right, matching the ring's direction.

export interface SeatDetailViewProps extends Pick<SeatDetailProps, 'viewerName' | 'showHands' | 'onCardClick' | 'onCounter' | 'onMarkerClick'> {
  seat: RingSeatModel;
  neighbours: Neighbours;
  onStep: (dir: 'prey' | 'predator') => void;
}

const NAV = 'inline-flex items-center gap-1 rounded-md border border-line-accent bg-panel px-2.5 py-1 text-xs text-ink-secondary hover:bg-hover disabled:opacity-40';

export function SeatDetailView({ seat, neighbours, onStep, ...rest }: SeatDetailViewProps) {
  return (
    <div className="flex h-full min-h-0 flex-col gap-3" style={{ display: 'flex', flexDirection: 'column', height: '100%', gap: 12 }}>
      <nav className="flex items-center justify-between gap-2" aria-label="Walk round the table">
        <button type="button" className={NAV} disabled={!neighbours.prey} onClick={() => onStep('prey')}>
          <ChevronLeft size={14} /> Prey{neighbours.prey ? `: ${neighbours.prey.name}` : ''}
        </button>
        <button type="button" className={NAV} disabled={!neighbours.predator} onClick={() => onStep('predator')}>
          Predator{neighbours.predator ? `: ${neighbours.predator.name}` : ''} <ChevronRight size={14} />
        </button>
      </nav>
      <div className="min-h-0 flex-1 overflow-y-auto rounded-md border border-line bg-panel p-4" data-testid="seat-detail">
        <SeatDetail seat={seat} size="seat" {...rest} />
      </div>
    </div>
  );
}
