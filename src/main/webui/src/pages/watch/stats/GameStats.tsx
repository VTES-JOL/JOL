import { useState } from 'react';
import type { GameDuration } from '../../../api/types';
import { SortableStatsTable, type StatsColumn } from './SortableStatsTable';
import { useStatsQuery, type StatsFilters } from './useStatsQuery';

interface Row extends GameDuration, Record<string, unknown> {}

export function GameStats(filters: StatsFilters) {
  const { data = [] } = useStatsQuery<GameDuration[]>('/stats/games', filters);
  const [nameFilter, setNameFilter] = useState('');
  const [playerFilter, setPlayerFilter] = useState('');

  const columns: StatsColumn<Row>[] = [
    { key: 'gameName', header: 'Game', sortMode: 'default', filter: { value: nameFilter, onChange: setNameFilter } },
    { key: 'players', header: 'Players', sortMode: 'default', filter: { value: playerFilter, onChange: setPlayerFilter } },
    { key: 'duration', header: 'Duration ', sortMode: 'duration' },
    {
      key: 'hasGw',
      header: 'GW? ',
      sortMode: 'boolean',
      render: (r) =>
        r.hasGw ? <i className="bi bi-check-circle text-success" /> : <i className="bi bi-x-circle text-danger" />,
    },
    { key: 'vps', header: 'VPs ', sortMode: 'default' },
  ];

  return <SortableStatsTable<Row> rows={data as Row[]} columns={columns} rowKey={(_, i) => i} />;
}
