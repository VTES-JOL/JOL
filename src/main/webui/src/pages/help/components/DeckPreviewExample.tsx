import type { CardCount, Deck } from '../../../api/types';
import { DeckPreview } from '../../../components/DeckPreview';

// Accepts the exact "4 x Card Name, 3 x Other Card (ADV)" text a player
// would type into the real deck editor — see deck-editor.mdx — so an author
// can build an example deck without knowing anything about the underlying
// Deck/CardCount shape.
function parseCardList(text: string): CardCount[] {
  return text
    .split(',')
    .map((part) => part.trim())
    .filter(Boolean)
    .map((part, i) => {
      const match = part.match(/^(\d+)\s*x?\s*(.+)$/i);
      return {
        id: i,
        name: match ? match[2].trim() : part,
        count: match ? parseInt(match[1], 10) : 1,
        comments: '',
      };
    });
}

const totalCount = (cards: CardCount[]) => cards.reduce((sum, c) => sum + c.count, 0);

export interface DeckPreviewExampleProps {
  name?: string;
  crypt: string;
  library: string;
  valid?: boolean;
}

// The deck editor's preview panel (crypt/library card list + Valid/Invalid
// badge), rendered through the real `components/DeckPreview.tsx`.
export function DeckPreviewExample({ name = 'Preview', crypt, library, valid = true }: DeckPreviewExampleProps) {
  const cryptCards = parseCardList(crypt);
  const libraryCards = parseCardList(library);
  const deck: Deck = {
    id: 'example',
    name,
    crypt: { count: totalCount(cryptCards), cards: cryptCards },
    library: { count: totalCount(libraryCards), cards: [{ type: 'Library', count: totalCount(libraryCards), cards: libraryCards }] },
    comments: '',
    player: '',
    author: '',
  };

  return (
    <div className="card shadow my-3" style={{ maxWidth: '26rem' }}>
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">{name}</span>
        {valid ? (
          <span className="badge bg-success-subtle text-success-emphasis border border-success-subtle">
            <i className="bi bi-check-circle me-1" />
            Valid
          </span>
        ) : (
          <span className="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">
            <i className="bi bi-exclamation-triangle me-1" />
            Invalid
          </span>
        )}
      </div>
      <div className="card-body p-2">
        <DeckPreview deck={deck} />
      </div>
    </div>
  );
}
