import { memo } from 'react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import type { HandCardContext } from './cardCommands';

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
      <ul className="hand flex list-none flex-col gap-2">
        {handRegion.cards.map((card, i) => {
          const coordinate = String(i + 1);
          const disciplines = card.disciplines ?? [];
          return (
            <li key={card.id}>
              <button
                type="button"
                onClick={() => play(card, coordinate)}
                title={`Play ${card.name ?? 'card'} (hand ${coordinate})`}
                className="flex w-full items-start gap-2.5 rounded-lg border border-line-accent bg-hover/40 p-3 text-left hover:border-ink hover:bg-hover"
              >
                <span className="mt-0.5 shrink-0 text-xs tabular-nums text-ink-muted">{coordinate}</span>
                <span className={`icon card-type mt-0.5 shrink-0 ${card.typeClass ?? ''}`} />
                <span className="min-w-0 flex-1">
                  <a
                    data-card-id={card.cardId}
                    data-secured={card.playtest ? 'true' : undefined}
                    className="card-name block text-sm font-medium text-ink"
                  >
                    {card.name}
                    {card.advanced && <i className="icon adv" />}
                  </a>
                  {(disciplines.length > 0 || card.cost || card.label) && (
                    <span className="mt-1 flex flex-wrap items-center gap-x-2 gap-y-1 text-xs text-ink-muted">
                      {disciplines.map((disc) => (
                        <span key={disc} className={`icon ${disc}`} />
                      ))}
                      {card.cost && <span>{card.cost}</span>}
                      {card.label && <span className="rounded bg-hover px-1.5 text-ink">{card.label}</span>}
                    </span>
                  )}
                </span>
              </button>
            </li>
          );
        })}
        {handRegion.cards.length === 0 && <li className="p-3 text-sm text-ink-muted">Your hand is empty.</li>}
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
