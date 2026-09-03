import { useRef } from 'react';
import type { CardCount, CardDetail, Deck } from '../api/types';
import { useCardTooltips } from '../hooks/useCardTooltips';
import { DeckCardIcons } from './DeckCardIcons';

const sortByName = (cards: CardCount[]) => [...cards].sort((a, b) => a.name.localeCompare(b.name));

function CardList({ cards, details }: { cards: CardCount[]; details?: Record<string, CardDetail> }) {
  return (
    <ul className="deck-list">
      {sortByName(cards).map((card) => {
        const detail = details?.[String(card.id)];
        return (
          <li key={card.id} className="flex items-baseline gap-1">
            <span className="shrink-0 tabular-nums text-ink-muted">{card.count} x</span>
            <a
              className="card-name"
              data-card-id={card.id}
              data-secured={card.comments === 'playtest' ? 'true' : undefined}
            >
              {card.name}
            </a>
            {detail && <DeckCardIcons detail={detail} />}
          </li>
        );
      })}
    </ul>
  );
}

// Read-only deck view shared by the lobby registration preview, the in-game
// deck panel and the tournament registration preview. When a `details` map is
// supplied (card id → CardDetail, from the enriched deck endpoints) each row
// also renders its clan / discipline / path / cost icons; without it the view
// degrades to the plain name list it has always been.
export function DeckPreview({ deck, details }: { deck: Deck; details?: Record<string, CardDetail> }) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [deck]);

  return (
    <div ref={ref}>
      <div className="font-semibold text-xs text-ink-muted mt-1">Crypt ({deck.crypt.count})</div>
      <CardList cards={deck.crypt.cards} details={details} />
      <div className="font-semibold text-xs text-ink-muted mt-2">Library ({deck.library.count})</div>
      {deck.library.cards.map((section) => (
        <div key={section.type}>
          <div className="font-semibold text-xs text-ink-muted mt-1">
            {section.type} ({section.count})
          </div>
          <CardList cards={section.cards} details={details} />
        </div>
      ))}
    </div>
  );
}
