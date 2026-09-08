import type { CardSnapshot, PlayerSnapshot } from '../../api/types';
import { PlayerBoard } from './PlayerBoard';
import type { MenuAnchor } from './CardContextMenu';
import type { HandCardContext, Submission, TableCardContext } from './cardCommands';

// The viewer's own seat, at the foot of the table column above the docked
// hand + command band (Main.dc.html). Its own board is the one you act from
// most, so it stays put while the opponents scroll above; it scrolls
// internally when tall. The border/spacing above it is owned by GamePage's
// dock wrapper. Hidden for a spectator / judge who isn't seated.
export function YourSeatDock({
  player,
  gameId,
  edgeColor,
  edgeTextColor,
  viewerName,
  onTableCardClick,
  onQuickCommand,
  onCounterBump,
  onPlayCardClick,
}: {
  player: PlayerSnapshot;
  gameId: string;
  edgeColor: string;
  edgeTextColor: 'white' | 'black';
  viewerName: string | null;
  onTableCardClick: (ctx: TableCardContext, anchor: MenuAnchor) => void;
  onQuickCommand: (submission: Submission) => void;
  onCounterBump?: (ctx: TableCardContext, kind: 'blood', step: number) => void;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  return (
    <div className="game-board flex-1 min-h-0 overflow-y-auto">
      <div className="mb-1 px-1 text-[0.7rem] font-semibold uppercase tracking-wide text-ink-muted">
        Your seat
      </div>
      <PlayerBoard
        player={player}
        gameId={gameId}
        edgeColor={edgeColor}
        edgeTextColor={edgeTextColor}
        isSeatedPlayer
        viewerName={viewerName}
        onTableCardClick={onTableCardClick}
        onQuickCommand={onQuickCommand}
        onCounterBump={onCounterBump}
        onPlayCardClick={onPlayCardClick}
      />
    </div>
  );
}
