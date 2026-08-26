import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { CardHidden } from './CardHidden';

const COUNTER_STYLE = (hasLife: boolean, hasBlood: boolean, capacity: number, otherVisibleRegion: boolean) => {
  if (hasLife && otherVisibleRegion) return 'text-bg-success';
  if (hasBlood || capacity > 0) return 'text-bg-danger';
  return 'text-bg-secondary';
};

// region.jsp's RegionType.OTHER_VISIBLE_REGIONS — regions visible to
// opponents too (READY, ASH_HEAP, TORPOR, REMOVED_FROM_GAME), used only to
// pick the counter badge color (green="life" counters vs red="blood"/capacity).
const OTHER_VISIBLE_REGIONS = new Set(['READY', 'ASH_HEAP', 'TORPOR', 'REMOVED_FROM_GAME']);

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
  if (!card.visible) return <CardHidden card={card} region={region} />;

  const hasVotes = !!card.votes && card.votes !== '0';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const showCounterBadge = (card.counters ?? 0) > 0 || (card.capacity ?? 0) > 0;
  const counterStyle = COUNTER_STYLE(!!card.hasLife, !!card.hasBlood, card.capacity ?? 0, OTHER_VISIBLE_REGIONS.has(region));
  const regionStyle = region === 'TORPOR' ? 'opacity-75' : '';
  const contestedStyle = card.contested ? 'bg-warning-subtle' : '';

  return (
    <li
      className={`list-group-item d-flex justify-content-between align-items-baseline px-2 pt-2 pb-1 ${regionStyle} ${shadow ? 'shadow' : ''} ${contestedStyle}`}
      onClick={onAction ? () => onAction({ coordinate, card, isChild }) : undefined}
      style={onAction ? { cursor: 'pointer' } : undefined}
    >
      <div className="mx-1 me-auto w-100">
        <div className="d-flex justify-content-between">
          <div className="d-flex flex-column">
            <div className="d-flex align-items-center gap-1">
              <a data-card-id={card.cardId} data-secured={card.playtest ? 'true' : undefined} className="card-name text-wrap">
                {card.name}
                {card.advanced && <i className="icon adv" />}
              </a>
              {hasVotes && <span className="badge rounded-pill text-bg-warning">{card.votes}</span>}
              {card.contested && (
                <span className="badge text-bg-warning p-1 px-2" style={{ fontSize: '0.6rem' }}>
                  CONTESTED
                </span>
              )}
            </div>
            <div className="d-flex align-items-center gap-1">
              {(card.disciplines ?? []).map((disc) => (
                <span key={disc} className={`icon ${disc}`} />
              ))}
            </div>
            <div className="d-flex align-items-center gap-1">
              <span className="badge bg-light text-black shadow border border-secondary-subtle">{card.label}</span>
            </div>
          </div>
          <div className="d-flex flex-column">
            <div className="d-flex justify-content-end align-items-center gap-1">
              {card.infernal && <i className="bi bi-fire text-danger fs-6" />}
              {card.locked && (
                <span className="badge text-bg-dark p-1 px-2" style={{ fontSize: '0.6rem' }}>
                  LOCKED
                </span>
              )}
              {showCounterBadge && <span className={`badge rounded-pill shadow ${counterStyle}`}>{counterText}</span>}
            </div>
            <div className="d-flex justify-content-end align-items-center gap-1">
              {card.path && card.path !== 'NONE' && <span className={`path ${card.path.toLowerCase()}`} />}
              {card.sect && card.sect !== 'NONE' && <small className="sect">{card.sect}</small>}
              {card.clan && card.clan !== 'NONE' && <span className={`clan ${card.clan.toLowerCase()}`} title={card.clan} />}
            </div>
          </div>
        </div>
        {(card.cards?.length ?? 0) > 0 && (
          <ol className="list-group list-group-numbered ms-n3">
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
    <CardHidden card={card} region={region} />
  );
}

export function RegionLabelBadges({ region }: { region: RegionSnapshot }) {
  return region.openHand ? <i className="bi bi-eye" /> : region.hiddenHand ? <i className="bi bi-eye-slash" /> : null;
}
