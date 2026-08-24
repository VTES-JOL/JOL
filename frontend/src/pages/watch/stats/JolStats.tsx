import { useEffect, useRef, useState } from 'react';
import { api } from '../../../api/client';
import type { JolStats as JolStatsDto } from '../../../api/types';
import { SortIcon, useTableSort } from '../statsUtils';
import { useSimpleTooltips } from '../../../hooks/useSimpleTooltips';
import { showError } from '../../../components/toast';

const regionNames = new Intl.DisplayNames(['en'], { type: 'region' });
const BEST_NATION_PATTERN = /^([A-Z]{2})\s*\((\d+)\s*GW\)$/;

interface Row extends JolStatsDto, Record<string, unknown> {
  month: string;
}

function BestNation({ value }: { value: string }) {
  const match = value !== '-' ? value.match(BEST_NATION_PATTERN) : null;
  if (!match) return <>-</>;
  const [, code, gw] = match;
  const name = regionNames.of(code);
  return (
    <>
      <span data-tippy-content={name} className={`fi fi-${code.toLowerCase()} fis`} /> {name} ({gw} GW)
    </>
  );
}

export function JolStats({ fromDate, toDate, isTourney }: { fromDate: string; toDate: string; isTourney: boolean }) {
  const [data, setData] = useState<Record<string, JolStatsDto>>({});
  const [monthFilter, setMonthFilter] = useState('');
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    api
      .post<Record<string, JolStatsDto>>('/stats/jol', { treshold: 0, fromDate, toDate, isTourney })
      .then(setData)
      .catch((err) => {
        console.error('Failed to load JOL stats', err);
        showError('Failed to load JOL stats.');
      });
  }, [fromDate, toDate, isTourney]);

  const rows: Row[] = Object.entries(data).map(([month, dto]) => ({ month, ...dto }));
  const { sorted, toggle } = useTableSort(rows);
  const filtered = sorted.filter((r) => r.month.toLowerCase().includes(monthFilter.toLowerCase()));

  useSimpleTooltips(ref, [data]);

  return (
    <div ref={ref} className="overflow-auto pb-3" style={{ height: '78vh' }}>
      <table className="table table-bordered table-sm mb-0">
        <thead>
          <tr>
            <th className="sticky-top bg-white">
              Month
              <input type="text" value={monthFilter} onChange={(e) => setMonthFilter(e.target.value)} />
              <SortIcon column="month" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Games Started <SortIcon column="gamesStartedPerMonth" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Games Ended <SortIcon column="gamesEndedPerMonth" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">Net</th>
            <th className="sticky-top bg-white">
              Wins <SortIcon column="winsPerMonth" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Win Rate <SortIcon column="winRate" onSort={toggle} mode="percent" />
            </th>
            <th className="sticky-top bg-white">
              Vp <SortIcon column="vpPerMonth" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Avg Vp <SortIcon column="avgVp" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Avg Duration <SortIcon column="avgDuration" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Player of the Month <SortIcon column="bestPlayer" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white w-25">
              Deck of the Month <SortIcon column="bestDeck" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Nation of the Month <SortIcon column="bestNation" onSort={toggle} />
            </th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((r) => (
            <tr key={r.month} className="border-top">
              <td>{r.month}</td>
              <td>{r.gamesStartedPerMonth}</td>
              <td>{r.gamesEndedPerMonth}</td>
              <td>{r.gamesStartedPerMonth - r.gamesEndedPerMonth}</td>
              <td>{r.winsPerMonth}</td>
              <td>{r.winRate}</td>
              <td>{r.vpPerMonth}</td>
              <td>{r.avgVp}</td>
              <td>{r.avgDuration}</td>
              <td>{r.bestPlayer}</td>
              <td>{r.bestDeck}</td>
              <td>
                <BestNation value={r.bestNation} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
