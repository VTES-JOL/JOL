import { memo } from 'react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { Region } from './Region';
import { RegionLabelBadges } from './Card';
import type { MenuAnchor } from './CardContextMenu';
import type { HandCardContext, Submission, TableCardContext } from './cardCommands';
import { usePileExpanded } from './usePileCollapse';

// The archival piles a seat ends in — collapsed by default into one count row
// instead of 4–5 stacked 29px headers (ui-design measured ~116–145px per seat
// reclaimed). Each segment expands its pile inline, rendering the unchanged
// <Region>. Order matches REGION_ORDER's tail.
const PILE_ORDER = ['ASH_HEAP', 'REMOVED_FROM_GAME', 'LIBRARY', 'CRYPT', 'HAND'];
const ABBREV: Record<string, string> = {
  ASH_HEAP: 'Ash',
  REMOVED_FROM_GAME: 'RFG',
  LIBRARY: 'Lib',
  CRYPT: 'Crypt',
  HAND: 'Hand',
};

export interface PilesFooterProps {
  regions: RegionSnapshot[];
  gameId: string;
  controller: string;
  controllerPool: number;
  isOwnRegion: boolean;
  isSeatedPlayer: boolean;
  onTableCardClick: (ctx: TableCardContext, anchor: MenuAnchor) => void;
  onQuickCommand: (submission: Submission) => void;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}

function PileSegment({
  region,
  gameId,
  controller,
  controllerPool,
  isOwnRegion,
  isSeatedPlayer,
  onTableCardClick,
  onQuickCommand,
  onPlayCardClick,
}: { region: RegionSnapshot } & Omit<PilesFooterProps, 'regions'>) {
  const [expanded, toggle] = usePileExpanded(gameId, controller, region.type);
  const abbrev = ABBREV[region.type] ?? region.label;
  return (
    <>
      <button
        type="button"
        onClick={toggle}
        title={region.label}
        aria-expanded={expanded}
        className={`inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-xs tabular-nums transition-colors ${
          expanded ? 'bg-hover text-ink' : 'text-ink-muted hover:bg-hover hover:text-ink-secondary'
        }`}
      >
        <span className="uppercase tracking-wide text-[0.65rem]">{abbrev}</span>
        <span>{region.cards.length}</span>
        <RegionLabelBadges region={region} />
      </button>
      {expanded && (
        <div className="basis-full">
          <Region
            region={region}
            defaultCollapsed={false}
            controller={controller}
            controllerPool={controllerPool}
            isOwnRegion={isOwnRegion}
            isSeatedPlayer={isSeatedPlayer}
            onTableCardClick={onTableCardClick}
            onQuickCommand={onQuickCommand}
            onPlayCardClick={onPlayCardClick}
          />
        </div>
      )}
    </>
  );
}

export const PilesFooter = memo(function PilesFooter({ regions, ...rest }: PilesFooterProps) {
  const ordered = [...regions].sort(
    (a, b) => PILE_ORDER.indexOf(a.type) - PILE_ORDER.indexOf(b.type),
  );
  if (ordered.length === 0) return null;
  return (
    <div className="mx-2 mt-1 flex flex-wrap items-center gap-x-1 gap-y-1 border-t border-line/60 pt-1.5">
      {ordered.map((region) => (
        <PileSegment key={region.type} region={region} {...rest} />
      ))}
    </div>
  );
});
