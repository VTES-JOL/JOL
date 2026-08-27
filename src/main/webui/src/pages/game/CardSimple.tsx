import type { CardSnapshot } from '../../api/types';
import { CardHidden } from './CardHidden';
import { Clan } from './Clan';

// Mirrors card-simple.jsp — used for ASH_HEAP/HAND/REMOVED_FROM_GAME/
// LIBRARY/RESEARCH regions, and the hand strip. No recursion — these regions
// never have nested cards in practice, matching legacy.
export function CardSimple({
  card,
  region,
  onClick,
}: {
  card: CardSnapshot;
  region: string;
  onClick?: () => void;
}) {
  if (!card.visible) return <CardHidden card={card} region={region} />;

  const regionStyle = region === 'REMOVED_FROM_GAME' ? 'opacity-50' : '';

  return (
    <li
      className={`flex-grow-1 list-group-item d-flex justify-content-between align-items-center p-1 shadow ${regionStyle}`}
      onClick={onClick}
      style={onClick ? { cursor: 'pointer' } : undefined}
    >
      <div className="mx-1 me-auto w-100 align-items-center">
        <div className="d-flex justify-content-between align-items-center w-100">
          <span>
            <a data-card-id={card.cardId} data-secured={card.playtest ? 'true' : undefined} className="card-name text-wrap">
              {card.name}
              {card.advanced && <i className="icon adv" />}
            </a>
          </span>
          <span className="d-flex gap-1 align-items-center">
            <span className="badge bg-light text-black shadow border-secondary-subtle">{card.label}</span>
            <span className={`icon card-type ${card.typeClass ?? ''}`} />
            {card.hasBlood && (
              <span>
                {(card.clanClasses ?? []).map((clan) => (
                  <Clan key={clan} value={clan} />
                ))}
              </span>
            )}
          </span>
        </div>
      </div>
    </li>
  );
}
