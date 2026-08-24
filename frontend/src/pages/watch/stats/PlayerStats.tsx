import { useEffect, useState } from 'react';
import { api } from '../../../api/client';
import type { StatsDto } from '../../../api/types';
import { StatsDtoTable } from './StatsDtoTable';
import { showError } from '../../../components/toast';

export function PlayerStats({ fromDate, toDate, isTourney }: { fromDate: string; toDate: string; isTourney: boolean }) {
  const [data, setData] = useState<Record<string, StatsDto>>({});
  const [threshold, setThreshold] = useState('0');
  const [nameFilter, setNameFilter] = useState('');

  useEffect(() => {
    api
      .post<Record<string, StatsDto>>('/stats/players', { treshold: Number(threshold) || 0, fromDate, toDate, isTourney })
      .then(setData)
      .catch((err) => {
        console.error('Failed to load player stats', err);
        showError('Failed to load player stats.');
      });
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
