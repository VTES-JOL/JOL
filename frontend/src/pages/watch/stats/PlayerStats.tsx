import { useEffect, useState } from 'react';
import { api } from '../../../api/client';
import type { StatsDto } from '../../../api/types';
import { StatsDtoTable } from './StatsDtoTable';
import { runRequest } from '../../../api/mutate';

export function PlayerStats({ fromDate, toDate, isTourney }: { fromDate: string; toDate: string; isTourney: boolean }) {
  const [data, setData] = useState<Record<string, StatsDto>>({});
  const [threshold, setThreshold] = useState('0');
  const [nameFilter, setNameFilter] = useState('');

  useEffect(() => {
    runRequest(
      api.post<Record<string, StatsDto>>('/stats/players', { treshold: Number(threshold) || 0, fromDate, toDate, isTourney }),
      'Failed to load player stats',
      setData,
    );
  }, [threshold, fromDate, toDate, isTourney]);

  return (
    <StatsDtoTable
      data={data}
      extended
      nameHeader="Player"
      renderName={(key) => key}
      threshold={threshold}
      onThresholdChange={setThreshold}
      nameFilter={nameFilter}
      onNameFilterChange={setNameFilter}
    />
  );
}
