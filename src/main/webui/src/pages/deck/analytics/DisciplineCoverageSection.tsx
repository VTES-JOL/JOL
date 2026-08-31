import type { CardDetail } from '../../../api/types';
import type { DeckEntry } from '../deckKit';
import { DisciplineIcon } from '../../../components/ui/icons';
import { BarRow } from './BarRow';
import { SectionHeader } from './SectionHeader';

interface Props {
  entries: DeckEntry[];
  detailMap: Map<string, CardDetail>;
}

/**
 * How many crypt cards (×count) provide each discipline, cross-referenced with
 * how many library cards need it. Inferior and superior are grouped together
 * (both keyed uppercase), and only disciplines the library actually requires
 * are shown.
 */
export function DisciplineCoverageSection({ entries, detailMap }: Props) {
  const cryptEntries = entries.filter((e) => e.isCrypt);
  const libEntries = entries.filter((e) => !e.isCrypt);
  if (cryptEntries.length === 0) return null;

  const cryptDiscs = new Map<string, number>();
  for (const entry of cryptEntries) {
    for (const d of detailMap.get(entry.cardId)?.disciplines ?? []) {
      const key = d.toUpperCase();
      cryptDiscs.set(key, (cryptDiscs.get(key) ?? 0) + entry.count);
    }
  }

  const libDiscs = new Map<string, number>();
  for (const entry of libEntries) {
    const d = detailMap.get(entry.cardId);
    if (!d) continue;
    for (const disc of [...d.orDisciplines, ...d.andDisciplines]) {
      const key = disc.toUpperCase();
      libDiscs.set(key, (libDiscs.get(key) ?? 0) + entry.count);
    }
  }

  if (cryptDiscs.size === 0 || libDiscs.size === 0) return null;

  const rows = [...cryptDiscs.entries()].filter(([disc]) => libDiscs.has(disc)).sort((a, b) => b[1] - a[1]);
  if (rows.length === 0) return null;
  const cryptMax = rows[0][1];

  return (
    <div className="jt:border-b jt:border-line/50">
      <SectionHeader title="Discipline Coverage" subtitle="Crypt cards · library cards needed in ( )" />
      <div className="jt:py-1">
        {rows.map(([disc, cryptCount]) => {
          const libCount = libDiscs.get(disc) ?? 0;
          const label = (
            <span className="jt:flex jt:items-center jt:gap-1">
              <DisciplineIcon discipline={disc} size={12} />
              <span>{disc}</span>
              {libCount > 0 && <span className="jt:text-ink-muted">({libCount})</span>}
            </span>
          );
          return <BarRow key={disc} label={label} count={cryptCount} max={cryptMax} />;
        })}
      </div>
    </div>
  );
}
