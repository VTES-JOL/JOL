import { TriangleAlert } from 'lucide-react';
import type { DeckInfoBean } from '../../api/types';
import { Badge } from '../../components/ui/Badge';

/**
 * A row in the deck list. Adapted from jol-quarkus's DeckListItem for jol's
 * `DeckInfoBean` (name + game-format tags + first comment line; no stored
 * summary/timestamp yet). Tailwind Tailwind-based.
 */
interface Props {
  deck: DeckInfoBean;
  selected?: boolean;
  onClick?: () => void;
}

export function DeckListItem({ deck, selected = false, onClick }: Props) {
  const legacy = deck.deckFormat === 'LEGACY';
  return (
    <div
      role="button"
      tabIndex={0}
      onClick={onClick}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') onClick?.();
      }}
      className={[
        'w-full text-left px-4 py-3 border-b border-line/50 transition-colors cursor-pointer',
        selected
          ? 'bg-accent/10 border-l-2 border-l-accent'
          : 'hover:bg-hover border-l-2 border-l-transparent',
      ].join(' ')}
    >
      <div className="flex items-center gap-1.5 min-w-0">
        {legacy && (
          <TriangleAlert
            className="w-3 h-3 text-away shrink-0"
            aria-label="Legacy format — resave to upgrade"
          />
        )}
        <span
          className={`text-sm font-semibold truncate ${selected ? 'text-accent' : 'text-ink'}`}
        >
          {deck.name}
        </span>
      </div>
      {deck.gameFormats.length > 0 && (
        <div className="flex flex-wrap items-center gap-1 mt-1.5">
          {deck.gameFormats.map((f) => (
            <Badge key={f} variant="format">
              {f}
            </Badge>
          ))}
        </div>
      )}
      {deck.comments && (
        <p className="text-xs text-ink-muted mt-0.5 line-clamp-1 leading-relaxed">{deck.comments}</p>
      )}
    </div>
  );
}
