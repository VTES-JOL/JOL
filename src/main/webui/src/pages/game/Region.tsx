import { useEffect, useRef, useState } from 'react';
import { MinusCircle, PlusCircle } from 'lucide-react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { Card, RegionLabelBadges, type TableCardClick } from './Card';
import { CardSimple } from './CardSimple';
import type { HandCardContext, TableCardContext } from './cardCommands';

const REGION_STYLE: Record<string, string> = {
  TORPOR: 'bg-blood/10',
  READY: 'bg-online/10',
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
// GameSnapshotFactory's javadoc). A region that gains a card auto-expands so
// the change is visible, even if a viewer had collapsed it.
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
  const prevCardCount = useRef(region.cards.length);

  useEffect(() => {
    if (region.cards.length > prevCardCount.current) {
      setCollapsed(false);
    }
    prevCardCount.current = region.cards.length;
  }, [region.cards.length]);

  if (region.cards.length === 0) return null;

  const style = REGION_STYLE[region.type] ?? 'bg-panel';
  const mode = clickMode(region.type, isOwnRegion, isSeatedPlayer);

  const onAction = ({ coordinate, card, isChild }: TableCardClick) =>
    onTableCardClick({ controller, controllerPool, regionType: region.type, regionCommandKey: region.commandKey, coordinate, card, isChild });

  return (
    <div className="mb-2">
      <div className={`p-2 flex justify-between items-center text-sm ${style}`}>
        <span className="flex items-center gap-1">
          <button type="button" className="text-ink-muted hover:text-ink" onClick={() => setCollapsed((prev) => !prev)}>
            {collapsed ? <PlusCircle size={15} /> : <MinusCircle size={15} />}
          </button>
          <span className="font-bold">{region.label}</span> <span>( {region.cards.length} )</span>
          <RegionLabelBadges region={region} />
        </span>
      </div>
      {!collapsed && (
        <ol className={`region list-none ${style}`}>
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
