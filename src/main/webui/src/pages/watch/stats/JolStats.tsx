import { useRef, useState } from 'react';
import type { JolStats as JolStatsDto } from '../../../api/types';
import { CountryFlag } from '../../../components/CountryFlag';
import { useSimpleTooltips } from '../../../hooks/useSimpleTooltips';
import { SortableStatsTable, type StatsColumn } from './SortableStatsTable';
import { useStatsQuery, type StatsFilters } from './useStatsQuery';

const BEST_NATION_PATTERN = /^([A-Z]{2})\s*\((\d+)\s*GW\)$/;

interface Row extends JolStatsDto, Record<string, unknown> {
  month: string;
}

function BestNation({ value }: { value: string }) {
  const match = value !== '-' ? value.match(BEST_NATION_PATTERN) : null;
  if (!match) return <>-</>;
  const [, code, gw] = match;
  return (
    <>
      <CountryFlag code={code} withName /> ({gw} GW)
    </>
  );
}

export function JolStats(filters: StatsFilters) {
  const { data = {} } = useStatsQuery<Record<string, JolStatsDto>>('/stats/jol', filters);
  const [monthFilter, setMonthFilter] = useState('');
  const ref = useRef<HTMLDivElement>(null);

  const rows: Row[] = Object.entries(data).map(([month, dto]) => ({ month, ...dto }));
  useSimpleTooltips(ref, [data]);

  const columns: StatsColumn<Row>[] = [
    { key: 'month', header: 'Month', sortMode: 'default', filter: { value: monthFilter, onChange: setMonthFilter } },
    { key: 'gamesStartedPerMonth', header: 'Games Started ', sortMode: 'default' },
    { key: 'gamesEndedPerMonth', header: 'Games Ended ', sortMode: 'default' },
    { key: 'net', header: 'Net', render: (r) => r.gamesStartedPerMonth - r.gamesEndedPerMonth },
    { key: 'winsPerMonth', header: 'Wins ', sortMode: 'default' },
    { key: 'winRate', header: 'Win Rate ', sortMode: 'percent' },
    { key: 'vpPerMonth', header: 'Vp ', sortMode: 'default' },
    { key: 'avgVp', header: 'Avg Vp ', sortMode: 'default' },
    { key: 'avgDuration', header: 'Avg Duration ', sortMode: 'default' },
    { key: 'bestPlayer', header: 'Player of the Month ', sortMode: 'default' },
    { key: 'bestDeck', header: 'Deck of the Month ', sortMode: 'default', thClassName: 'w-25' },
    { key: 'bestNation', header: 'Nation of the Month ', sortMode: 'default', render: (r) => <BestNation value={r.bestNation} /> },
  ];

  return (
    <div ref={ref}>
      <SortableStatsTable<Row> rows={rows} columns={columns} rowKey={(r) => r.month} />
    </div>
  );
}
