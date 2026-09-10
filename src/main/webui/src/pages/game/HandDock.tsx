import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { HandStrip } from './HandStrip';
import type { HandCardContext } from './cardCommands';

// The wide-dock hand column — a bordered panel with its own "Your hand" header
// + count badge over a scrolling list-layout HandStrip. The narrow dock and the
// mobile sheet use HandStrip directly; this is the full-height first-class zone.
export function HandDock({
  className,
  handRegion,
  show,
  onPlayCardClick,
}: {
  className?: string;
  handRegion: RegionSnapshot | undefined;
  /** false for a spectator / judge — the panel still frames the (empty) zone. */
  show: boolean;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  return (
    <div className={`flex min-h-0 flex-col overflow-hidden rounded-md border border-line bg-surface/30${className ? ` ${className}` : ''}`}>
      <div className="flex shrink-0 items-center gap-2 border-b border-line px-2 py-1">
        <span className="text-xs font-bold uppercase tracking-wide text-ink-muted">Your hand</span>
        <span className="rounded-full bg-accent px-1.5 text-[0.7rem] font-semibold text-white tabular-nums">
          {handRegion?.cards.length ?? 0}
        </span>
      </div>
      <div className="min-h-0 flex-1 overflow-y-auto p-2">
        {show && <HandStrip handRegion={handRegion} show layout="list" onPlayCardClick={onPlayCardClick} />}
      </div>
    </div>
  );
}
