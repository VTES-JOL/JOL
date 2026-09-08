import { memo, useState } from 'react';
import { ChevronRight } from 'lucide-react';
import type { CardSnapshot, PlayerSnapshot } from '../../api/types';
import { PlayerBoard } from './PlayerBoard';
import type { SeatRelation } from './seatOrder';
import type { MenuAnchor } from './CardContextMenu';
import type { HandCardContext, Submission, TableCardContext } from './cardCommands';

// One opponent seat in the #opponents scroller. An ousted seat (pool < 1) —
// out of the game, nothing actionable — collapses to a one-line strip so it
// stops eating a full grid column (ui-design #16); click to expand the full
// board if you need to inspect it. A live seat is just the PlayerBoard.
export const SeatColumn = memo(function SeatColumn({
  player,
  gameId,
  edgeColor,
  edgeTextColor,
  isSeatedPlayer,
  viewerName,
  relation,
  pingable = false,
  onTableCardClick,
  onQuickCommand,
  onCounterBump,
  onPlayCardClick,
}: {
  player: PlayerSnapshot;
  gameId: string;
  edgeColor: string;
  edgeTextColor: 'white' | 'black';
  isSeatedPlayer: boolean;
  viewerName: string | null;
  relation: SeatRelation;
  pingable?: boolean;
  onTableCardClick: (ctx: TableCardContext, anchor: MenuAnchor) => void;
  onQuickCommand: (submission: Submission) => void;
  onCounterBump?: (ctx: TableCardContext, kind: 'blood', step: number) => void;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  const ousted = player.pool < 1;
  const [expanded, setExpanded] = useState(false);

  if (ousted && !expanded) {
    return (
      <button
        type="button"
        onClick={() => setExpanded(true)}
        className="flex w-full items-center gap-2 rounded-lg border border-line-accent bg-hover/60 px-2 py-1.5 text-left text-sm text-ink-muted opacity-80 hover:opacity-100"
      >
        <ChevronRight size={13} className="shrink-0" />
        <span className="truncate font-semibold text-ink-secondary">{player.name}</span>
        {/* Oust vs withdrawal and the VP recipient come from a persisted
            per-exit record (backend C5) — the ring has re-linked past this seat
            by now, so neither is derivable from the live snapshot. */}
        {player.exitKind === 'WITHDRAW' ? (
          <span className="shrink-0 text-xs">— withdrew (+1 VP)</span>
        ) : player.exitKind === 'OUST' && player.exitVpRecipient ? (
          <span className="shrink-0 truncate text-xs">
            — ousted <span aria-hidden>→</span> {player.exitVpRecipient}
          </span>
        ) : (
          <span className="shrink-0 text-xs">— out</span>
        )}
        {player.victoryPoints > 0 && (
          <span className="ml-auto shrink-0 rounded-full bg-gold px-2 py-0.5 text-xs font-medium text-surface">
            {player.victoryPoints.toFixed(1).replace(/\.0$/, '')} VP
          </span>
        )}
      </button>
    );
  }

  return (
    <div>
      {ousted && (
        <button
          type="button"
          onClick={() => setExpanded(false)}
          className="mb-0.5 text-xs text-ink-muted hover:text-ink"
        >
          Collapse ousted seat
        </button>
      )}
      <PlayerBoard
        player={player}
        gameId={gameId}
        edgeColor={edgeColor}
        edgeTextColor={edgeTextColor}
        isSeatedPlayer={isSeatedPlayer}
        viewerName={viewerName}
        relation={relation}
        pingable={pingable}
        onTableCardClick={onTableCardClick}
        onQuickCommand={onQuickCommand}
        onCounterBump={onCounterBump}
        onPlayCardClick={onPlayCardClick}
      />
    </div>
  );
});
