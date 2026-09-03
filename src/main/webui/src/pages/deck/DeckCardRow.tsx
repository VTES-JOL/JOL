import type { CardDetail } from '../../api/types';
import type { DeckEntry } from './deckKit';
import { CryptCardRow } from './CryptCardRow';
import { LibraryCardRow } from './LibraryCardRow';

interface Props {
  entry: DeckEntry;
  detail?: CardDetail;
  onIncrement?: () => void;
  onDecrement?: () => void;
  /** Render card names as hoverable `a.card-name` links (read-only DeckView). */
  linkCards?: boolean;
}

export function DeckCardRow({ entry, detail, onIncrement, onDecrement, linkCards }: Props) {
  return entry.isCrypt ? (
    <CryptCardRow
      entry={entry}
      detail={detail}
      onIncrement={onIncrement}
      onDecrement={onDecrement}
      linkCards={linkCards}
    />
  ) : (
    <LibraryCardRow
      entry={entry}
      detail={detail}
      onIncrement={onIncrement}
      onDecrement={onDecrement}
      linkCards={linkCards}
    />
  );
}
