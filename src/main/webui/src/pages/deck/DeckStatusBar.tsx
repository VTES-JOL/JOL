import { TriangleAlert } from 'lucide-react';
import type { DeckValidity } from '../../api/types';
import { type DeckEntry, computeSummary, getBannedEntries } from '../../components/ui/deckKit';
import { SummaryStats } from '../../components/ui/SummaryStats';
import { FormatValidityBadges } from './FormatValidityBadges';

/**
 * Editor sub-header: live crypt/library/groups summary on the left; per-format
 * validity chips and a banned-card count on the right. Ported from jol-quarkus;
 * Tailwind `jt:` -prefixed.
 */
interface Props {
  entries: DeckEntry[];
  formatValidity: Record<string, DeckValidity>;
}

export function DeckStatusBar({ entries, formatValidity }: Props) {
  const summary = computeSummary(entries);
  const banned = getBannedEntries(entries);
  const hasValidity = Object.keys(formatValidity).length > 0;

  return (
    <div className="jt:px-3 jt:py-1.5 jt:border-b jt:border-line/50 jt:flex jt:flex-wrap jt:items-center jt:justify-between jt:gap-x-4 jt:gap-y-1.5 jt:min-h-[28px]">
      <div className="jt:flex jt:items-center jt:min-w-0">
        {summary ? (
          <SummaryStats summary={summary} validate />
        ) : (
          <span className="jt:text-[11px] jt:text-ink-muted">Empty deck</span>
        )}
      </div>
      <div className="jt:flex jt:items-center jt:gap-2 jt:shrink-0">
        {hasValidity && <FormatValidityBadges validity={formatValidity} />}
        {banned.length > 0 && (
          <div className="jt:flex jt:items-center jt:gap-1.5 jt:px-2 jt:py-1 jt:rounded jt:border jt:border-blood/30 jt:bg-blood/10 jt:text-[11px] jt:text-blood-soft">
            <TriangleAlert className="jt:w-3 jt:h-3 jt:shrink-0" />
            <span>{banned.length} banned</span>
          </div>
        )}
      </div>
    </div>
  );
}
