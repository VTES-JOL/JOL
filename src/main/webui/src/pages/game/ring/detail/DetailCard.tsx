import { Flame, Lock, Minus, Plus } from 'lucide-react';
import type { KeyboardEvent, MouseEvent } from 'react';
import { Clan } from '../../Clan';
import { Path } from '../../Path';
import { Sect } from '../../Sect';
import { bloodFraction, cardLabel, type AttachmentKind, type CardView } from '../cardView';

// The Triad / Seat card: full name, disciplines, clan / sect / path, blood
// against capacity (with an optional stepper), state chips and the attached
// cards by name. Deliberately NOT MiniCard — the overview glyph stays glanceable
// while this one has room to be read. Both are fed by the same CardView.

export type DetailSize = 'triad' | 'seat';

const CHIP = 'inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[0.65rem] font-semibold';
const ATT_DOT: Record<AttachmentKind, string> = { equipment: 'bg-gold', retainer: 'bg-accent', other: 'bg-arcane' };

export interface DetailCardProps {
  view: CardView;
  seatColor: string;
  size?: DetailSize;
  onClick?: (view: CardView) => void;
  /** Shows − / + on a vampire's blood (the caller decides who may edit). */
  onCounter?: (view: CardView, step: number) => void;
}

export function DetailCard({ view, seatColor, size = 'seat', onClick, onCounter }: DetailCardProps) {
  const label = cardLabel(view);
  const s = view.source;
  const big = size === 'seat';
  const border = view.contested ? 'border-gold bg-gold/10' : view.torpor || view.faceDown ? 'border-dashed border-ink-muted' : view.locked ? 'border-accent bg-accent/5' : 'border-line-accent';
  const stop = (fn: () => void) => (e: MouseEvent) => {
    e.stopPropagation();
    fn();
  };
  const key = onClick
    ? (e: KeyboardEvent) => {
        if ((e.key === 'Enter' || e.key === ' ') && e.target === e.currentTarget) {
          e.preventDefault();
          onClick(view);
        }
      }
    : undefined;
  const interactive = onClick ? { role: 'button' as const, tabIndex: 0, onClick: () => onClick(view), onKeyDown: key } : {};

  if (view.hidden) {
    return (
      <div
        aria-label={label}
        className={`flex min-h-[3.25rem] items-center justify-center rounded-md border border-dashed border-ink-muted bg-panel px-2 py-1 text-[0.7rem] text-ink-muted ${onClick ? 'cursor-pointer' : ''}`}
        style={{ borderLeftColor: seatColor, borderLeftWidth: 4 }}
        {...interactive}
      >
        Hidden card
      </div>
    );
  }

  const isVamp = view.kind === 'vampire';
  return (
    <div
      aria-label={label}
      data-card-instance={view.id}
      className={`flex flex-col gap-1 rounded-md border p-2 text-ink ${border} ${onClick ? 'cursor-pointer hover:bg-hover/60 focus-visible:outline-2 focus-visible:outline-accent' : ''}`}
      style={{ borderLeftColor: seatColor, borderLeftWidth: 4 }}
      {...interactive}
    >
      <div className="flex items-start gap-1.5">
        <span className={`min-w-0 flex-1 font-medium leading-tight ${big ? 'text-sm' : 'text-xs'}`}>
          {view.name}
          {s.advanced && <i className="icon adv" />}
        </span>
        {view.titleTag && <span className={`${CHIP} bg-gold text-surface`}>{view.titleTag}</span>}
        {s.votes && s.votes !== '0' && <span className={`${CHIP} bg-gold-soft text-gold`}>{s.votes}v</span>}
        {s.infernal && <Flame size={13} className="shrink-0 text-blood" />}
        {view.locked && (
          <span className={`${CHIP} bg-accent text-white`} title="Locked">
            <Lock size={11} strokeWidth={2.75} />
          </span>
        )}
      </div>

      {(s.disciplines?.length || s.clan || s.sect || s.path) && (
        <div className="flex flex-wrap items-center gap-x-1 gap-y-0.5">
          {(s.disciplines ?? []).map((d) => (
            <span key={d} className={`icon ${d}`} />
          ))}
          <Path value={s.path} />
          <Sect value={s.sect} />
          <Clan value={s.clan} />
        </div>
      )}

      {isVamp && (
        <div className="flex items-center gap-1.5">
          {onCounter && (
            <button type="button" aria-label={`Remove blood from ${view.name}`} onClick={stop(() => onCounter(view, -1))} className="rounded border border-line-accent p-0.5 text-ink-muted hover:text-ink">
              <Minus size={12} />
            </button>
          )}
          <span className="relative h-4 flex-1 overflow-hidden rounded-sm bg-white/10" role="img" aria-label={`Blood ${view.counters}${view.capacity ? ` of ${view.capacity}` : ''}`}>
            <i className="absolute inset-y-0 left-0 block bg-blood" style={{ width: `${Math.round(bloodFraction(view) * 100)}%` }} />
            <b className="absolute inset-0 flex items-center justify-center font-mono text-[10px] text-white [text-shadow:0_1px_2px_rgba(0,0,0,.7)]">
              {view.counters}
              {view.capacity > 0 && ` / ${view.capacity}`}
            </b>
          </span>
          {onCounter && (
            <button type="button" aria-label={`Add blood to ${view.name}`} onClick={stop(() => onCounter(view, 1))} className="rounded border border-line-accent p-0.5 text-ink-muted hover:text-ink">
              <Plus size={12} />
            </button>
          )}
        </div>
      )}
      {view.kind === 'ally' && <span className="font-mono text-xs text-ink">Life {view.counters}</span>}
      {view.kind === 'location' && <span className="text-[0.65rem] uppercase tracking-wide text-ink-muted">Location</span>}

      {(view.contested || view.torpor || view.faceDown || s.label) && (
        <div className="flex flex-wrap gap-1">
          {view.contested && <span className={`${CHIP} bg-gold text-surface`}>CONTESTED</span>}
          {view.torpor && <span className={`${CHIP} border border-dashed border-ink-muted text-ink-muted`}>TORPOR</span>}
          {view.faceDown && <span className={`${CHIP} border border-dashed border-ink-muted text-ink-muted`}>FACE DOWN</span>}
          {s.label && !view.titleTag && <span className={`${CHIP} border border-line bg-hover text-ink`}>{s.label}</span>}
        </div>
      )}

      {(s.cards?.length ?? 0) > 0 && (
        <ul className="m-0 flex list-none flex-col gap-0.5 p-0">
          {s.cards!.map((a, i) => {
            const kind: AttachmentKind = view.attachments[i] ?? 'other';
            return (
              <li key={a.id} className="flex items-center gap-1.5 text-[0.7rem] text-ink-secondary">
                <i className={`block h-1.5 w-1.5 shrink-0 rounded-[2px] ${ATT_DOT[kind]}`} />
                <span className="min-w-0 flex-1 truncate">{a.visible ? (a.name ?? 'Card') : 'Hidden card'}</span>
                {a.counters > 0 && <span className="font-mono text-[0.65rem]">{a.counters}</span>}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
