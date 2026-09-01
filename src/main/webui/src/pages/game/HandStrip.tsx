import { memo } from 'react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { CardSimple } from './CardSimple';
import type { HandCardContext } from './cardCommands';

// Mirrors hand-card.jsp/hand.jsp — the compact "always visible to you" strip
// of the viewer's own hand, separate from that same hand's entry in their
// player board (which stays collapsed by default like any other region).
//
// memo'd + fed the resolved hand region (not the whole GameSnapshot): when an
// opponent acts, TanStack structural sharing keeps `handRegion`'s reference,
// so the strip doesn't re-render. It re-renders only when the viewer's own
// hand actually changes.
export const HandStrip = memo(function HandStrip({
  handRegion,
  show,
  onPlayCardClick,
}: {
  handRegion: RegionSnapshot | undefined;
  show: boolean;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  if (!show || !handRegion) return null;

  return (
    <div
      className="hand flex flex-col min-h-0 rounded-lg border border-line-accent bg-surface/85 shadow-lg overflow-hidden"
      data-region="hand"
    >
      <div className="px-3 py-1.5 border-b border-line bg-panel/60 text-sm font-semibold text-ink shrink-0">
        Hand
      </div>
      <ol className="flex-1 min-h-0 list-none scrollable divide-y divide-line/40">
        {handRegion.cards.map((card, i) => (
          <CardSimple
            key={card.id}
            card={card}
            region="HAND"
            coordinate={String(i + 1)}
            onClick={() =>
              onPlayCardClick({ regionType: handRegion.type, regionCommandKey: handRegion.commandKey, coordinate: String(i + 1) }, card)
            }
          />
        ))}
      </ol>
    </div>
  );
});
