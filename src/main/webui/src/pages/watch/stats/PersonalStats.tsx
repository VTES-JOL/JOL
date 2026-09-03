import { useState } from 'react';
import type { DeckMatchup, OpponentStats } from '../../../api/types';
import { TabBar, type TabDef } from '../../../components/TabBar';
import { SortableStatsTable, type StatsColumn } from './SortableStatsTable';
import { useStatsQuery, type StatsFilters } from './useStatsQuery';

interface PersonalProps extends StatsFilters {
  player: string;
}

const SUB_TABS: TabDef<'opponent' | 'deck'>[] = [
  { id: 'opponent', label: 'Opponent Performance' },
  { id: 'deck', label: 'Deck Performance' },
];

interface OpponentRow extends OpponentStats, Record<string, unknown> {}
interface DeckRow extends DeckMatchup, Record<string, unknown> {}

function OpponentPerformance({ player, ...filters }: PersonalProps) {
  const { data = {}, isPending } = useStatsQuery<Record<string, OpponentStats>>(
    `/stats/performance/${encodeURIComponent(player)}/players`,
    filters,
  );
  const [nameFilter, setNameFilter] = useState('');

  const columns: StatsColumn<OpponentRow>[] = [
    { key: 'opponent', header: 'Opponent', sortMode: 'default', filter: { value: nameFilter, onChange: setNameFilter } },
    { key: 'games', header: 'Number of Games ', sortMode: 'default' },
    { key: 'wins', header: 'Wins ', sortMode: 'default' },
    { key: 'winRate', header: 'Win Rate ', sortMode: 'percent' },
    { key: 'winOpponent', header: 'Opponent Won ', sortMode: 'default' },
    { key: 'winRateOpponent', header: 'Win Rate Against Opponent ', sortMode: 'percent' },
    { key: 'winOther', header: 'Other player won ', sortMode: 'default' },
    { key: 'losses', header: 'Losses ', sortMode: 'default' },
  ];

  return (
    <SortableStatsTable<OpponentRow>
      rows={Object.values(data) as OpponentRow[]}
      columns={columns}
      rowKey={(r) => r.opponent}
      loading={isPending}
      defaultSort={{ key: 'games', ascending: false }}
    />
  );
}

function DeckPerformance({ player, ...filters }: PersonalProps) {
  const { data = [], isPending } = useStatsQuery<DeckMatchup[]>(
    `/stats/performance/${encodeURIComponent(player)}/decks`,
    filters,
  );
  const [deckFilter, setDeckFilter] = useState('');
  const [opponentFilter, setOpponentFilter] = useState('');
  const [gamesFilter, setGamesFilter] = useState('');

  const columns: StatsColumn<DeckRow>[] = [
    { key: 'deckName', header: 'Deck', sortMode: 'default', filter: { value: deckFilter, onChange: setDeckFilter } },
    {
      key: 'opponentDeckName',
      header: 'Opponent Deck',
      sortMode: 'default',
      filter: { value: opponentFilter, onChange: setOpponentFilter },
    },
    { key: 'gameNames', header: 'Game Names', sortMode: 'default', filter: { value: gamesFilter, onChange: setGamesFilter } },
    { key: 'games', header: 'Games ', sortMode: 'default' },
    { key: 'totalWins', header: 'Wins ', sortMode: 'default' },
    { key: 'totalVP', header: 'VP ', sortMode: 'default' },
    { key: 'averageVP', header: 'Avg VP ', sortMode: 'default' },
    { key: 'opponentTotalVP', header: 'Opponent VP ', sortMode: 'default' },
    { key: 'opponentAverageVP', header: 'Opponent Avg VP ', sortMode: 'default' },
    { key: 'vpDifference', header: 'VP Difference ', sortMode: 'default' },
  ];

  return (
    <SortableStatsTable<DeckRow>
      rows={data as DeckRow[]}
      columns={columns}
      rowKey={(_, i) => i}
      loading={isPending}
      defaultSort={{ key: 'games', ascending: false }}
    />
  );
}

export function PersonalStats({ player, ...filters }: PersonalProps) {
  const [subTab, setSubTab] = useState<'opponent' | 'deck'>('opponent');

  return (
    <div>
      <TabBar tabs={SUB_TABS} active={subTab} onChange={setSubTab} className="mt-3" />
      <div className="mt-3">
        {subTab === 'opponent' ? (
          <OpponentPerformance player={player} {...filters} />
        ) : (
          <DeckPerformance player={player} {...filters} />
        )}
      </div>
    </div>
  );
}
