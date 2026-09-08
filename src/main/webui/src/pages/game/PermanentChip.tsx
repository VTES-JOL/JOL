import { memo, type MouseEvent } from 'react';
import { Lock } from 'lucide-react';
import type { CardSnapshot } from '../../api/types';
import type { MenuAnchor } from './CardContextMenu';
import type { QuickKind, TableCardClick } from './Card';
import { CardHidden } from './CardHidden';

// N2 — READY holds non-minion cards too (masters, locations, powerbases:
// Powerbase: Montreal, Information Highway, Veil of Darkness). They don't want
// a capacity ring or a discipline row, so they render as a compact one-line
// chip rather than a MinionTile.
const CHIP = 'inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[0.7rem] font-semibold';
const TAP = 'inline-flex items-center justify-center p-2.5 md:p-1';

export const PermanentChip = memo(function PermanentChip({
  card,
  region,
  coordinate,
  onAction,
  onQuick,
  onCardClick,
}: {
  card: CardSnapshot;
  region: string;
  coordinate: string;
  onAction?: (click: TableCardClick, anchor: MenuAnchor) => void;
  onQuick?: (click: TableCardClick, kind: QuickKind) => void;
  onCardClick?: () => void;
}) {
  if (!card.visible) {
    return <CardHidden card={card} region={region} coordinate={coordinate} />;
  }

  const openMenu = onAction
    ? (e: MouseEvent) => {
        e.stopPropagation();
        onAction({ coordinate, card, isChild: false }, { x: e.clientX, y: e.clientY });
      }
    : undefined;
  const rowClick = onCardClick
    ? (e: MouseEvent) => {
        e.stopPropagation();
        onCardClick();
      }
    : openMenu;
  const rowContextMenu =
    onCardClick || !openMenu
      ? undefined
      : (e: MouseEvent) => {
          e.preventDefault();
          openMenu(e);
        };
  const quick = onQuick
    ? (kind: QuickKind) => (e: MouseEvent) => {
        e.stopPropagation();
        onQuick({ coordinate, card, isChild: false }, kind);
      }
    : null;

  return (
    <li
      className={`group flex list-none items-center gap-1.5 self-start rounded border px-2 py-1 text-sm ${
        card.contested ? 'border-gold bg-gold/10' : card.locked ? 'border-accent bg-accent/5' : 'border-line-accent bg-hover/40'
      }`}
      onClick={rowClick}
      onContextMenu={rowContextMenu}
      style={rowClick ? { cursor: 'pointer' } : undefined}
    >
      <span className="shrink-0 select-all text-xs tabular-nums text-ink-muted">{coordinate}</span>
      <a
        data-card-id={card.cardId}
        data-secured={card.playtest ? 'true' : undefined}
        className={`card-name min-w-0 flex-1 truncate ${card.faceDown ? 'opacity-60' : ''}`}
      >
        {card.name}
        {card.advanced && <i className="icon adv" />}
      </a>
      {card.contested && <span className={`${CHIP} bg-gold text-surface`}>CONTESTED</span>}
      {card.faceDown && (
        <span className={`${CHIP} border border-dashed border-ink-muted text-ink-muted`} title="Only you can see this card">
          FACE DOWN
        </span>
      )}
      {card.label && <span className={`${CHIP} border border-line bg-hover text-ink`}>{card.label}</span>}
      {(card.counters ?? 0) > 0 && (
        <span className="rounded-full bg-blood px-2 py-0.5 text-xs font-medium text-surface tabular-nums">{card.counters}</span>
      )}
      {card.locked ? (
        quick ? (
          <button
            type="button"
            aria-label={`Unlock ${card.name ?? 'card'}`}
            title="Unlock"
            onClick={quick('unlock')}
            className={`${TAP} shrink-0 rounded bg-accent text-surface hover:bg-accent-dim`}
          >
            <Lock size={11} strokeWidth={2.75} />
          </button>
        ) : (
          <span className={`${TAP} shrink-0 rounded bg-accent text-surface`} title="Locked">
            <Lock size={11} strokeWidth={2.75} />
          </span>
        )
      ) : (
        quick && (
          <button
            type="button"
            aria-label={`Lock ${card.name ?? 'card'}`}
            title="Lock"
            onClick={quick('lock')}
            className={`${TAP} shrink-0 rounded border border-line-accent text-ink-muted opacity-0 transition-opacity hover:border-ink hover:text-ink focus-visible:opacity-100 group-hover:opacity-100`}
          >
            <Lock size={11} />
          </button>
        )
      )}
    </li>
  );
});
