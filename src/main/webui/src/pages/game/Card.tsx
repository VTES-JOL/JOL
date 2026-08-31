import { Eye, EyeOff, Flame } from 'lucide-react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { CardHidden } from './CardHidden';
import { Clan } from './Clan';
import { Sect } from './Sect';
import { Path } from './Path';

export const COUNTER_STYLE = (hasLife: boolean, hasBlood: boolean, capacity: number, otherVisibleRegion: boolean) => {
  if (hasLife && otherVisibleRegion) return 'bg-online text-surface';
  if (hasBlood || capacity > 0) return 'bg-blood text-surface';
  return 'bg-hover text-ink-muted';
};

// region.jsp's RegionType.OTHER_VISIBLE_REGIONS — regions visible to
// opponents too (READY, ASH_HEAP, TORPOR, REMOVED_FROM_GAME), used only to
// pick the counter badge color (green="life" counters vs red="blood"/capacity).
export const OTHER_VISIBLE_REGIONS = new Set(['READY', 'ASH_HEAP', 'TORPOR', 'REMOVED_FROM_GAME']);

const PILL = 'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium';

export interface TableCardClick {
  coordinate: string;
  card: CardSnapshot;
  isChild: boolean;
}

// Mirrors card.jsp — the "full" card rendering used for CRYPT and the
// non-simple regions (READY/TORPOR/UNCONTROLLED). Recurses into its own
// `cards` for nested equipment/allies/blood-counter stacks.
export function Card({
  card,
  region,
  shadow,
  coordinate,
  isChild = false,
  onAction,
}: {
  card: CardSnapshot;
  region: string;
  shadow: boolean;
  coordinate: string;
  isChild?: boolean;
  onAction?: (click: TableCardClick) => void;
}) {
  if (!card.visible) return <CardHidden card={card} region={region} coordinate={coordinate} />;

  const hasVotes = !!card.votes && card.votes !== '0';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const showCounterBadge = (card.counters ?? 0) > 0 || (card.capacity ?? 0) > 0;
  const counterStyle = COUNTER_STYLE(!!card.hasLife, !!card.hasBlood, card.capacity ?? 0, OTHER_VISIBLE_REGIONS.has(region));
  const regionStyle = region === 'TORPOR' ? 'opacity-75' : '';
  const contestedStyle = card.contested ? 'bg-gold/15' : '';

  return (
    <li
      className={`flex justify-between items-baseline px-2 pt-2 pb-1 border-b border-line/50 ${regionStyle} ${shadow ? 'shadow-sm' : ''} ${contestedStyle}`}
      onClick={onAction ? () => onAction({ coordinate, card, isChild }) : undefined}
      style={onAction ? { cursor: 'pointer' } : undefined}
    >
      <div className="mx-1 me-auto w-full">
        <div className="flex justify-between">
          <div className="flex flex-col">
            <div className="flex items-center gap-1">
              <span className="text-ink-muted text-xs tabular-nums select-all shrink-0">{coordinate}</span>
              <a data-card-id={card.cardId} data-secured={card.playtest ? 'true' : undefined} className="card-name text-wrap">
                {card.name}
                {card.advanced && <i className="icon adv" />}
              </a>
              {hasVotes && <span className={`${PILL} bg-gold text-surface`}>{card.votes}</span>}
              {card.contested && (
                <span className={`${PILL} bg-gold text-surface text-[0.7rem]`}>CONTESTED</span>
              )}
            </div>
            <div className="flex items-center gap-1">
              {(card.disciplines ?? []).map((disc) => (
                <span key={disc} className={`icon ${disc}`} />
              ))}
            </div>
            {
              card.label && <div className="flex items-center gap-1"><span className={`${PILL} bg-hover text-ink border border-line`}>{card.label}</span></div>
            }
          </div>
          <div className="flex flex-col">
            <div className="flex justify-end items-center gap-1">
              {card.infernal && <Flame size={14} className="text-blood" />}
              {card.locked && <span className={`${PILL} bg-ink text-base text-[0.7rem]`}>LOCKED</span>}
              {showCounterBadge && <span className={`${PILL} ${counterStyle} shadow-sm`}>{counterText}</span>}
            </div>
            <div className="flex justify-end items-center gap-1">
              <Path value={card.path} />
              <Sect value={card.sect} />
              <Clan value={card.clan} />
            </div>
          </div>
        </div>
        {(card.cards?.length ?? 0) > 0 && (
          // Nested equipment/retainer/counter stack. Preflight zeroes the
          // browser-default <ol> padding Bootstrap's Reboot left in place, so
          // the indent that used to come for free is set explicitly here —
          // it compounds per recursion level, stepping deep stacks inward.
          <ol className="list-none ml-1">
            {card.cards!.map((nested, i) => (
              <NestedCard key={nested.id} card={nested} region={region} coordinate={`${coordinate}.${i + 1}`} onAction={onAction} />
            ))}
          </ol>
        )}
      </div>
    </li>
  );
}

function NestedCard({
  card,
  region,
  coordinate,
  onAction,
}: {
  card: CardSnapshot;
  region: string;
  coordinate: string;
  onAction?: (click: TableCardClick) => void;
}) {
  return card.visible ? (
    <Card card={card} region={region} shadow={false} coordinate={coordinate} isChild onAction={onAction} />
  ) : (
    <CardHidden card={card} region={region} coordinate={coordinate} />
  );
}

export function RegionLabelBadges({ region }: { region: RegionSnapshot }) {
  return region.openHand ? (
    <Eye size={13} className="inline" />
  ) : region.hiddenHand ? (
    <EyeOff size={13} className="inline" />
  ) : null;
}
