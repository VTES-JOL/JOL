import { AlertTriangle, CheckCircle2 } from 'lucide-react';
import type { CardCount, Deck } from '../../../api/types';
import { DeckView } from '../../../components/DeckView';
import { Card, CardHeader, CardBody } from '../../../components/ui/Card';
import { Badge } from '../../../components/ui/Badge';

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

// The deck editor's preview panel (grouped crypt/library card list +
// Valid/Invalid badge), rendered through the real `components/DeckView.tsx`.
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
    <Card className="my-3 max-w-md">
      <CardHeader className="flex items-center justify-between">
        <span className="text-sm font-semibold text-ink">{name}</span>
        {valid ? (
          <Badge variant="online">
            <CheckCircle2 size={12} className="mr-1" />
            Valid
          </Badge>
        ) : (
          <Badge variant="blood">
            <AlertTriangle size={12} className="mr-1" />
            Invalid
          </Badge>
        )}
      </CardHeader>
      <CardBody className="p-0">
        <DeckView deck={deck} />
      </CardBody>
    </Card>
  );
}
