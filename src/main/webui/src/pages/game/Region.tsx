import { memo, useCallback, useEffect, useRef, useState } from 'react';
import { MinusCircle, PlusCircle } from 'lucide-react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { Card, RegionLabelBadges, type TableCardClick } from './Card';
import { CardSimple } from './CardSimple';
import type { HandCardContext, TableCardContext } from './cardCommands';

// READY/TORPOR/UNCONTROLLED are live board state — prominent header, coloured
// left edge. Everything else (ash heap, RFG, library, crypt, hand, research)
// is a reference pile: quiet, smaller header, no edge.
const PRIMARY_REGIONS = new Set(['READY', 'TORPOR', 'UNCONTROLLED']);

const REGION_ACCENT: Record<string, string> = {
  READY: 'border-l-online',
  TORPOR: 'border-l-blood',
  UNCONTROLLED: 'border-l-gold',
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
//
// React.memo: `region` keeps the same reference across an unrelated ['game',
// id] refetch (TanStack structural sharing), and every other prop is a
// primitive or a stable callback, so an opponent's action skips this whole
// card list. `onAction` is useCallback'd for the same reason — otherwise a
// fresh closure each render would defeat Card's own memo.
export const Region = memo(function Region({
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

  const onAction = useCallback(
    ({ coordinate, card, isChild }: TableCardClick) =>
      onTableCardClick({ controller, controllerPool, regionType: region.type, regionCommandKey: region.commandKey, coordinate, card, isChild }),
    [onTableCardClick, controller, controllerPool, region.type, region.commandKey],
  );

  if (region.cards.length === 0) return null;

  const primary = PRIMARY_REGIONS.has(region.type);
  const accent = REGION_ACCENT[region.type] ?? 'border-l-line-accent';
  const mode = clickMode(region.type, isOwnRegion, isSeatedPlayer);

  return (
    <div className={`mb-2 ${primary ? `border-l-2 ${accent}` : ''}`}>
      <div
        className={`px-2 py-1.5 flex justify-between items-center ${
          primary ? 'bg-panel border-b border-line-accent' : 'bg-panel/40'
        }`}
      >
        <span className="flex items-center gap-1.5 min-w-0">
          <button
            type="button"
            className="text-ink-muted hover:text-ink shrink-0"
            onClick={() => setCollapsed((prev) => !prev)}
            aria-label={collapsed ? `Expand ${region.label}` : `Collapse ${region.label}`}
          >
            {collapsed ? <PlusCircle size={15} /> : <MinusCircle size={15} />}
          </button>
          <span
            className={`uppercase tracking-wide truncate ${
              primary ? 'font-bold text-xs text-ink' : 'font-semibold text-[0.7rem] text-ink-muted'
            }`}
          >
            {region.label}
          </span>
          <RegionLabelBadges region={region} />
        </span>
        <span className={`text-xs tabular-nums shrink-0 ${primary ? 'text-ink-secondary' : 'text-ink-muted'}`}>
          {region.cards.length}
        </span>
      </div>
      {!collapsed && (
        <ol className="region list-none divide-y divide-line/40">
          {region.cards.map((card, i) => {
            const coordinate = String(i + 1);
            if (region.simple) {
              const onClick =
                mode === 'action'
                  ? () => onAction({ coordinate, card, isChild: false })
                  : mode === 'play'
                    ? () => onPlayCardClick({ regionType: region.type, regionCommandKey: region.commandKey, coordinate }, card)
                    : undefined;
              return <CardSimple key={card.id} card={card} region={region.type} coordinate={coordinate} onClick={onClick} />;
            }
            return (
              <Card
                key={card.id}
                card={card}
                region={region.type}
                coordinate={coordinate}
                onAction={mode === 'action' ? onAction : undefined}
              />
            );
          })}
        </ol>
      )}
    </div>
  );
});
