import { useState, type MouseEvent } from 'react';
import { ChevronDown, ChevronRight, Lock } from 'lucide-react';
import type { CardSnapshot } from '../../api/types';
import type { MenuAnchor } from './CardContextMenu';
import { NestedCard, type TableCardClick } from './Card';

// A minion's attached cards (equipment, retainers, action modifiers,
// counter/blood stacks). Two forms:
//
//  - ≤ 4 attachments        → a wrapping chip strip, always shown.
//  - ≥ 5 attachments        → an "N attached ▸" header; the strip collapses so
//                             a wall vampire holding a dozen guns doesn't make
//                             its tile 14 rows tall. "Expand" swaps to the full
//                             indented list (Card.tsx's NestedCard) for reading
//                             a big stack or acting on one card, including any
//                             deeper nesting.
//
// A chip carries: a type-colour dot, the card name (or a card-back for a
// face-down card you don't control), a counter badge, a lock marker, and a "▸"
// when the card has its own attachments. Clicking a chip opens that card's
// action menu, same as a row in the expanded list.

const COLLAPSE_AT = 5;

function typeDot(typeClass?: string): string {
  const t = (typeClass ?? '').toLowerCase();
  if (t.includes('master')) return 'bg-gold';
  if (t.includes('modifier')) return 'bg-accent';
  if (t.includes('reaction')) return 'bg-arcane';
  if (t.includes('combat')) return 'bg-blood-soft';
  if (t.includes('ally') || t.includes('retainer')) return 'bg-online';
  if (t.includes('action') || t.includes('political') || t.includes('event')) return 'bg-blood';
  // equipment + anything unmapped
  return 'bg-ink-muted';
}

export function AttachedCards({
  cards,
  parentCoordinate,
  region,
  onAction,
}: {
  cards: CardSnapshot[];
  parentCoordinate: string;
  region: string;
  onAction?: (click: TableCardClick, anchor: MenuAnchor) => void;
}) {
  const many = cards.length >= COLLAPSE_AT;
  const [expanded, setExpanded] = useState(false);
  // ≥5: default to the chip strip (dense); "expanded" swaps to the full list.
  const [listMode, setListMode] = useState(false);

  if (cards.length === 0) return null;

  const openMenu = (card: CardSnapshot, coordinate: string) => (e: MouseEvent) => {
    e.stopPropagation();
    onAction?.({ coordinate, card, isChild: true }, { x: e.clientX, y: e.clientY });
  };

  if (listMode) {
    return (
      <div className="mt-1.5 ml-1">
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            setListMode(false);
          }}
          className="mb-1 inline-flex items-center gap-1 text-[0.65rem] font-semibold uppercase tracking-wide text-ink-muted hover:text-ink"
        >
          <ChevronDown size={12} /> {cards.length} attached
        </button>
        <ol className="list-none divide-y divide-line/20 border-l border-line pl-1.5">
          {cards.map((nested, i) => (
            <NestedCard
              key={nested.id}
              card={nested}
              region={region}
              coordinate={`${parentCoordinate}.${i + 1}`}
              onAction={onAction}
            />
          ))}
        </ol>
      </div>
    );
  }

  const showChips = !many || expanded;

  return (
    <div className="mt-1.5">
      {many && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            setExpanded((v) => !v);
          }}
          className="inline-flex items-center gap-1 text-[0.65rem] font-semibold uppercase tracking-wide text-ink-muted hover:text-ink"
        >
          {expanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
          {cards.length} attached
        </button>
      )}
      {showChips && (
        <div className="mt-1 flex flex-wrap gap-1">
          {cards.map((card, i) => {
            const coordinate = `${parentCoordinate}.${i + 1}`;
            const count =
              (card.counters ?? 0) > 0
                ? `${card.counters}${(card.capacity ?? 0) > 0 ? `/${card.capacity}` : ''}`
                : null;
            const backOnly = !card.visible;
            const hasChildren = (card.cards?.length ?? 0) > 0;
            return (
              <button
                key={card.id}
                type="button"
                onClick={openMenu(card, coordinate)}
                title={backOnly ? 'Face-down attachment' : (card.name ?? undefined)}
                className={`inline-flex max-w-[11rem] items-center gap-1 rounded border px-1.5 py-0.5 text-[0.7rem] text-ink-secondary hover:border-ink hover:text-ink ${
                  card.faceDown ? 'border-dashed border-ink-muted' : 'border-line'
                } bg-hover/60`}
              >
                <span className={`h-1.5 w-1.5 shrink-0 rounded-[2px] ${typeDot(card.typeClass)}`} />
                {backOnly ? (
                  <span className="text-ink-muted">face-down</span>
                ) : (
                  <a data-card-id={card.cardId} className="card-name truncate">
                    {card.name}
                  </a>
                )}
                {card.locked && <Lock size={9} className="shrink-0 text-blood-soft" />}
                {count && (
                  <span className="shrink-0 rounded bg-blood px-1 text-[0.6rem] font-semibold text-white tabular-nums">
                    {count}
                  </span>
                )}
                {hasChildren && <span className="shrink-0 text-ink-muted">▸</span>}
              </button>
            );
          })}
          {many && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                setListMode(true);
              }}
              className="rounded border border-line px-1.5 py-0.5 text-[0.7rem] text-ink-muted hover:border-ink hover:text-ink"
            >
              list ▾
            </button>
          )}
        </div>
      )}
    </div>
  );
}
