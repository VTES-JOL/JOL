import { TriangleAlert } from 'lucide-react';
import type { CardDetail } from '../../api/types';
import type { DeckEntry } from './deckKit';
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
      <span className="w-4 shrink-0 flex items-center justify-center">
        {detail?.path && <PathIcon path={detail.path} size={16} />}
      </span>
      <span className="w-4 shrink-0 flex items-center justify-center">
        {detail?.clan && <ClanIcon clan={detail.clan} size={16} />}
      </span>

      <div className="flex items-center gap-1 flex-1 min-w-0">
        <span className={`text-xs truncate ${entry.banned ? 'text-blood-soft' : 'text-ink-secondary'}`}>
          {entry.name}
        </span>
        {groupHint(entry) && (
          <span className="text-[11px] text-ink-muted shrink-0 tabular-nums">{groupHint(entry)}</span>
        )}
        {entry.banned && <TriangleAlert className="w-3 h-3 text-blood-soft shrink-0" />}
      </div>

      {detail && detail.disciplines.length > 0 && (
        <div className="flex items-center gap-0.5 shrink-0 ml-1">
          {detail.disciplines.map((d, i) => (
            <DisciplineIcon key={i} discipline={d} size={16} />
          ))}
        </div>
      )}

      {detail?.capacity != null && (
        <span className="ml-1 shrink-0 inline-flex items-center justify-center min-w-[18px] h-[18px] px-1 rounded bg-blood/15 text-blood-soft text-[11px] font-semibold tabular-nums">
          {detail.capacity}
        </span>
      )}
    </CardRowShell>
  );
}
