import { useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Bell, Gamepad2, TriangleAlert } from 'lucide-react';
import { api } from '../../api/client';
import type { GamesSummary, GameStatusBean } from '../../api/types';
import { Card, CardHeader, CardTitle } from '../../components/ui/Card';
import { EmptyState } from '../../components/ui/EmptyState';
import { TabBar } from '../../components/TabBar';
import { useAuth } from '../../auth/useAuth';
import { useSimpleTooltips } from '../../hooks/useSimpleTooltips';
import { pathForGame } from '../../routes';

type TabId = 'myGames' | 'tournamentGames' | 'oustedGames';

const TABS: {
  id: TabId;
  label: string;
  field: keyof GamesSummary;
  showSeatRow: boolean;
  emptyTitle: string;
  emptyHint: string;
}[] = [
  {
    id: 'myGames',
    label: 'Active',
    field: 'games',
    showSeatRow: true,
    emptyTitle: 'No active games',
    emptyHint: 'Start or join one from the Lobby.',
  },
  {
    id: 'tournamentGames',
    label: 'Tournament',
    field: 'tournament',
    showSeatRow: true,
    emptyTitle: 'No tournament games',
    emptyHint: 'Games from tournaments you’re in show up here.',
  },
  {
    id: 'oustedGames',
    label: 'Ousted',
    field: 'ousted',
    showSeatRow: false,
    emptyTitle: 'No ousted games',
    emptyHint: 'Games where you’ve been ousted stay here for review.',
  },
];

function SeatRow({ game }: { game: GameStatusBean }) {
  const pred = game.predator ? game.players[game.predator] : undefined;
  const active = game.activePlayer ? game.players[game.activePlayer] : undefined;
  const prey = game.prey ? game.players[game.prey] : undefined;

  return (
    <div className="flex items-center gap-1 text-ink-muted text-xs mt-1">
      <ArrowLeft size={12} />
      <span data-tippy-content="Predator">{pred?.playerName ?? game.predator}</span>
      {pred?.pinged && <TriangleAlert size={11} className="text-blood" />}
      <span className="mx-1 text-ink-muted/60">·</span>
      <strong className="text-ink" data-tippy-content="Active player">
        {active?.playerName ?? game.activePlayer}
      </strong>
      <span className="mx-1 text-ink-muted/60">·</span>
      <span data-tippy-content="Prey">{prey?.playerName ?? game.prey}</span>
      {prey?.pinged && <TriangleAlert size={11} className="text-blood" />}
      <ArrowRight size={12} />
    </div>
  );
}

function GameRow({ game, player, showSeatRow }: { game: GameStatusBean; player: string | null; showSeatRow: boolean }) {
  const self = player ? game.players[player] : undefined;
  const pinged = !!self?.pinged;
  const needsAttention = !!self && !self.current;
  const accent = pinged ? 'border-l-4 border-blood' : needsAttention ? 'border-l-4 border-accent/40' : '';

  return (
    <li className={`border-b border-line/50 ${accent}`}>
      <Link to={pathForGame(game.gameId)} className="block px-2 py-2 no-underline text-ink hover:bg-hover">
        <div className="flex items-center justify-between">
          <span className="font-bold break-words flex items-center gap-1">
            {pinged && <TriangleAlert size={12} className="text-blood" />}
            {!pinged && needsAttention && <Bell size={12} />}
            {game.name}
          </span>
          {game.turn && <span className="text-xs text-ink-muted ml-2 whitespace-nowrap">{game.turn}</span>}
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

  const listRef = useRef<HTMLDivElement>(null);
  useSimpleTooltips(listRef, [summary, activeTab]);

  return (
    // Content-height, not flex-1: with a handful of games this card would
    // otherwise stretch to the (chat-driven) column height and leave a big
    // empty panel. Cap it so a long list scrolls internally instead.
    <Card className="flex flex-col min-h-0 overflow-hidden max-h-[45vh] lg:max-h-full">
      <CardHeader>
        <CardTitle>Games List</CardTitle>
      </CardHeader>
      <TabBar
        size="sm"
        tabs={TABS.map((t) => ({ id: t.id, label: t.label, badge: summary[t.field].length || undefined }))}
        active={activeTab}
        onChange={setActiveTab}
      />
      <div ref={listRef} className="flex-1 min-h-0 overflow-y-auto">
        {games.length === 0 ? (
          <EmptyState icon={Gamepad2} title={tab.emptyTitle} description={tab.emptyHint} />
        ) : (
          <ul className="list-none">
            {games.map((game) => (
              <GameRow key={game.name} game={game} player={player} showSeatRow={tab.showSeatRow} />
            ))}
          </ul>
        )}
      </div>
    </Card>
  );
}
