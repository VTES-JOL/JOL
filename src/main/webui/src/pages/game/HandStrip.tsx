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
    <div className="card shadow hand" data-region="hand">
      <div className="card-header bg-body-secondary">Hand</div>
      <ol className="card-body list-group list-group-numbered p-0 scrollable">
        {hand.cards.map((card, i) => (
          <CardSimple
            key={card.id}
            card={card}
            region="HAND"
            onClick={() => onPlayCardClick({ regionType: hand.type, regionCommandKey: hand.commandKey, coordinate: String(i + 1) }, card)}
          />
        ))}
      </ol>
    </div>
  );
}
