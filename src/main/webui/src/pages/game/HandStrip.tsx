import { memo } from 'react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import type { HandCardContext } from './cardCommands';

// A colour band by card type, so the hand reads as scannable groups
// (master / reaction / combat / …) without leaning on card art.
function typeKey(typeClass?: string): string {
  const t = (typeClass ?? '').toLowerCase();
  if (t.includes('master')) return 'gold';
  if (t.includes('modifier')) return 'accent';
  if (t.includes('reaction')) return 'arcane';
  if (t.includes('combat')) return 'blood-soft';
  if (t.includes('ally') || t.includes('retainer')) return 'online';
  if (t.includes('equip')) return 'line-accent';
  if (t.includes('action') || t.includes('political') || t.includes('event')) return 'blood';
  return 'line-accent';
}
const BAND_BG: Record<string, string> = {
  gold: 'bg-gold',
  accent: 'bg-accent',
  arcane: 'bg-arcane',
  'blood-soft': 'bg-blood-soft',
  online: 'bg-online',
  'line-accent': 'bg-line-accent',
  blood: 'bg-blood',
};

// The viewer's own hand. Two layouts:
//   'strip' (default) — a horizontal row of concise chips that scrolls sideways,
//     for the dock band (Main.dc.html: the hand reads left to right, not tall).
//   'list' — a vertical stack of larger tiles (name · type icon · cost /
//     disciplines), for the mobile hand sheet where there's a whole screen to
//     use. Click either to open the play-card modal.
//
// memo'd + fed the resolved hand region (not the whole GameSnapshot): an
// opponent acting keeps `handRegion`'s reference (TanStack structural sharing),
// so it only re-renders when the viewer's own hand changes.
export const HandStrip = memo(function HandStrip({
  handRegion,
  show,
  layout = 'strip',
  onPlayCardClick,
}: {
  handRegion: RegionSnapshot | undefined;
  show: boolean;
  layout?: 'strip' | 'list';
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  if (!show || !handRegion) return null;

  const play = (card: CardSnapshot, coordinate: string) =>
    onPlayCardClick({ regionType: handRegion.type, regionCommandKey: handRegion.commandKey, coordinate }, card);

  if (layout === 'list') {
    return (
      <ul className="hand grid list-none gap-2 [grid-template-columns:repeat(auto-fill,minmax(min(10rem,100%),1fr))]">
        {handRegion.cards.map((card, i) => {
          const coordinate = String(i + 1);
          const disciplines = card.disciplines ?? [];
          return (
            <li key={card.id}>
              <button
                type="button"
                onClick={() => play(card, coordinate)}
                title={`Play ${card.name ?? 'card'} (hand ${coordinate})`}
                className="flex h-full w-full flex-col overflow-hidden rounded-lg border border-line-accent bg-surface text-left transition-colors hover:border-ink"
              >
                <span className={`h-1.5 w-full shrink-0 ${BAND_BG[typeKey(card.typeClass)]}`} />
                <span className="flex flex-1 flex-col gap-1 p-2">
                  <a
                    data-card-id={card.cardId}
                    data-secured={card.playtest ? 'true' : undefined}
                    className="card-name text-sm font-semibold leading-tight text-ink [text-wrap:balance]"
                  >
                    {card.name}
                    {card.advanced && <i className="icon adv" />}
                  </a>
                  <span className="mt-auto flex flex-wrap items-center gap-x-1.5 gap-y-0.5 text-[0.7rem] text-ink-muted">
                    <span className="shrink-0 tabular-nums">{coordinate}</span>
                    <span className={`icon card-type ${card.typeClass ?? ''}`} />
                    {disciplines.map((disc) => (
                      <span key={disc} className={`icon ${disc}`} />
                    ))}
                    {card.cost && <span>{card.cost}</span>}
                    {card.label && <span className="rounded bg-hover px-1 text-ink">{card.label}</span>}
                  </span>
                </span>
              </button>
            </li>
          );
        })}
        {handRegion.cards.length === 0 && (
          <li className="col-span-full p-3 text-sm text-ink-muted">Your hand is empty.</li>
        )}
      </ul>
    );
  }

  return (
    <ul className="hand no-scrollbar flex list-none items-center gap-1.5 overflow-x-auto overflow-y-hidden py-0.5">
      {handRegion.cards.map((card, i) => {
        const coordinate = String(i + 1);
        return (
          <li key={card.id} className="shrink-0">
            <button
              type="button"
              onClick={() => play(card, coordinate)}
              title={`Play ${card.name ?? 'card'} (hand ${coordinate})`}
              className="flex max-w-[13rem] items-center gap-1.5 rounded border border-line-accent bg-hover/50 px-2 py-1 text-xs text-ink hover:border-ink hover:bg-hover"
            >
              <span className="shrink-0 text-ink-muted tabular-nums">{coordinate}</span>
              <span className={`icon card-type shrink-0 ${card.typeClass ?? ''}`} />
              <a data-card-id={card.cardId} data-secured={card.playtest ? 'true' : undefined} className="card-name truncate">
                {card.name}
                {card.advanced && <i className="icon adv" />}
              </a>
            </button>
          </li>
        );
      })}
      {handRegion.cards.length === 0 && <li className="px-1 text-xs text-ink-muted">Hand empty</li>}
    </ul>
  );
});
