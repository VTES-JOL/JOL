import type { CardDetail } from '../../api/types';
import type { DeckEntry } from '../../components/ui/deckKit';
import { CryptCardRow } from './CryptCardRow';
import { LibraryCardRow } from './LibraryCardRow';

interface Props {
  entry: DeckEntry;
  detail?: CardDetail;
  onIncrement?: () => void;
  onDecrement?: () => void;
}

export function DeckCardRow({ entry, detail, onIncrement, onDecrement }: Props) {
  return entry.isCrypt ? (
    <CryptCardRow entry={entry} detail={detail} onIncrement={onIncrement} onDecrement={onDecrement} />
  ) : (
    <LibraryCardRow entry={entry} detail={detail} onIncrement={onIncrement} onDecrement={onDecrement} />
  );
}
