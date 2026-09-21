import type { RingSeatModel } from './ringModel';

// Compact standings table for the corner of the board: one row per seat with
// colour, name, badges and pool / VP / hand. Sized by its content (never full
// height). A click on a row is the same action as clicking that seat's wedge.
// `showHands` lists the hand cards this viewer can read (judge view).

export interface SeatLegendProps {
  seats: RingSeatModel[];
  viewerName?: string | null;
  showHands?: boolean;
  /** Pool at or below this reads as danger. */
  lowPool?: number;
  onSeatClick?: (seat: RingSeatModel) => void;
}

const BADGE = 'rounded px-1 text-[8px] font-bold uppercase';

export function SeatLegend({ seats, viewerName = null, showHands = false, lowPool = 8, onSeatClick }: SeatLegendProps) {
  return (
    <section
      aria-label="Seats"
      className="w-[210px] self-start rounded-[10px] border border-line bg-panel text-ink"
      style={{ width: 210, alignSelf: 'flex-start' }}
    >
      <h3 className="border-b border-line px-3.5 py-2.5 text-[11px] font-bold uppercase tracking-[.08em] text-ink-muted">Seats</h3>
      <ul className="m-0 flex list-none flex-col gap-2 p-2.5">
        {seats.map((s) => {
          const self = s.name === viewerName;
          const body = (
            <>
              <span className="flex flex-wrap items-center gap-1.5">
                <span className="min-w-0 flex-1 truncate text-xs font-semibold">{s.name}</span>
                {s.active && <span className={`${BADGE} bg-accent-soft text-accent`}>Turn</span>}
                {self && <span className={`${BADGE} bg-gold-soft text-gold`}>You</span>}
                {s.edge && <span className={`${BADGE} border border-gold/40 bg-gold-soft text-gold`}>Edge</span>}
                {s.ousted && <span className={`${BADGE} bg-hover text-ink-muted`}>{s.exitLabel}</span>}
              </span>
              <span className="flex gap-3 font-mono text-[10.5px] text-ink-secondary">
                <span>
                  Pool <b className={s.pool <= lowPool ? 'font-semibold text-blood-bright' : 'font-semibold text-ink'}>{s.pool}</b>
                </span>
                <span>
                  VP <b className="font-semibold text-ink">{s.victoryPoints}</b>
                </span>
                <span>
                  Hand <b className="font-semibold text-ink">{s.handCount}</b>
                </span>
              </span>
              {showHands && s.handNames.length > 0 && (
                <span className="flex flex-wrap gap-[3px]">
                  {s.handNames.map((n, i) => (
                    <span key={i} className="rounded border border-line bg-hover px-1 py-px text-[8.5px] text-ink-secondary">
                      {n}
                    </span>
                  ))}
                </span>
              )}
            </>
          );
          const cls = `flex w-full flex-col gap-1 rounded-md border border-line border-l-[3px] bg-surface px-[9px] py-2 text-left ${
            self ? 'shadow-[0_0_0_1px_rgba(207,159,63,.4)]' : ''
          } ${s.ousted ? 'opacity-60' : ''}`;
          return (
            <li key={s.name}>
              {onSeatClick ? (
                <button type="button" className={`${cls} cursor-pointer hover:bg-hover`} style={{ borderLeftColor: s.color }} onClick={() => onSeatClick(s)}>
                  {body}
                </button>
              ) : (
                <div className={cls} style={{ borderLeftColor: s.color }}>
                  {body}
                </div>
              )}
            </li>
          );
        })}
      </ul>
    </section>
  );
}
