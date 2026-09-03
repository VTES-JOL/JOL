import { useRef, useState } from 'react';
import type { JolStats as JolStatsDto } from '../../../api/types';
import { CountryFlag } from '../../../components/CountryFlag';
import { useSimpleTooltips } from '../../../hooks/useSimpleTooltips';
import { displayDeckName } from '../../../utils/deckName';
import { SortableStatsTable, type StatsColumn } from './SortableStatsTable';
import { useStatsQuery, type StatsFilters } from './useStatsQuery';

const BEST_NATION_PATTERN = /^([A-Z]{2})\s*\((\d+)\s*GW\)$/;
const GW_SUFFIX = /\s*\((\d+)\s*GW\)\s*$/;

interface Row extends JolStatsDto, Record<string, unknown> {
  month: string;
}

const dash = <span className="text-ink-muted">–</span>;

function BestNation({ value }: { value: string }) {
  const match = value && value !== '-' ? value.match(BEST_NATION_PATTERN) : null;
  if (!match) return dash;
  const [, code, gw] = match;
  return (
    <>
      <CountryFlag code={code} withName /> ({gw} GW)
    </>
  );
}

/**
 * "Player/Deck of the Month" comes as a ` / `-joined tie list with a trailing
 * `(N GW)`. Collapse long ties to "N-way tie" (full list on hover) so one tall
 * cell doesn't stretch the whole row, and drop the "no deck name" sentinel
 * from deck ties.
 */
function TieCell({ value, isDeck = false }: { value: string; isDeck?: boolean }) {
  if (!value || value === '-') return dash;
  const m = value.match(GW_SUFFIX);
  const gw = m ? m[1] : null;
  const body = m ? value.slice(0, m.index) : value;
  let names = body.split(' / ').map((s) => s.trim()).filter(Boolean);
  if (isDeck) names = names.map(displayDeckName).filter((n): n is string => !!n);
  if (names.length === 0) return dash;

  const joined = names.join(' / ');
  const full = gw ? `${joined} (${gw} GW)` : joined;
  return (
    <span className="block max-w-[15rem] truncate" title={full}>
      {names.length > 2 ? `${names.length}-way tie` : joined}
      {gw && <span className="text-ink-muted"> ({gw} GW)</span>}
    </span>
  );
}

export function JolStats(filters: StatsFilters) {
  const { data = {}, isPending } = useStatsQuery<Record<string, JolStatsDto>>('/stats/jol', filters);
  const [monthFilter, setMonthFilter] = useState('');
  const ref = useRef<HTMLDivElement>(null);

  const rows: Row[] = Object.entries(data).map(([month, dto]) => ({ month, ...dto }));
  useSimpleTooltips(ref, [data]);

  const columns: StatsColumn<Row>[] = [
    { key: 'month', header: 'Month', sortMode: 'default', filter: { value: monthFilter, onChange: setMonthFilter } },
    { key: 'gamesStartedPerMonth', header: 'Games Started', sortMode: 'default' },
    { key: 'gamesEndedPerMonth', header: 'Games Ended', sortMode: 'default' },
    { key: 'net', header: 'Net', render: (r) => r.gamesStartedPerMonth - r.gamesEndedPerMonth },
    { key: 'winsPerMonth', header: 'Wins', sortMode: 'default' },
    { key: 'winRate', header: 'Win Rate', sortMode: 'percent' },
    { key: 'vpPerMonth', header: 'VP', sortMode: 'default' },
    { key: 'avgVp', header: 'Avg VP', sortMode: 'default' },
    { key: 'avgDuration', header: 'Avg Elapsed', sortMode: 'duration' },
    { key: 'bestPlayer', header: 'Player of the Month', sortMode: 'default', render: (r) => <TieCell value={r.bestPlayer} /> },
    { key: 'bestDeck', header: 'Deck of the Month', sortMode: 'default', render: (r) => <TieCell value={r.bestDeck} isDeck /> },
    { key: 'bestNation', header: 'Nation of the Month', sortMode: 'default', render: (r) => <BestNation value={r.bestNation} /> },
  ];

  return (
    <div ref={ref}>
      <SortableStatsTable<Row>
        rows={rows}
        columns={columns}
        rowKey={(r) => r.month}
        loading={isPending}
        defaultSort={{ key: 'month', ascending: false }}
      />
    </div>
  );
}
