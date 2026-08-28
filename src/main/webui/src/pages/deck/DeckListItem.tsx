import { TriangleAlert } from 'lucide-react';
import type { DeckInfoBean } from '../../api/types';
import { Badge } from '../../components/ui/Badge';

/**
 * A row in the deck list. Adapted from jol-quarkus's DeckListItem for jol's
 * `DeckInfoBean` (name + game-format tags + first comment line; no stored
 * summary/timestamp yet). Tailwind `jt:` -prefixed.
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
        'jt:w-full jt:text-left jt:px-4 jt:py-3 jt:border-b jt:border-line/50 jt:transition-colors jt:cursor-pointer',
        selected
          ? 'jt:bg-accent/10 jt:border-l-2 jt:border-l-accent'
          : 'jt:hover:bg-hover jt:border-l-2 jt:border-l-transparent',
      ].join(' ')}
    >
      <div className="jt:flex jt:items-center jt:gap-1.5 jt:min-w-0">
        {legacy && (
          <TriangleAlert
            className="jt:w-3 jt:h-3 jt:text-away jt:shrink-0"
            aria-label="Legacy format — resave to upgrade"
          />
        )}
        <span
          className={`jt:text-sm jt:font-semibold jt:truncate ${selected ? 'jt:text-accent' : 'jt:text-ink'}`}
        >
          {deck.name}
        </span>
      </div>
      {deck.gameFormats.length > 0 && (
        <div className="jt:flex jt:flex-wrap jt:items-center jt:gap-1 jt:mt-1.5">
          {deck.gameFormats.map((f) => (
            <Badge key={f} variant="format">
              {f}
            </Badge>
          ))}
        </div>
      )}
      {deck.comments && (
        <p className="jt:text-xs jt:text-ink-muted jt:mt-0.5 jt:line-clamp-1 jt:leading-relaxed">{deck.comments}</p>
      )}
    </div>
  );
}
