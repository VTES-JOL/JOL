import { useState } from 'react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { Card, RegionLabelBadges, type TableCardClick } from './Card';
import { CardSimple } from './CardSimple';
import type { HandCardContext, TableCardContext } from './cardCommands';

const REGION_STYLE: Record<string, string> = {
  TORPOR: 'bg-danger-subtle',
  READY: 'bg-success-subtle',
};

// card.jsp/card-simple.jsp/card-hidden.jsp's click routing, replicated
// exactly: READY/TORPOR/UNCONTROLLED (full card.jsp) and ASH_HEAP always
// route to the on-table action modal for any seated player (no owner check
// — you bleed/block/contest OPPONENTS' cards); HAND/RESEARCH open the
// play-card modal, but only for the viewer's own region. Every other
// region/viewer combination (including any non-seated viewer — spectators,
// judges, admins) is inert, matching `showAction`'s outer
// `game.getPlayers().contains(viewer)` gate.
type ClickMode = 'action' | 'play' | null;

function clickMode(regionType: string, isOwnRegion: boolean, isSeatedPlayer: boolean): ClickMode {
  if (!isSeatedPlayer) return null;
  if (regionType === 'READY' || regionType === 'TORPOR' || regionType === 'UNCONTROLLED' || regionType === 'ASH_HEAP') return 'action';
  if ((regionType === 'HAND' || regionType === 'RESEARCH') && isOwnRegion) return 'play';
  return null;
}

// Mirrors region.jsp — collapse/expand is purely local UI state here (see
// GameSnapshotFactory's javadoc: the server no longer tracks this at all,
// simplifying away GameView's per-viewer `collapsed` set).
export function Region({
  region,
  defaultCollapsed,
  controller,
  controllerPool,
  isOwnRegion,
  isSeatedPlayer,
  onTableCardClick,
  onPlayCardClick,
}: {
  region: RegionSnapshot;
  defaultCollapsed: boolean;
  controller: string;
  controllerPool: number;
  isOwnRegion: boolean;
  isSeatedPlayer: boolean;
  onTableCardClick: (ctx: TableCardContext) => void;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  const [collapsed, setCollapsed] = useState(defaultCollapsed);

  if (region.cards.length === 0) return null;

  const style = REGION_STYLE[region.type] ?? 'bg-body-secondary';
  const mode = clickMode(region.type, isOwnRegion, isSeatedPlayer);

  const onAction = ({ coordinate, card, isChild }: TableCardClick) =>
    onTableCardClick({ controller, controllerPool, regionType: region.type, regionCommandKey: region.commandKey, coordinate, card, isChild });

  return (
    <div className="mb-2 text-bg-light">
      <div className={`p-2 d-flex justify-content-between align-items-center ${style}`}>
        <span>
          <button className="btn btn-sm p-0" onClick={() => setCollapsed((prev) => !prev)}>
            <i className={`fs-6 bi ${collapsed ? 'bi-plus-circle' : 'bi-dash-circle'}`} />
          </button>{' '}
          <span className="fw-bold">{region.label}</span> <span>( {region.cards.length} )</span>{' '}
          <RegionLabelBadges region={region} />
        </span>
      </div>
      {!collapsed && (
        <ol className={`region list-group list-group-flush list-group-numbered ${style}`}>
          {region.cards.map((card, i) => {
            const coordinate = String(i + 1);
            if (region.simple) {
              const onClick =
                mode === 'action'
                  ? () => onAction({ coordinate, card, isChild: false })
                  : mode === 'play'
                    ? () => onPlayCardClick({ regionType: region.type, regionCommandKey: region.commandKey, coordinate }, card)
                    : undefined;
              return <CardSimple key={card.id} card={card} region={region.type} onClick={onClick} />;
            }
            return (
              <Card
                key={card.id}
                card={card}
                region={region.type}
                shadow
                coordinate={coordinate}
                onAction={mode === 'action' ? onAction : undefined}
              />
            );
          })}
        </ol>
      )}
    </div>
  );
}
