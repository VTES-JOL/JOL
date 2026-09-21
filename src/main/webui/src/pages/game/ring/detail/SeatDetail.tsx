import type { ReactNode } from 'react';
import type { CardView } from '../cardView';
import type { MarkerView, RingSeatModel } from '../ringModel';
import { markerLabel } from '../ringModel';
import { UncontrolledMarker } from '../UncontrolledMarker';
import { DetailCard, type DetailSize } from './DetailCard';

// One seat, read properly: header (name, badges, pool / VP / hand), then the
// ready cards, torpor and uncontrolled sections. Used for each column of the
// Triad and for the single-seat level; only the card width differs.

export interface SeatDetailProps {
  seat: RingSeatModel;
  size: DetailSize;
  viewerName?: string | null;
  showHands?: boolean;
  /** Small label above the name: "Prey", "Predator", "Focus". */
  role?: string;
  headerExtra?: ReactNode;
  onCardClick?: (seat: RingSeatModel, view: CardView) => void;
  onCounter?: (seat: RingSeatModel, view: CardView, step: number) => void;
  onMarkerClick?: (seat: RingSeatModel, marker: MarkerView) => void;
}

const BADGE = 'rounded px-1 text-[8px] font-bold uppercase';
const CARD_MIN = { triad: 150, seat: 200 } as const;

function Section({ title, count, children }: { title: string; count: number; children: ReactNode }) {
  return (
    <section className="flex flex-col gap-1.5">
      <h4 className="m-0 text-[10px] font-bold uppercase tracking-wider text-ink-muted">
        {title} <span className="font-mono font-normal">{count}</span>
      </h4>
      {children}
    </section>
  );
}

export function SeatDetail({ seat, size, viewerName, showHands, role, headerExtra, onCardClick, onCounter, onMarkerClick }: SeatDetailProps) {
  const grid = { display: 'grid', gap: 6, gridTemplateColumns: `repeat(auto-fill, minmax(${CARD_MIN[size]}px, 1fr))` } as const;
  const self = seat.name === viewerName;
  return (
    <div className={`flex flex-col gap-3 text-ink ${seat.ousted ? 'opacity-70' : ''}`} data-seat-detail={seat.name}>
      <header className="flex flex-col gap-1 border-b border-line pb-2" style={{ borderBottom: `2px solid ${seat.color}` }}>
        {role && <span className="text-[10px] font-bold uppercase tracking-wider text-ink-muted">{role}</span>}
        <div className="flex flex-wrap items-center gap-1.5">
          <h3 className={`m-0 min-w-0 flex-1 truncate font-semibold ${size === 'seat' ? 'text-base' : 'text-sm'}`} style={{ color: seat.color }}>
            {seat.name}
          </h3>
          {seat.active && <span className={`${BADGE} bg-accent-soft text-accent`}>Turn</span>}
          {self && <span className={`${BADGE} bg-gold-soft text-gold`}>You</span>}
          {seat.edge && <span className={`${BADGE} border border-gold/40 bg-gold-soft text-gold`}>Edge</span>}
          {seat.ousted && <span className={`${BADGE} bg-hover text-ink-muted`}>{seat.exitLabel}</span>}
          {headerExtra}
        </div>
        <div className="flex gap-3 font-mono text-xs text-ink-secondary">
          <span>
            Pool <b className={seat.pool <= 8 ? 'text-blood-bright' : 'text-ink'}>{seat.pool}</b>
          </span>
          <span>
            VP <b className="text-ink">{seat.victoryPoints}</b>
          </span>
          <span>
            Hand <b className="text-ink">{seat.handCount}</b>
          </span>
        </div>
        {showHands && seat.handNames.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {seat.handNames.map((n, i) => (
              <span key={i} className="rounded border border-line bg-hover px-1 py-px text-[0.65rem] text-ink-secondary">
                {n}
              </span>
            ))}
          </div>
        )}
      </header>

      <Section title="Ready" count={seat.ready.length}>
        {seat.ready.length === 0 ? (
          <p className="m-0 text-xs text-ink-muted">Nothing in play.</p>
        ) : (
          <div style={grid}>
            {seat.ready.map((v) => (
              <DetailCard
                key={v.id}
                view={v}
                seatColor={seat.color}
                size={size}
                onClick={onCardClick && ((c) => onCardClick(seat, c))}
                onCounter={onCounter && ((c, step) => onCounter(seat, c, step))}
              />
            ))}
          </div>
        )}
      </Section>

      {seat.torpor.length > 0 && (
        <Section title="Torpor" count={seat.torpor.length}>
          <div style={grid}>
            {seat.torpor.map((v) => (
              <DetailCard key={v.id} view={v} seatColor={seat.color} size={size} onClick={onCardClick && ((c) => onCardClick(seat, c))} />
            ))}
          </div>
        </Section>
      )}

      {seat.uncontrolled.length > 0 && (
        <Section title="Uncontrolled" count={seat.uncontrolled.length}>
          <ul className="m-0 flex list-none flex-wrap gap-2 p-0">
            {seat.uncontrolled.map((m) => (
              <li key={m.id} className="flex items-center gap-1.5 text-xs text-ink-secondary">
                <UncontrolledMarker marker={m} seatColor={seat.color} onClick={onMarkerClick && ((mk) => onMarkerClick(seat, mk))} />
                <span>{m.hidden || !m.name ? 'Unknown' : m.name}</span>
                {m.capacity > 0 && <span className="sr-only">{markerLabel(m)}</span>}
              </li>
            ))}
          </ul>
        </Section>
      )}
    </div>
  );
}
