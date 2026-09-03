import { memo } from 'react';
import { Eye, EyeOff, Flame, Lock } from 'lucide-react';
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
//
// React.memo: `card` keeps its reference across an unrelated ['game', id]
// refetch (TanStack structural sharing), `onAction` is stable (Region's
// useCallback), the rest are primitives — so an opponent's action doesn't
// re-render cards whose state didn't change.
export const Card = memo(function Card({
  card,
  region,
  coordinate,
  isChild = false,
  onAction,
  onCardClick,
}: {
  card: CardSnapshot;
  region: string;
  coordinate: string;
  isChild?: boolean;
  onAction?: (click: TableCardClick) => void;
  // Overrides the default action-modal wiring for this row only — used to send
  // a controller's click on their own face-down card to the play-card modal.
  onCardClick?: () => void;
}) {
  if (!card.visible) {
    // A face-down card the viewer doesn't control: card back, plus any (rare)
    // still-visible children rendered through the normal per-node dispatch.
    if (card.faceDown && (card.cards?.length ?? 0) > 0) {
      return (
        <>
          <CardHidden card={card} region={region} coordinate={coordinate} />
          <ol className="list-none mt-1 ml-2 border-l border-line pl-1.5 divide-y divide-line/20">
            {card.cards!.map((nested, i) => (
              <NestedCard key={nested.id} card={nested} region={region} coordinate={`${coordinate}.${i + 1}`} onAction={onAction} />
            ))}
          </ol>
        </>
      );
    }
    return <CardHidden card={card} region={region} coordinate={coordinate} />;
  }

  const rowClick = onCardClick ?? (onAction ? () => onAction({ coordinate, card, isChild }) : undefined);
  const faceDownStyle = card.faceDown ? 'opacity-60 border-l-2 border-dashed border-ink-muted' : '';

  const hasVotes = !!card.votes && card.votes !== '0';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const showCounterBadge = (card.counters ?? 0) > 0 || (card.capacity ?? 0) > 0;
  const counterStyle = COUNTER_STYLE(!!card.hasLife, !!card.hasBlood, card.capacity ?? 0, OTHER_VISIBLE_REGIONS.has(region));
  const regionStyle = region === 'TORPOR' ? 'opacity-75' : '';
  const contestedStyle = card.contested ? 'bg-gold/15' : '';
  const rowPad = isChild ? 'px-2 py-1' : 'px-2 pt-2 pb-1';

  return (
    <li
      className={`flex justify-between items-start ${rowPad} ${regionStyle} ${contestedStyle} ${faceDownStyle} ${card.locked ? 'border-l-2 border-accent bg-accent/5' : ''}`}
      onClick={rowClick}
      style={rowClick ? { cursor: 'pointer' } : undefined}
    >
      <div className="mx-1 me-auto w-full">
        <div className="flex justify-between">
          <div className="flex flex-col">
            <div className="flex items-center gap-1">
              {card.locked && (
                <span
                  className="shrink-0 inline-flex items-center rounded bg-accent text-surface px-1 py-0.5"
                  title="Locked"
                >
                  <Lock size={11} strokeWidth={2.75} />
                </span>
              )}
              <span className="text-ink-muted text-xs tabular-nums select-all shrink-0">{coordinate}</span>
              <a data-card-id={card.cardId} data-secured={card.playtest ? 'true' : undefined} className="card-name text-wrap">
                {card.name}
                {card.advanced && <i className="icon adv" />}
              </a>
              {hasVotes && <span className={`${PILL} bg-gold text-surface`}>{card.votes}</span>}
              {card.contested && (
                <span className={`${PILL} bg-gold text-surface text-[0.7rem]`}>CONTESTED</span>
              )}
              {card.faceDown && (
                <span className={`${PILL} bg-hover text-ink-muted border border-dashed border-ink-muted text-[0.7rem]`} title="Only you can see this card">
                  FACE DOWN
                </span>
              )}
            </div>
            {(card.disciplines?.length ?? 0) > 0 && (
              <div className="flex items-center gap-1">
                {card.disciplines!.map((disc) => (
                  <span key={disc} className={`icon ${disc}`} />
                ))}
              </div>
            )}
            {card.label && (
              <div className="flex items-center gap-1">
                <span className={`${PILL} bg-hover text-ink border border-line`}>{card.label}</span>
              </div>
            )}
          </div>
          <div className="flex flex-col">
            <div className="flex justify-end items-center gap-1">
              {card.infernal && <Flame size={14} className="text-blood" />}
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
          // A minion's attached cards (equipment, retainers, action modifiers,
          // blood/counter stacks). Rendered as an indented branch with a left
          // rail so the group reads as one unit; the rail + indent compound
          // per level to form a depth ladder for deep stacks.
          <ol className="list-none mt-1 ml-2 border-l border-line pl-1.5 divide-y divide-line/20">
            {card.cards!.map((nested, i) => (
              <NestedCard key={nested.id} card={nested} region={region} coordinate={`${coordinate}.${i + 1}`} onAction={onAction} />
            ))}
          </ol>
        )}
      </div>
    </li>
  );
});

const NestedCard = memo(function NestedCard({
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
    <Card card={card} region={region} coordinate={coordinate} isChild onAction={onAction} />
  ) : (
    <CardHidden card={card} region={region} coordinate={coordinate} />
  );
});

export function RegionLabelBadges({ region }: { region: RegionSnapshot }) {
  return region.openHand ? (
    <Eye size={13} className="inline" />
  ) : region.hiddenHand ? (
    <EyeOff size={13} className="inline" />
  ) : null;
}
