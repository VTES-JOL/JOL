import { memo, type CSSProperties } from 'react';
import { bloodFraction, cardLabel, type AttachmentKind, type CardView } from './cardView';
import { MINI_CARD_H, MINI_CARD_W } from './miniCardFootprint';
import { allyPipRows } from './pipLayout';

// The overview-level card glyph used by the Wedge and Panels layouts: a 46×64
// playing-card box a player can read at a glance. It draws ONLY its own box —
// never positions itself — and turns itself 90° when locked (rotation and
// `scale` are transforms, so the box's layout size stays 46×64; the packer
// gets the on-screen size from miniCardFootprint()). Everything inside the box
// may change freely; if the box itself changes, update miniCardFootprint.ts.
//
// Triad / Seat levels use a separate, richer renderer — see plan §2.

export type MiniCardMeter = 'gauge' | 'pips';

const ATTACH_COLOR: Record<AttachmentKind, string> = {
  equipment: 'bg-gold',
  retainer: 'bg-accent',
  other: 'bg-arcane',
};
const MAX_ATTACH_SHOWN = 3;
const MAX_PIPS = 12;

export interface MiniCardProps {
  view: CardView;
  /** Seat colour — border and header. Any CSS colour. */
  seatColor: string;
  meter?: MiniCardMeter;
  /** Global glyph scale from the packer, 0.52 – 1. */
  scale?: number;
  /** Makes the glyph a button (opens the card menu / detail); otherwise it is a labelled image. */
  onClick?: (view: CardView) => void;
}

// An unfilled gauge / pip gets a thin blood-coloured outline so the *capacity* is
// visible, not just the blood: "2 of 6" and "0 of 6" read as partly / fully empty.
const CAPACITY_OUTLINE = 'inset 0 0 0 1px color-mix(in srgb, var(--color-blood) 70%, transparent)';
const NUMBER_SHADOW = '[text-shadow:0_0_2px_#000,0_0_3px_#000,0_1px_2px_#000]';

function Body({ view, meter }: { view: CardView; meter: MiniCardMeter }) {
  if (view.kind === 'vampire') {
    const notFull = view.capacity > 0 && view.counters < view.capacity;
    if (meter === 'gauge') {
      return (
        <span
          className="relative block min-h-[22px] w-full flex-1 overflow-hidden rounded-[2px] bg-blood/25"
          style={notFull ? { boxShadow: CAPACITY_OUTLINE } : undefined}
        >
          <i
            className="absolute inset-x-0 bottom-0 block bg-blood"
            style={{ height: `${Math.round(bloodFraction(view) * 100)}%` }}
          />
          <b className={`absolute inset-0 flex items-center justify-center font-mono text-[14px] font-extrabold leading-none text-white ${NUMBER_SHADOW}`}>
            {view.counters}
            {view.capacity > 0 && <u className="ml-px text-[8px] font-bold no-underline opacity-90">/{view.capacity}</u>}
          </b>
        </span>
      );
    }
    const pips = Math.min(view.capacity, MAX_PIPS);
    return (
      <>
        <span className="flex w-full gap-px">
          {Array.from({ length: pips }, (_, i) => (
            <i
              key={i}
              className={`block h-2 flex-1 ${i < view.counters ? 'bg-blood' : 'bg-blood/20'}`}
              style={i < view.counters ? undefined : { boxShadow: CAPACITY_OUTLINE }}
            />
          ))}
        </span>
        <span className="font-mono text-[8.5px] font-bold leading-none text-ink">
          {view.counters}/{view.capacity}
        </span>
      </>
    );
  }
  if (view.kind === 'ally') {
    // An ally has no capacity, so for display its capacity is its counters: one green pip
    // per counter, all filled, equally sized like a vampire's row (never an empty slot).
    const { rows, height } = allyPipRows(view.counters);
    return (
      <>
        {rows.length > 0 && (
          <span className="flex w-full flex-col gap-px" aria-hidden>
            {rows.map((n, r) => (
              <span key={r} className="flex w-full gap-px">
                {Array.from({ length: n }, (_, i) => (
                  <i key={i} className="block flex-1 bg-green-500" style={{ height }} />
                ))}
              </span>
            ))}
          </span>
        )}
        <span className="font-mono text-[8.5px] font-bold leading-none text-ink">{view.counters}</span>
      </>
    );
  }
  return null;
}

export const MiniCard = memo(function MiniCard({ view, seatColor, meter = 'gauge', scale = 1, onClick }: MiniCardProps) {
  const label = cardLabel(view);
  const style = {
    '--seat': seatColor,
    width: MINI_CARD_W,
    height: MINI_CARD_H,
    transform: `rotate(${view.locked ? 90 : 0}deg) scale(${scale})`,
  } as CSSProperties;

  const tone = view.hidden
    ? 'bg-panel'
    : view.kind === 'location'
      ? 'bg-arcane-soft'
      : 'bg-surface';
  const state = [
    view.contested ? 'shadow-[0_0_0_2px_var(--color-blood)]' : '',
    view.torpor ? 'border-dashed opacity-80 grayscale-[.65]' : '',
    view.faceDown && !view.torpor ? 'border-dashed' : '',
    onClick ? 'cursor-pointer hover:brightness-125 focus-visible:outline-2 focus-visible:outline-accent' : '',
  ].join(' ');
  const cls = `relative box-border flex shrink-0 select-none flex-col overflow-hidden rounded-[5px] border-[1.5px] border-[var(--seat)] p-0 text-left ${tone} ${state}`;

  const content = view.hidden ? (
    <span
      aria-hidden
      className="block h-full w-full opacity-60"
      style={{ background: 'repeating-linear-gradient(45deg, var(--seat) 0 1.5px, transparent 1.5px 6px)' }}
    />
  ) : (
    <>
      <span className="line-clamp-2 block h-[22px] flex-none overflow-hidden break-words bg-[var(--seat)] px-[3px] py-[2px] text-center text-[6.8px] font-bold leading-[1.12] text-[#0d0c10]">
        {view.shortName}
      </span>
      <span className="flex flex-1 flex-col items-center justify-center gap-[3px] px-1 py-[2px]">
        <Body view={view} meter={meter} />
        <span className="flex min-h-2 w-full items-center justify-between">
          <span className="font-mono text-[6.5px] font-bold text-gold">{view.titleTag}</span>
          <span className="flex gap-0.5">
            {view.attachments.slice(0, MAX_ATTACH_SHOWN).map((a, i) => (
              <i key={i} className={`block h-[7px] w-[7px] rounded-[2px] ${ATTACH_COLOR[a]}`} />
            ))}
          </span>
        </span>
      </span>
    </>
  );

  return onClick ? (
    <button type="button" aria-label={label} title={label} className={cls} style={style} data-meter={view.kind === 'vampire' ? meter : undefined} onClick={() => onClick(view)}>
      {content}
    </button>
  ) : (
    <div role="img" aria-label={label} title={label} className={cls} style={style} data-meter={view.kind === 'vampire' ? meter : undefined}>
      {content}
    </div>
  );
});
