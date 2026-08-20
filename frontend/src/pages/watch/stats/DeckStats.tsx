import { useEffect, useState } from 'react';
import { api } from '../../../api/client';
import type { StatsDto } from '../../../api/types';
import { StatsDtoTable } from './StatsDtoTable';

export function DeckStats({ fromDate, toDate, isTourney }: { fromDate: string; toDate: string; isTourney: boolean }) {
  const [data, setData] = useState<Record<string, StatsDto>>({});
  const [threshold, setThreshold] = useState('0');
  const [nameFilter, setNameFilter] = useState('');

  useEffect(() => {
    api
      .post<Record<string, StatsDto>>('/stats/decks', { treshold: Number(threshold) || 0, fromDate, toDate, isTourney })
      .then(setData)
      .catch((err) => console.error('Failed to load deck stats', err));
  }, [threshold, fromDate, toDate, isTourney]);

  return (
    <StatsDtoTable
      data={data}
      extended={false}
      nameHeader="Deck / Player"
      renderName={(key) => key}
      threshold={threshold}
      onThresholdChange={setThreshold}
      nameFilter={nameFilter}
      onNameFilterChange={setNameFilter}
    />
  );
}
