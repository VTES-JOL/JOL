import type { CardSnapshot, GameSnapshot } from '../../api/types';
import { CardSimple } from './CardSimple';
import type { HandCardContext } from './cardCommands';

// Mirrors hand-card.jsp/hand.jsp — the compact "always visible to you" strip
// of the viewer's own hand, separate from that same hand's entry in their
// player board (which stays collapsed by default like any other region).
export function HandStrip({
  game,
  viewerName,
  onPlayCardClick,
}: {
  game: GameSnapshot;
  viewerName: string | null;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  if (!game.player || !viewerName) return null;
  const me = game.players.find((p) => p.name === viewerName);
  const hand = me?.regions.find((r) => r.type === 'HAND');
  if (!hand) return null;

  return (
    <div
      className="hand flex flex-col min-h-0 rounded-lg border border-line-accent bg-surface/85 shadow-lg overflow-hidden"
      data-region="hand"
    >
      <div className="px-3 py-1.5 border-b border-line bg-panel/60 text-sm font-semibold text-ink shrink-0">
        Hand
      </div>
      <ol className="flex-1 min-h-0 list-none scrollable">
        {hand.cards.map((card, i) => (
          <CardSimple
            key={card.id}
            card={card}
            region="HAND"
            coordinate={String(i + 1)}
            onClick={() => onPlayCardClick({ regionType: hand.type, regionCommandKey: hand.commandKey, coordinate: String(i + 1) }, card)}
          />
        ))}
      </ol>
    </div>
  );
}
