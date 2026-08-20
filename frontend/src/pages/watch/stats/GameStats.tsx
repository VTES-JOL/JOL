import { useEffect, useState } from 'react';
import { api } from '../../../api/client';
import type { GameDuration } from '../../../api/types';
import { SortIcon, useTableSort } from '../statsUtils';

export function GameStats({ fromDate, toDate, isTourney }: { fromDate: string; toDate: string; isTourney: boolean }) {
  const [data, setData] = useState<GameDuration[]>([]);
  const [nameFilter, setNameFilter] = useState('');
  const [playerFilter, setPlayerFilter] = useState('');

  useEffect(() => {
    api
      .post<GameDuration[]>('/stats/games', { treshold: 0, fromDate, toDate, isTourney })
      .then(setData)
      .catch((err) => console.error('Failed to load game stats', err));
  }, [fromDate, toDate, isTourney]);

  const { sorted, toggle } = useTableSort(data as unknown as (GameDuration & Record<string, unknown>)[]);
  const filtered = sorted.filter(
    (r) =>
      r.gameName.toLowerCase().includes(nameFilter.toLowerCase()) &&
      r.players.toLowerCase().includes(playerFilter.toLowerCase()),
  );

  return (
    <div className="overflow-auto pb-3" style={{ height: '78vh' }}>
      <table className="table table-bordered table-sm mb-0">
        <thead>
          <tr>
            <th className="sticky-top bg-white">
              Game
              <input type="text" value={nameFilter} onChange={(e) => setNameFilter(e.target.value)} />
              <SortIcon column="gameName" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Players
              <input type="text" value={playerFilter} onChange={(e) => setPlayerFilter(e.target.value)} />
              <SortIcon column="players" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Duration <SortIcon column="duration" onSort={toggle} mode="duration" />
            </th>
            <th className="sticky-top bg-white">
              GW? <SortIcon column="hasGw" onSort={toggle} mode="boolean" />
            </th>
            <th className="sticky-top bg-white">
              VPs <SortIcon column="vps" onSort={toggle} />
            </th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((r, i) => (
            <tr key={i} className="border-top">
              <td>{r.gameName}</td>
              <td>{r.players}</td>
              <td>{r.duration}</td>
              <td>
                {r.hasGw ? (
                  <i className="bi bi-check-circle text-success" />
                ) : (
                  <i className="bi bi-x-circle text-danger" />
                )}
              </td>
              <td>{r.vps}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
