import { memo } from 'react';
import { Bell, ChevronLeft, ChevronRight, TriangleAlert } from 'lucide-react';
import type { CardSnapshot, PlayerSnapshot } from '../../api/types';
import { Region } from './Region';
import { PilesFooter } from './PilesFooter';
import { poolTone } from './poolTone';
import { SeatRelationChip } from './SeatRelationChip';
import type { SeatRelation } from './seatOrder';
import type { MenuAnchor } from './CardContextMenu';
import type { HandCardContext, Submission, TableCardContext } from './cardCommands';

// Live board state — full <Region> rendering, in this order.
const BOARD_REGION_ORDER = ['READY', 'TORPOR', 'UNCONTROLLED', 'RESEARCH'];
// Archival piles — collapsed into PilesFooter's one count row.
const PILE_REGIONS = new Set(['ASH_HEAP', 'REMOVED_FROM_GAME', 'LIBRARY', 'CRYPT', 'HAND']);
// GameView's default-collapsed set — see Region.tsx's comment on why this is
// purely client-side now. RESEARCH only starts collapsed once that player is ousted.

const PILL = 'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium';

// React.memo so an opponent acting — a full ['game', id] refetch — doesn't
// re-render every board: TanStack's structural sharing hands back the same
// `player` reference for any PlayerSnapshot that didn't change, and the other
// props here are primitives / stable callbacks (see GamePage's useCallback),
// so the memo comparison passes and the whole subtree (Region → Card) is
// skipped. Passing the needed `game` scalars instead of the whole
// GameSnapshot is what keeps that comparison meaningful.
export const PlayerBoard = memo(function PlayerBoard({
  player,
  gameId,
  edgeColor,
  edgeTextColor,
  isSeatedPlayer,
  viewerName,
  relation = null,
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
  relation?: SeatRelation;
  // Show a nudge button in this seat's header (the viewer can ping this player).
  pingable?: boolean;
  onTableCardClick: (ctx: TableCardContext, anchor: MenuAnchor) => void;
  onQuickCommand: (submission: Submission) => void;
  onCounterBump?: (ctx: TableCardContext, kind: 'blood', step: number) => void;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  const isViewer = player.name === viewerName;
  const activeBorder = player.active
    ? 'border-2 border-accent'
    : isViewer
      ? 'border-2 border-line-accent'
      : 'border border-line-accent';
  const ousted = player.pool < 1;

  const boardRegions = player.regions
    .filter((r) => !PILE_REGIONS.has(r.type))
    .sort((a, b) => BOARD_REGION_ORDER.indexOf(a.type) - BOARD_REGION_ORDER.indexOf(b.type));
  const pileRegions = player.regions.filter((r) => PILE_REGIONS.has(r.type));

  return (
    <div className="min-w-0">
      {relation && (
        <div className="mb-0.5 flex px-1">
          <SeatRelationChip relation={relation} />
        </div>
      )}
      <div className={`rounded-lg bg-hover shadow-lg overflow-hidden ${activeBorder} ${ousted ? 'opacity-70' : ''}`}>
        <div className={`px-2 py-1.5 border-b border-line ${player.active ? 'bg-accent/15' : 'bg-panel/60'}`}>
          <div className="flex justify-between items-center gap-2">
            <span className="font-bold flex items-center gap-1 min-w-0">
              <span className="truncate">{player.name}</span>
              {player.pinged ? (
                <TriangleAlert size={13} className="text-blood shrink-0" aria-label="Pinged" />
              ) : (
                pingable &&
                !isViewer && (
                  <button
                    type="button"
                    title={`Ping ${player.name}`}
                    aria-label={`Ping ${player.name}`}
                    onClick={() => onQuickCommand({ ping: player.name })}
                    // -m-1 offsets the padding so the larger tap area doesn't
                    // stretch the header row.
                    className="-m-1 inline-flex shrink-0 items-center justify-center rounded p-1 text-ink-muted hover:bg-hover hover:text-ink"
                  >
                    <Bell size={13} />
                  </button>
                )
              )}
            </span>
            {player.edge && (
              <span
                className={`${PILL} border border-line gap-1`}
                style={{ background: edgeColor, color: edgeTextColor }}
              >
                <ChevronLeft size={11} />
                Edge
                <ChevronRight size={11} />
              </span>
            )}
            <span className="flex items-center gap-1 shrink-0">
              {player.victoryPoints > 0 && (
                <span className={`${PILL} bg-gold text-surface`}>
                  {player.victoryPoints.toFixed(1).replace(/\.0$/, '')} VP
                </span>
              )}
              <span className={`${PILL} ${poolTone(player.pool)}`}>{player.pool}</span>
            </span>
          </div>
        </div>
        <div className="py-2">
          {boardRegions.map((region) => (
            <Region
              key={region.type}
              region={region}
              defaultCollapsed={ousted && region.type === 'RESEARCH'}
              controller={player.name}
              controllerPool={player.pool}
              isOwnRegion={isViewer}
              isSeatedPlayer={isSeatedPlayer}
              onTableCardClick={onTableCardClick}
              onQuickCommand={onQuickCommand}
              onCounterBump={onCounterBump}
              onPlayCardClick={onPlayCardClick}
            />
          ))}
          <PilesFooter
            regions={pileRegions}
            gameId={gameId}
            controller={player.name}
            controllerPool={player.pool}
            isOwnRegion={isViewer}
            isSeatedPlayer={isSeatedPlayer}
            onTableCardClick={onTableCardClick}
            onQuickCommand={onQuickCommand}
            onPlayCardClick={onPlayCardClick}
          />
        </div>
      </div>
    </div>
  );
});
