import type { CardDetail } from '../../api/types';
import type { DeckEntry } from './deckKit';
import { Panel } from '../../components/ui/Panel';
import { OpeningHandSection } from './analytics/OpeningHandSection';
import { LibraryTypeSection } from './analytics/LibraryTypeSection';
import { LibraryCostSection } from './analytics/LibraryCostSection';
import { CryptCapacityCurve } from './analytics/CryptCapacityCurve';
import { ClanDistributionSection } from './analytics/ClanDistributionSection';
import { DisciplineCoverageSection } from './analytics/DisciplineCoverageSection';

/**
 * Right-hand analytics panel. Ported from jol-quarkus; each section is an
 * independent pure component. Progressive disclosure by panel width (applied
 * here, not in the sections):
 *   base:  Opening Hand + Library Types
 *   xl+:   + Library Costs + Crypt Capacity
 *   2xl+:  + Clan Distribution + Discipline Coverage
 */
interface Props {
  entries: DeckEntry[];
  detailMap: Map<string, CardDetail>;
}

export function DeckAnalyticsPanel({ entries, detailMap }: Props) {
  return (
    <Panel title="Analytics" headerClassName="max-lg:hidden">
      {entries.length === 0 ? (
        <div className="flex-1 flex items-center justify-center p-6 text-xs text-ink-muted text-center leading-relaxed">
          Add cards to see analytics.
        </div>
      ) : (
        <div className="overflow-y-auto flex-1 min-h-0">
          <OpeningHandSection entries={entries} />
          <LibraryTypeSection entries={entries} />

          <div className="hidden xl:block">
            <LibraryCostSection entries={entries} detailMap={detailMap} />
            <CryptCapacityCurve entries={entries} detailMap={detailMap} />
          </div>

          <div className="hidden 2xl:block">
            <ClanDistributionSection entries={entries} detailMap={detailMap} />
            <DisciplineCoverageSection entries={entries} detailMap={detailMap} />
          </div>
        </div>
      )}
    </Panel>
  );
}
