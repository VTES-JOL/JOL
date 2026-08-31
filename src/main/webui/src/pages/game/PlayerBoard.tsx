import { ChevronLeft, ChevronRight, TriangleAlert } from 'lucide-react';
import type { CardSnapshot, GameSnapshot, PlayerSnapshot } from '../../api/types';
import { Region } from './Region';
import type { HandCardContext, TableCardContext } from './cardCommands';

const REGION_ORDER = ['READY', 'TORPOR', 'UNCONTROLLED', 'ASH_HEAP', 'REMOVED_FROM_GAME', 'RESEARCH', 'LIBRARY', 'CRYPT', 'HAND'];
// GameView's default-collapsed set — see Region.tsx's comment on why this is
// purely client-side now. These five always start collapsed; READY/TORPOR/
// UNCONTROLLED/RESEARCH only start collapsed once that player is ousted.
const ALWAYS_COLLAPSED = new Set(['ASH_HEAP', 'REMOVED_FROM_GAME', 'LIBRARY', 'HAND', 'CRYPT']);

const PILL = 'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium';

function poolStyle(pool: number): string {
  if (pool === 0) return 'bg-ink text-base';
  return pool < 0 ? 'bg-gold text-surface' : 'bg-blood text-surface';
}

export function PlayerBoard({
  player,
  game,
  viewerName,
  onTableCardClick,
  onPlayCardClick,
}: {
  player: PlayerSnapshot;
  game: GameSnapshot;
  viewerName: string | null;
  onTableCardClick: (ctx: TableCardContext) => void;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  const isViewer = player.name === viewerName;
  const activeBorder = player.active
    ? 'border-2 border-accent'
    : isViewer
      ? 'border-2 border-line-accent'
      : 'border border-line-accent';
  const ousted = player.pool < 1;

  const regions = [...player.regions].sort((a, b) => REGION_ORDER.indexOf(a.type) - REGION_ORDER.indexOf(b.type));

  return (
    <div className="min-w-0">
      <div className={`rounded-lg bg-hover shadow-lg overflow-hidden ${activeBorder} ${ousted ? 'opacity-70' : ''}`}>
        <div className={`px-2 py-1.5 border-b border-line ${player.active ? 'bg-accent/15' : 'bg-panel/60'}`}>
          <div className="flex justify-between items-center gap-2">
            <span className="font-bold flex items-center gap-1 min-w-0">
              <span className="truncate">{player.name}</span>
              {player.pinged && <TriangleAlert size={13} className="text-blood shrink-0" />}
            </span>
            {player.edge && (
              <span
                className={`${PILL} border border-line gap-1`}
                style={{ background: game.edgeColor, color: game.edgeTextColor }}
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
              <span className={`${PILL} ${poolStyle(player.pool)}`}>{player.pool}</span>
            </span>
          </div>
        </div>
        <div className="py-2">
          {regions.map((region) => (
            <Region
              key={region.type}
              region={region}
              defaultCollapsed={ALWAYS_COLLAPSED.has(region.type) || (ousted && ['READY', 'TORPOR', 'UNCONTROLLED', 'RESEARCH'].includes(region.type))}
              controller={player.name}
              controllerPool={player.pool}
              isOwnRegion={isViewer}
              isSeatedPlayer={game.player}
              onTableCardClick={onTableCardClick}
              onPlayCardClick={onPlayCardClick}
            />
          ))}
        </div>
      </div>
    </div>
  );
}
