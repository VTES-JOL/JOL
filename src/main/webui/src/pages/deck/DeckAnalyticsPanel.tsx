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
    <Panel title="Analytics">
      {entries.length === 0 ? (
        <div className="jt:flex-1 jt:flex jt:items-center jt:justify-center jt:p-6 jt:text-xs jt:text-ink-muted jt:text-center jt:leading-relaxed">
          Add cards to see analytics.
        </div>
      ) : (
        <div className="jt:overflow-y-auto jt:flex-1 jt:min-h-0">
          <OpeningHandSection entries={entries} />
          <LibraryTypeSection entries={entries} />

          <div className="jt:hidden jt:xl:block">
            <LibraryCostSection entries={entries} detailMap={detailMap} />
            <CryptCapacityCurve entries={entries} detailMap={detailMap} />
          </div>

          <div className="jt:hidden jt:2xl:block">
            <ClanDistributionSection entries={entries} detailMap={detailMap} />
            <DisciplineCoverageSection entries={entries} detailMap={detailMap} />
          </div>
        </div>
      )}
    </Panel>
  );
}
