import type { DeckEntry } from '../deckKit';
import { BarRow } from './BarRow';
import { SectionHeader } from './SectionHeader';

interface Props {
  entries: DeckEntry[];
}

export function LibraryTypeSection({ entries }: Props) {
  const libEntries = entries.filter((e) => !e.isCrypt);
  if (libEntries.length === 0) return null;

  // Multi-type cards (e.g. ["Action","Combat"]) contribute to each type bucket.
  const typeCounts = new Map<string, number>();
  for (const entry of libEntries) {
    for (const type of entry.types) {
      typeCounts.set(type, (typeCounts.get(type) ?? 0) + entry.count);
    }
  }

  const rows = [...typeCounts.entries()].sort((a, b) => b[1] - a[1]);
  const max = rows[0]?.[1] ?? 1;
  const total = libEntries.reduce((s, e) => s + e.count, 0);

  return (
    <div className="jt:border-b jt:border-line/50">
      <SectionHeader title="Library Types" subtitle={`${total} cards · multi-type counted in each`} />
      <div className="jt:py-1">
        {rows.map(([type, count]) => (
          <BarRow key={type} label={type} count={count} max={max} />
        ))}
      </div>
    </div>
  );
}
