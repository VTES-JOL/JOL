import { useRef, useState } from 'react';
import type { StatsDto } from '../../../api/types';
import { CountryFlag, countryName } from '../../../components/CountryFlag';
import { useSimpleTooltips } from '../../../hooks/useSimpleTooltips';
import { StatsDtoTable } from './StatsDtoTable';
import { useStatsQuery, type StatsFilters } from './useStatsQuery';

export function NationStats(filters: StatsFilters) {
  const [threshold, setThreshold] = useState('');
  const [nameFilter, setNameFilter] = useState('');
  const ref = useRef<HTMLDivElement>(null);
  const { data = {}, isPending } = useStatsQuery<Record<string, StatsDto>>('/stats/nations', {
    ...filters,
    threshold: Number(threshold) || 0,
  });

  useSimpleTooltips(ref, [data]);

  return (
    <div ref={ref}>
      <StatsDtoTable
        data={data}
        loading={isPending}
        extended={false}
        nameHeader="Nation"
        renderName={(code) => (
          <>
            {countryName(code)} / <CountryFlag code={code} />
          </>
        )}
        threshold={threshold}
        onThresholdChange={setThreshold}
        nameFilter={nameFilter}
        onNameFilterChange={setNameFilter}
        filterValue={(code) => countryName(code) ?? code}
      />
    </div>
  );
}
