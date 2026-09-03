import { useState } from 'react';
import { Check, X } from 'lucide-react';
import type { GameDuration } from '../../../api/types';
import { SortableStatsTable, type StatsColumn } from './SortableStatsTable';
import { useStatsQuery, type StatsFilters } from './useStatsQuery';

interface Row extends GameDuration, Record<string, unknown> {}

export function GameStats(filters: StatsFilters) {
  const { data = [], isPending } = useStatsQuery<GameDuration[]>('/stats/games', filters);
  const [nameFilter, setNameFilter] = useState('');
  const [playerFilter, setPlayerFilter] = useState('');

  const columns: StatsColumn<Row>[] = [
    { key: 'gameName', header: 'Game', sortMode: 'default', filter: { value: nameFilter, onChange: setNameFilter } },
    { key: 'players', header: 'Players', sortMode: 'default', filter: { value: playerFilter, onChange: setPlayerFilter } },
    { key: 'duration', header: 'Elapsed', sortMode: 'duration' },
    {
      key: 'hasGw',
      header: 'GW? ',
      sortMode: 'boolean',
      render: (r) =>
        r.hasGw ? (
          <Check size={14} className="inline text-online" />
        ) : (
          <X size={14} className="inline text-blood-soft" />
        ),
    },
    { key: 'vps', header: 'VPs ', sortMode: 'default' },
  ];

  return (
    <SortableStatsTable<Row>
      rows={data as Row[]}
      columns={columns}
      rowKey={(_, i) => i}
      loading={isPending}
      defaultSort={{ key: 'duration', mode: 'duration', ascending: false }}
    />
  );
}
