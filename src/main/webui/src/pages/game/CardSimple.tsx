import type { KeyboardEvent, MouseEvent } from 'react';
import type { CardSnapshot } from '../../api/types';
import { CardHidden } from './CardHidden';
import { Clan } from './Clan';

// Mirrors card-simple.jsp — used for ASH_HEAP/HAND/REMOVED_FROM_GAME/
// LIBRARY/RESEARCH regions, and the hand strip. No recursion — these regions
// never have nested cards in practice, matching legacy.
export function CardSimple({
  card,
  region,
  coordinate,
  onClick,
  onContextMenu,
}: {
  card: CardSnapshot;
  region: string;
  coordinate: string;
  onClick?: (e: MouseEvent) => void;
  onContextMenu?: (e: MouseEvent) => void;
}) {
  if (!card.visible)
    return <CardHidden card={card} region={region} coordinate={coordinate} onClick={onClick} onContextMenu={onContextMenu} />;

  const regionStyle = region === 'REMOVED_FROM_GAME' ? 'opacity-50' : '';
  const faceDownStyle = card.faceDown ? 'opacity-60' : '';

  // F10: keyboard path for the click-to-act tile. Callers position the
  // action menu off e.clientX/clientY, so a keyboard activation fakes those
  // from the tile's own centre.
  const onKeyDown = onClick
    ? (e: KeyboardEvent<HTMLLIElement>) => {
        if (e.key !== 'Enter' && e.key !== ' ') return;
        e.preventDefault();
        const r = e.currentTarget.getBoundingClientRect();
        onClick({
          clientX: r.left + r.width / 2,
          clientY: r.top + r.height / 2,
          stopPropagation: () => {},
          preventDefault: () => {},
        } as unknown as MouseEvent);
      }
    : undefined;

  return (
    <li
      className={`flex justify-between items-center p-1 ${regionStyle} ${faceDownStyle} ${
        onClick ? 'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent' : ''
      }`}
      onClick={onClick}
      onContextMenu={onContextMenu}
      onKeyDown={onKeyDown}
      style={onClick ? { cursor: 'pointer' } : undefined}
      {...(onClick ? { role: 'button' as const, tabIndex: 0 } : {})}
    >
      <div className="mx-1 me-auto w-full">
        <div className="flex justify-between items-center w-full">
          <span className="flex items-center gap-1">
            <span className="text-ink-muted text-xs tabular-nums select-all shrink-0">{coordinate}</span>
            <a data-card-id={card.cardId} data-secured={card.playtest ? 'true' : undefined} className="card-name text-wrap">
              {card.name}
              {card.advanced && <i className="icon adv" />}
            </a>
          </span>
          <span className="flex gap-1 items-center">
            {card.faceDown && (
              <span
                className="inline-flex items-center rounded bg-hover text-ink-muted border border-dashed border-ink-muted px-1.5 text-[0.7rem]"
                title="Only you can see this card"
              >
                FACE DOWN
              </span>
            )}
            {card.label && (
              <span className="inline-flex items-center rounded bg-hover text-ink border border-line px-1.5 text-xs">
                {card.label}
              </span>
            )}
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
