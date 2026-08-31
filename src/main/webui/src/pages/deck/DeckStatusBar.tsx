import { TriangleAlert } from 'lucide-react';
import type { DeckValidity } from '../../api/types';
import { type DeckEntry, computeSummary, getBannedEntries } from './deckKit';
import { SummaryStats } from './SummaryStats';
import { FormatValidityBadges } from './FormatValidityBadges';

/**
 * Editor sub-header: live crypt/library/groups summary on the left; per-format
 * validity chips and a banned-card count on the right. Ported from jol-quarkus;
 * Tailwind Tailwind-based.
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
    <div className="px-3 py-1.5 border-b border-line/50 flex flex-wrap items-center justify-between gap-x-4 gap-y-1.5 min-h-[28px]">
      <div className="flex items-center min-w-0">
        {summary ? (
          <SummaryStats summary={summary} validate />
        ) : (
          <span className="text-[11px] text-ink-muted">Empty deck</span>
        )}
      </div>
      <div className="flex items-center gap-2 shrink-0">
        {hasValidity && <FormatValidityBadges validity={formatValidity} />}
        {banned.length > 0 && (
          <div className="flex items-center gap-1.5 px-2 py-1 rounded border border-blood/30 bg-blood/10 text-[11px] text-blood-soft">
            <TriangleAlert className="w-3 h-3 shrink-0" />
            <span>{banned.length} banned</span>
          </div>
        )}
      </div>
    </div>
  );
}
