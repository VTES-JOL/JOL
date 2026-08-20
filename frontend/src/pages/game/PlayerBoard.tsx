import type { CardSnapshot, GameSnapshot, PlayerSnapshot } from '../../api/types';
import { Region } from './Region';
import type { HandCardContext, TableCardContext } from './cardCommands';

const REGION_ORDER = ['READY', 'TORPOR', 'UNCONTROLLED', 'ASH_HEAP', 'REMOVED_FROM_GAME', 'RESEARCH', 'LIBRARY', 'CRYPT', 'HAND'];
// GameView's default-collapsed set — see Region.tsx's comment on why this is
// purely client-side now. These five always start collapsed; READY/TORPOR/
// UNCONTROLLED/RESEARCH only start collapsed once that player is ousted.
const ALWAYS_COLLAPSED = new Set(['ASH_HEAP', 'REMOVED_FROM_GAME', 'LIBRARY', 'HAND', 'CRYPT']);

function poolStyle(pool: number): string {
  if (pool === 0) return 'text-bg-dark';
  return pool < 0 ? 'text-bg-warning' : 'text-bg-danger';
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
  const activeStyle = player.active ? 'text-bg-light border-dark border-2' : isViewer ? 'border-secondary border-2' : '';
  const activeHeaderStyle = player.active ? 'bg-info-subtle' : '';
  const ousted = player.pool < 1;

  const regions = [...player.regions].sort((a, b) => REGION_ORDER.indexOf(a.type) - REGION_ORDER.indexOf(b.type));

  return (
    <div className="col-xl col-lg-3 col-md-4 col-sm-6 g-2 player">
      <div className={`card shadow-lg ${activeStyle}`}>
        <div className={`card-header ${activeHeaderStyle} ${activeStyle}`}>
          <h6 className="d-flex justify-content-between align-items-center mb-0 lh-base">
            <span className="fw-bold">
              <span>{player.name}</span>
              {player.pinged && <i className="bi-exclamation-triangle ms-2" />}
            </span>
            {player.edge && (
              <span
                className="badge border border-secondary fw-bold align-items-center d-flex gap-1"
                style={{ background: game.edgeColor, color: game.edgeTextColor }}
              >
                <i className="bi bi-chevron-left" />
                Edge
                <i className="bi bi-chevron-right" />
              </span>
            )}
            <span className="d-inline align-items-center">
              {player.victoryPoints > 0 && (
                <span className="badge rounded-pill text-bg-warning">{player.victoryPoints.toFixed(1).replace(/\.0$/, '')} VP</span>
              )}
              <span className={`badge rounded-pill ${poolStyle(player.pool)}`}>{player.pool}</span>
            </span>
          </h6>
        </div>
        <div className="card-body px-0 py-2">
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
