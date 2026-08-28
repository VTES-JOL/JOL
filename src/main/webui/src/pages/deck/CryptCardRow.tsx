import { TriangleAlert } from 'lucide-react';
import type { CardDetail } from '../../api/types';
import type { DeckEntry } from '../../components/ui/deckKit';
import { ClanIcon, DisciplineIcon, PathIcon } from '../../components/ui/icons';
import { CardRowShell } from './CardRowShell';

interface Props {
  entry: DeckEntry;
  detail?: CardDetail;
  onIncrement?: () => void;
  onDecrement?: () => void;
}

function groupHint(entry: DeckEntry): string | null {
  const parts: string[] = [];
  if (entry.group && entry.group !== 'ANY') parts.push(`G${entry.group}`);
  if (entry.advanced) parts.push('ADV');
  return parts.length > 0 ? parts.join(' ') : null;
}

export function CryptCardRow({ entry, detail, onIncrement, onDecrement }: Props) {
  return (
    <CardRowShell entry={entry} onIncrement={onIncrement} onDecrement={onDecrement}>
      <span className="jt:w-4 jt:shrink-0 jt:flex jt:items-center jt:justify-center">
        {detail?.path && <PathIcon path={detail.path} size={16} />}
      </span>
      <span className="jt:w-4 jt:shrink-0 jt:flex jt:items-center jt:justify-center">
        {detail?.clan && <ClanIcon clan={detail.clan} size={16} />}
      </span>

      <div className="jt:flex jt:items-center jt:gap-1 jt:flex-1 jt:min-w-0">
        <span className={`jt:text-xs jt:truncate ${entry.banned ? 'jt:text-blood-soft' : 'jt:text-ink-secondary'}`}>
          {entry.name}
        </span>
        {groupHint(entry) && (
          <span className="jt:text-[11px] jt:text-ink-muted jt:shrink-0 jt:tabular-nums">{groupHint(entry)}</span>
        )}
        {entry.banned && <TriangleAlert className="jt:w-3 jt:h-3 jt:text-blood-soft jt:shrink-0" />}
      </div>

      {detail && detail.disciplines.length > 0 && (
        <div className="jt:flex jt:items-center jt:gap-0.5 jt:shrink-0 jt:ml-1">
          {detail.disciplines.map((d, i) => (
            <DisciplineIcon key={i} discipline={d} size={16} />
          ))}
        </div>
      )}

      {detail?.capacity != null && (
        <span className="jt:ml-1 jt:shrink-0 jt:inline-flex jt:items-center jt:justify-center jt:min-w-[18px] jt:h-[18px] jt:px-1 jt:rounded jt:bg-blood/15 jt:text-blood-soft jt:text-[11px] jt:font-semibold jt:tabular-nums">
          {detail.capacity}
        </span>
      )}
    </CardRowShell>
  );
}
