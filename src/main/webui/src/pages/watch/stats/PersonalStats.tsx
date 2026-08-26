import { useEffect, useState } from 'react';
import { api } from '../../../api/client';
import type { DeckMatchup, OpponentStats } from '../../../api/types';
import { SortIcon, useTableSort } from '../statsUtils';
import { runRequest } from '../../../api/mutate';

interface OpponentRow extends OpponentStats, Record<string, unknown> {}

function OpponentPerformance({
  player,
  fromDate,
  toDate,
  isTourney,
}: {
  player: string;
  fromDate: string;
  toDate: string;
  isTourney: boolean;
}) {
  const [data, setData] = useState<Record<string, OpponentStats>>({});
  const [nameFilter, setNameFilter] = useState('');

  useEffect(() => {
    runRequest(
      api.post<Record<string, OpponentStats>>(`/stats/performance/${encodeURIComponent(player)}/players`, {
        treshold: 0,
        fromDate,
        toDate,
        isTourney,
      }),
      'Failed to load opponent stats',
      setData,
    );
  }, [player, fromDate, toDate, isTourney]);

  const rows = Object.values(data) as OpponentRow[];
  const { sorted, toggle } = useTableSort(rows);
  const filtered = sorted.filter((r) => r.opponent.toLowerCase().includes(nameFilter.toLowerCase()));

  return (
    <div className="overflow-auto pb-3" style={{ height: '73vh' }}>
      <table className="table table-bordered table-sm mb-0">
        <thead>
          <tr>
            <th className="sticky-top bg-white">
              Opponent
              <input type="text" value={nameFilter} onChange={(e) => setNameFilter(e.target.value)} />
              <SortIcon column="opponent" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Number of Games <SortIcon column="games" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Wins <SortIcon column="wins" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Win Rate <SortIcon column="winRate" onSort={toggle} mode="percent" />
            </th>
            <th className="sticky-top bg-white">
              Opponent Won <SortIcon column="winOpponent" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Win Rate Against Opponent <SortIcon column="winRateOpponent" onSort={toggle} mode="percent" />
            </th>
            <th className="sticky-top bg-white">
              Other player won <SortIcon column="winOther" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Losses <SortIcon column="losses" onSort={toggle} />
            </th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((r) => (
            <tr key={r.opponent} className="border-top">
              <td>{r.opponent}</td>
              <td>{r.games}</td>
              <td>{r.wins}</td>
              <td>{r.winRate}</td>
              <td>{r.winOpponent}</td>
              <td>{r.winRateOpponent}</td>
              <td>{r.winOther}</td>
              <td>{r.losses}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function DeckPerformance({
  player,
  fromDate,
  toDate,
  isTourney,
}: {
  player: string;
  fromDate: string;
  toDate: string;
  isTourney: boolean;
}) {
  const [data, setData] = useState<DeckMatchup[]>([]);
  const [deckFilter, setDeckFilter] = useState('');
  const [opponentFilter, setOpponentFilter] = useState('');
  const [gamesFilter, setGamesFilter] = useState('');

  useEffect(() => {
    runRequest(
      api.post<DeckMatchup[]>(`/stats/performance/${encodeURIComponent(player)}/decks`, {
        treshold: 0,
        fromDate,
        toDate,
        isTourney,
      }),
      'Failed to load deck performance',
      setData,
    );
  }, [player, fromDate, toDate, isTourney]);

  const { sorted, toggle } = useTableSort(data as unknown as (DeckMatchup & Record<string, unknown>)[]);
  const filtered = sorted.filter(
    (r) =>
      r.deckName.toLowerCase().includes(deckFilter.toLowerCase()) &&
      r.opponentDeckName.toLowerCase().includes(opponentFilter.toLowerCase()) &&
      r.gameNames.toLowerCase().includes(gamesFilter.toLowerCase()),
  );

  return (
    <div className="overflow-auto pb-3" style={{ height: '73vh' }}>
      <table className="table table-bordered table-sm mb-0">
        <thead>
          <tr>
            <th className="sticky-top bg-white">
              Deck
              <input type="text" value={deckFilter} onChange={(e) => setDeckFilter(e.target.value)} />
              <SortIcon column="deckName" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Opponent Deck
              <input type="text" value={opponentFilter} onChange={(e) => setOpponentFilter(e.target.value)} />
              <SortIcon column="opponentDeckName" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Game Names
              <input type="text" value={gamesFilter} onChange={(e) => setGamesFilter(e.target.value)} />
              <SortIcon column="gameNames" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Games <SortIcon column="games" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Wins <SortIcon column="totalWins" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              VP <SortIcon column="totalVP" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Avg VP <SortIcon column="averageVP" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Opponent VP <SortIcon column="opponentTotalVP" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              Opponent Avg VP <SortIcon column="opponentAverageVP" onSort={toggle} />
            </th>
            <th className="sticky-top bg-white">
              VP Difference <SortIcon column="vpDifference" onSort={toggle} />
            </th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((r, i) => (
            <tr key={i} className="border-top">
              <td>{r.deckName}</td>
              <td>{r.opponentDeckName}</td>
              <td>{r.gameNames}</td>
              <td>{r.games}</td>
              <td>{r.totalWins}</td>
              <td>{r.totalVP}</td>
              <td>{r.averageVP}</td>
              <td>{r.opponentTotalVP}</td>
              <td>{r.opponentAverageVP}</td>
              <td>{r.vpDifference}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function PersonalStats({
  player,
  fromDate,
  toDate,
  isTourney,
}: {
  player: string;
  fromDate: string;
  toDate: string;
  isTourney: boolean;
}) {
  const [subTab, setSubTab] = useState<'opponent' | 'deck'>('opponent');

  return (
    <div>
      <ul className="nav nav-tabs mt-3">
        <li className="nav-item">
          <button className={`nav-link ${subTab === 'opponent' ? 'active' : ''}`} onClick={() => setSubTab('opponent')}>
            Opponent Performance
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${subTab === 'deck' ? 'active' : ''}`} onClick={() => setSubTab('deck')}>
            Deck Performance
          </button>
        </li>
      </ul>
      <div className="tab-content mt-3">
        {subTab === 'opponent' ? (
          <OpponentPerformance player={player} fromDate={fromDate} toDate={toDate} isTourney={isTourney} />
        ) : (
          <DeckPerformance player={player} fromDate={fromDate} toDate={toDate} isTourney={isTourney} />
        )}
      </div>
    </div>
  );
}
