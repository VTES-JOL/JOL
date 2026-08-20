import { useRef } from 'react';
import type { CardCount, Deck } from '../../api/types';
import { useCardTooltips } from '../../hooks/useCardTooltips';

const sortByName = (cards: CardCount[]) => [...cards].sort((a, b) => a.name.localeCompare(b.name));

function CardList({ cards }: { cards: CardCount[] }) {
  return (
    <ul className="deck-list">
      {sortByName(cards).map((card) => (
        <li key={card.id}>
          {card.count} x{' '}
          <a
            className="card-name"
            data-card-id={card.id}
            data-secured={card.comments === 'playtest' ? 'true' : undefined}
          >
            {card.name}
          </a>
        </li>
      ))}
    </ul>
  );
}

// Mirrors ds.js's renderDeck() for the "#deckPreview"/plain-preview case
// (the "#gameDeck" variant's extra deck-name header and comments box belong
// to the not-yet-converted game page).
export function DeckPreview({ deck }: { deck: Deck }) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [deck]);

  return (
    <div ref={ref}>
      <div className="fw-semibold small text-muted mt-1">Crypt ({deck.crypt.count})</div>
      <CardList cards={deck.crypt.cards} />
      <div className="fw-semibold small text-muted mt-2">Library ({deck.library.count})</div>
      {deck.library.cards.map((section) => (
        <div key={section.type}>
          <div className="fw-semibold small text-muted mt-1">
            {section.type} ({section.count})
          </div>
          <CardList cards={section.cards} />
        </div>
      ))}
    </div>
  );
}
