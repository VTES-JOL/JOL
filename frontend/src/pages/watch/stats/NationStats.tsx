import { useEffect, useRef, useState } from 'react';
import { api } from '../../../api/client';
import type { StatsDto } from '../../../api/types';
import { StatsDtoTable } from './StatsDtoTable';
import { useSimpleTooltips } from '../../../hooks/useSimpleTooltips';
import { runRequest } from '../../../api/mutate';

const regionNames = new Intl.DisplayNames(['en'], { type: 'region' });

export function NationStats({ fromDate, toDate, isTourney }: { fromDate: string; toDate: string; isTourney: boolean }) {
  const [data, setData] = useState<Record<string, StatsDto>>({});
  const [threshold, setThreshold] = useState('0');
  const [nameFilter, setNameFilter] = useState('');
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    runRequest(
      api.post<Record<string, StatsDto>>('/stats/nations', { treshold: Number(threshold) || 0, fromDate, toDate, isTourney }),
      'Failed to load nation stats',
      setData,
    );
  }, [threshold, fromDate, toDate, isTourney]);

  useSimpleTooltips(ref, [data]);

  return (
    <div ref={ref}>
      <StatsDtoTable
        data={data}
        extended={false}
        nameHeader="Nation"
        renderName={(code) => (
          <>
            {regionNames.of(code)} / <span data-tippy-content={regionNames.of(code)} className={`fi fi-${code.toLowerCase()} fis`} />
          </>
        )}
        threshold={threshold}
        onThresholdChange={setThreshold}
        nameFilter={nameFilter}
        onNameFilterChange={setNameFilter}
        filterValue={(code) => regionNames.of(code) ?? code}
      />
    </div>
  );
}
