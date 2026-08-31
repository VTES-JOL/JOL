import { Fragment } from 'react';
import { TriangleAlert } from 'lucide-react';
import type { CardDetail } from '../../api/types';
import type { DeckEntry } from './deckKit';
import { ClanIcon, CostIcon, DisciplineIcon, PathIcon } from '../../components/ui/icons';
import { CardRowShell } from './CardRowShell';

interface Props {
  entry: DeckEntry;
  detail?: CardDetail;
  onIncrement?: () => void;
  onDecrement?: () => void;
}

const SEP = 'text-[11px] text-ink-muted leading-none select-none';

export function LibraryCardRow({ entry, detail, onIncrement, onDecrement }: Props) {
  const orDiscs = detail?.orDisciplines ?? [];
  const andDiscs = detail?.andDisciplines ?? [];
  const reqClans = detail?.requirementClans ?? [];
  const reqPath = detail?.requirementPath ?? null;
  const hasRightIcons = reqPath != null || reqClans.length > 0 || orDiscs.length > 0 || andDiscs.length > 0;

  return (
    <CardRowShell entry={entry} onIncrement={onIncrement} onDecrement={onDecrement}>
      <div className="flex items-center gap-1 flex-1 min-w-0">
        <span className={`text-xs truncate ${entry.banned ? 'text-blood-soft' : 'text-ink-secondary'}`}>
          {entry.name}
        </span>
        {entry.banned && <TriangleAlert className="w-3 h-3 text-blood-soft shrink-0" />}
        {detail?.poolCost != null && (
          <CostIcon type="pool" amount={detail.poolCost === -1 ? 'x' : detail.poolCost} size={20} />
        )}
        {detail?.bloodCost != null && (
          <CostIcon type="blood" amount={detail.bloodCost === -1 ? 'x' : detail.bloodCost} size={20} />
        )}
      </div>

      {hasRightIcons && (
        <div className="flex items-center gap-0.5 shrink-0 ml-1">
          {reqPath && <PathIcon path={reqPath} size={16} />}
          {reqClans.map((c, i) => (
            <ClanIcon key={i} clan={c} size={16} />
          ))}
          {orDiscs.map((d, i) => (
            <Fragment key={i}>
              {i > 0 && <span className={SEP}>/</span>}
              <DisciplineIcon discipline={d} size={16} />
            </Fragment>
          ))}
          {andDiscs.map((d, i) => (
            <Fragment key={i}>
              {i > 0 && <span className={SEP}>&amp;</span>}
              <DisciplineIcon discipline={d} size={16} />
            </Fragment>
          ))}
        </div>
      )}
    </CardRowShell>
  );
}
