import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Bell, TriangleAlert } from 'lucide-react';
import { api } from '../../api/client';
import type { GamesSummary, GameStatusBean } from '../../api/types';
import { Card, CardHeader, CardTitle } from '../../components/ui/Card';
import { TabBar } from '../../components/TabBar';
import { useAuth } from '../../auth/useAuth';
import { pathForGame } from '../../routes';

type TabId = 'myGames' | 'tournamentGames' | 'oustedGames';

const TABS: { id: TabId; label: string; field: keyof GamesSummary; showSeatRow: boolean; emptyMessage: string }[] = [
  { id: 'myGames', label: 'Active', field: 'games', showSeatRow: true, emptyMessage: 'No active games. Start or join one from the Lobby.' },
  { id: 'tournamentGames', label: 'Tournament', field: 'tournament', showSeatRow: true, emptyMessage: 'No tournament games in progress.' },
  { id: 'oustedGames', label: 'Ousted', field: 'ousted', showSeatRow: false, emptyMessage: 'No ousted games to review.' },
];

function SeatRow({ game }: { game: GameStatusBean }) {
  const pred = game.predator ? game.players[game.predator] : undefined;
  const active = game.activePlayer ? game.players[game.activePlayer] : undefined;
  const prey = game.prey ? game.players[game.prey] : undefined;

  return (
    <div className="jt:flex jt:items-center jt:gap-1 jt:text-ink-muted jt:text-xs jt:mt-1">
      <ArrowLeft size={12} />
      <span>{pred?.playerName ?? game.predator}</span>
      {pred?.pinged && <TriangleAlert size={11} className="jt:text-blood" />}
      <span className="jt:mx-1 jt:text-ink-muted/60">·</span>
      <strong className="jt:text-ink">{active?.playerName ?? game.activePlayer}</strong>
      <span className="jt:mx-1 jt:text-ink-muted/60">·</span>
      <span>{prey?.playerName ?? game.prey}</span>
      {prey?.pinged && <TriangleAlert size={11} className="jt:text-blood" />}
      <ArrowRight size={12} />
    </div>
  );
}

function GameRow({ game, player, showSeatRow }: { game: GameStatusBean; player: string | null; showSeatRow: boolean }) {
  const self = player ? game.players[player] : undefined;
  const pinged = !!self?.pinged;
  const needsAttention = !!self && !self.current;
  const accent = pinged ? 'jt:border-l-4 jt:border-blood' : needsAttention ? 'jt:border-l-4 jt:border-accent/40' : '';

  return (
    <li className={`jt:border-b jt:border-line/50 ${accent}`}>
      <Link to={pathForGame(game.gameId)} className="jt:block jt:px-2 jt:py-2 jt:no-underline jt:text-ink jt:hover:bg-hover">
        <div className="jt:flex jt:items-center jt:justify-between">
          <span className="jt:font-bold jt:break-words jt:flex jt:items-center jt:gap-1">
            {pinged && <TriangleAlert size={12} className="jt:text-blood" />}
            {!pinged && needsAttention && <Bell size={12} />}
            {game.name}
          </span>
          {game.turn && <span className="jt:text-xs jt:text-ink-muted jt:ml-2 jt:whitespace-nowrap">{game.turn}</span>}
        </div>
        {showSeatRow && game.predator && <SeatRow game={game} />}
      </Link>
    </li>
  );
}

const EMPTY: GamesSummary = { games: [], tournament: [], ousted: [] };

export function GamesPanel() {
  const { player } = useAuth();
  const { data: summary = EMPTY } = useQuery({
    queryKey: ['main-games'],
    queryFn: () => api.get<GamesSummary>('/main/games'),
  });
  const [activeTab, setActiveTab] = useState<TabId>('myGames');
  const tab = TABS.find((t) => t.id === activeTab)!;
  const games = summary[tab.field];

  return (
    <Card className="jt:flex jt:flex-col jt:flex-1 jt:min-h-0 jt:overflow-hidden">
      <CardHeader>
        <CardTitle>Games List</CardTitle>
      </CardHeader>
      <TabBar
        tabs={TABS.map((t) => ({ id: t.id, label: t.label, badge: summary[t.field].length }))}
        active={activeTab}
        onChange={setActiveTab}
        className="ms-0 border-0"
        tabClassName="px-3 py-2"
      />
      <div className="jt:flex-1 jt:min-h-0 jt:overflow-y-auto">
        {games.length === 0 ? (
          <div className="jt:text-ink-muted jt:text-sm jt:text-center jt:p-4">{tab.emptyMessage}</div>
        ) : (
          <ul className="jt:list-none">
            {games.map((game) => (
              <GameRow key={game.name} game={game} player={player} showSeatRow={tab.showSeatRow} />
            ))}
          </ul>
        )}
      </div>
    </Card>
  );
}
