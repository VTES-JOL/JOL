import { useState } from 'react';
import type { StatsDto } from '../../../api/types';
import { StatsDtoTable } from './StatsDtoTable';
import { useStatsQuery, type StatsFilters } from './useStatsQuery';

export function PlayerStats(filters: StatsFilters) {
  const [threshold, setThreshold] = useState('');
  const [nameFilter, setNameFilter] = useState('');
  const { data = {}, isPending } = useStatsQuery<Record<string, StatsDto>>('/stats/players', {
    ...filters,
    threshold: Number(threshold) || 0,
  });

  return (
    <StatsDtoTable
      data={data}
      loading={isPending}
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
