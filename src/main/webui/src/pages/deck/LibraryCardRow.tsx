import { Fragment } from 'react';
import { TriangleAlert } from 'lucide-react';
import type { CardDetail } from '../../api/types';
import type { DeckEntry } from '../../components/ui/deckKit';
import { ClanIcon, CostIcon, DisciplineIcon, PathIcon } from '../../components/ui/icons';
import { CardRowShell } from './CardRowShell';

interface Props {
  entry: DeckEntry;
  detail?: CardDetail;
  onIncrement?: () => void;
  onDecrement?: () => void;
}

const SEP = 'jt:text-[11px] jt:text-ink-muted jt:leading-none jt:select-none';

export function LibraryCardRow({ entry, detail, onIncrement, onDecrement }: Props) {
  const orDiscs = detail?.orDisciplines ?? [];
  const andDiscs = detail?.andDisciplines ?? [];
  const reqClans = detail?.requirementClans ?? [];
  const reqPath = detail?.requirementPath ?? null;
  const hasRightIcons = reqPath != null || reqClans.length > 0 || orDiscs.length > 0 || andDiscs.length > 0;

  return (
    <CardRowShell entry={entry} onIncrement={onIncrement} onDecrement={onDecrement}>
      <div className="jt:flex jt:items-center jt:gap-1 jt:flex-1 jt:min-w-0">
        <span className={`jt:text-xs jt:truncate ${entry.banned ? 'jt:text-blood-soft' : 'jt:text-ink-secondary'}`}>
          {entry.name}
        </span>
        {entry.banned && <TriangleAlert className="jt:w-3 jt:h-3 jt:text-blood-soft jt:shrink-0" />}
        {detail?.poolCost != null && (
          <CostIcon type="pool" amount={detail.poolCost === -1 ? 'x' : detail.poolCost} size={20} />
        )}
        {detail?.bloodCost != null && (
          <CostIcon type="blood" amount={detail.bloodCost === -1 ? 'x' : detail.bloodCost} size={20} />
        )}
      </div>

      {hasRightIcons && (
        <div className="jt:flex jt:items-center jt:gap-0.5 jt:shrink-0 jt:ml-1">
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
