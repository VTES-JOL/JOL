import { useEffect, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  ArrowLeft,
  ArrowRight,
  Bell,
  Droplet,
  Gamepad2,
  Rows3,
  StretchHorizontal,
  Swords,
  TriangleAlert,
} from 'lucide-react';
import { api } from '../../api/client';
import type { GamesSummary, GameStatusBean, PlayerStatus } from '../../api/types';
import { Card, CardHeader, CardTitle } from '../../components/ui/Card';
import { EmptyState } from '../../components/ui/EmptyState';
import { TabBar } from '../../components/TabBar';
import { useAuth } from '../../auth/useAuth';
import { useSimpleTooltips } from '../../hooks/useSimpleTooltips';
import { pathForGame } from '../../routes';

type TabId = 'myGames' | 'tournamentGames' | 'oustedGames';
type Density = 'compact' | 'detailed';

const DENSITY_KEY = 'jol.gamesPanel.density';

function loadDensity(): Density {
  try {
    return localStorage.getItem(DENSITY_KEY) === 'detailed' ? 'detailed' : 'compact';
  } catch {
    return 'compact';
  }
}

function formatVp(vp: number): string {
  return Number.isInteger(vp) ? String(vp) : vp.toFixed(1);
}

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

function GameRow({
  game,
  player,
  showSeatRow,
}: {
  game: GameStatusBean;
  player: string | null;
  showSeatRow: boolean;
}) {
  const self = player ? game.players[player] : undefined;
  const pinged = !!self?.pinged;
  const needsAttention = !!self && !self.current;
  const accent = pinged ? 'border-l-4 border-blood' : needsAttention ? 'border-l-4 border-accent/40' : '';
  const hasEdge = !!game.edge && game.edge === player;

  return (
    <li className={`border-b border-line/50 ${accent}`}>
      <Link to={pathForGame(game.gameId)} className="block px-2 py-2 no-underline text-ink hover:bg-hover">
        <div className="flex items-center justify-between gap-2">
          <span className="font-bold break-words flex items-center gap-1 min-w-0">
            {pinged && <TriangleAlert size={12} className="text-blood shrink-0" />}
            {!pinged && needsAttention && <Bell size={12} className="shrink-0" />}
            {hasEdge && <Swords size={12} className="text-gold-soft shrink-0" data-tippy-content="You hold the edge" />}
            <span className="truncate">{game.name}</span>
          </span>
          <span className="flex items-center gap-2 shrink-0 text-xs">
            {game.turn && <span className="text-ink-muted whitespace-nowrap">{game.turn}</span>}
          </span>
        </div>
        {showSeatRow && game.predator && <SeatRow game={game} />}
      </Link>
    </li>
  );
}

/** One seat line inside the detailed card. */
function DetailSeat({
  name,
  status,
  active,
  isSelf,
  hasEdge,
}: {
  name: string;
  status: PlayerStatus | undefined;
  active: boolean;
  isSelf: boolean;
  hasEdge: boolean;
}) {
  const ousted = !!status?.ousted;
  return (
    <div
      className={[
        'grid grid-cols-[minmax(0,1fr)_auto_auto] items-center gap-2 px-2 py-1 text-xs border-t border-line/40 first:border-t-0',
        active ? 'bg-arcane/10' : ousted ? 'text-ink-muted' : '',
      ].join(' ')}
    >
      <span className={`truncate ${active ? 'font-semibold text-arcane-soft' : ''} ${isSelf ? 'underline decoration-dotted underline-offset-2' : ''}`} title={name}>
        {name}
        {hasEdge && <Swords size={11} className="ml-1 inline align-[-1px] text-gold-soft" aria-label="has the edge" />}
      </span>
      <span className="whitespace-nowrap tabular-nums text-right">
        {ousted ? (
          <span className="rounded bg-blood/15 px-1 py-0.5 text-[10px] font-bold text-blood-soft">OUT</span>
        ) : (
          <span className="inline-flex items-center gap-0.5">
            <Droplet size={11} className="fill-current text-blood" />
            {status?.pool ?? 0}
          </span>
        )}
      </span>
      <span className="w-12 whitespace-nowrap text-right tabular-nums">
        {status && status.vp > 0 ? (
          <span className="font-semibold text-gold-soft">{formatVp(status.vp)} VP</span>
        ) : (
          <span className="text-ink-muted">–</span>
        )}
      </span>
    </div>
  );
}

function DetailedGameCard({ game, player }: { game: GameStatusBean; player: string | null }) {
  const self = player ? game.players[player] : undefined;
  const pinged = !!self?.pinged;
  const needsAttention = !!self && !self.current;
  const accent = pinged ? 'border-l-4 border-blood' : needsAttention ? 'border-l-4 border-accent/40' : 'border-l-4 border-transparent';
  const seats = game.seating.length ? game.seating : Object.keys(game.players);

  return (
    <li className={`border-b border-line/50 ${accent}`}>
      <Link to={pathForGame(game.gameId)} className="block no-underline text-ink hover:bg-hover">
        <div className="flex items-center justify-between gap-2 px-2 pt-2">
          <span className="font-bold break-words flex items-center gap-1 min-w-0">
            {pinged && <TriangleAlert size={12} className="text-blood shrink-0" />}
            {!pinged && needsAttention && <Bell size={12} className="shrink-0" />}
            <span className="truncate">{game.name}</span>
          </span>
          {game.turn && <span className="text-xs text-ink-muted whitespace-nowrap shrink-0">{game.turn}</span>}
        </div>
        <div className="mt-1 mb-2 mx-2 rounded border border-line/50 overflow-hidden">
          {seats.map((name) => (
            <DetailSeat
              key={name}
              name={name}
              status={game.players[name]}
              active={name === game.activePlayer}
              isSelf={name === player}
              hasEdge={!!game.edge && name === game.edge}
            />
          ))}
        </div>
      </Link>
    </li>
  );
}

const EMPTY: GamesSummary = { games: [], tournament: [], ousted: [] };

function DensityToggle({ value, onChange }: { value: Density; onChange: (d: Density) => void }) {
  const opts: { id: Density; label: string; Icon: typeof Rows3 }[] = [
    { id: 'compact', label: 'Compact view', Icon: Rows3 },
    { id: 'detailed', label: 'Detailed view', Icon: StretchHorizontal },
  ];
  return (
    <div className="flex items-center gap-0.5">
      {opts.map(({ id, label, Icon }) => (
        <button
          key={id}
          type="button"
          aria-label={label}
          aria-pressed={value === id}
          title={label}
          onClick={() => onChange(id)}
          className={`rounded p-1 border ${
            value === id
              ? 'border-line-accent bg-accent text-surface'
              : 'border-transparent text-ink-muted hover:bg-hover hover:text-ink'
          }`}
        >
          <Icon size={14} />
        </button>
      ))}
    </div>
  );
}

export function GamesPanel() {
  const { player } = useAuth();
  const { data: summary = EMPTY } = useQuery({
    queryKey: ['main-games'],
    queryFn: () => api.get<GamesSummary>('/main/games'),
  });
  const [activeTab, setActiveTab] = useState<TabId>('myGames');
  const [density, setDensity] = useState<Density>(loadDensity);
  const tab = TABS.find((t) => t.id === activeTab)!;
  const games = summary[tab.field];

  useEffect(() => {
    try {
      localStorage.setItem(DENSITY_KEY, density);
    } catch {
      /* private mode — fine, just don't persist */
    }
  }, [density]);

  const listRef = useRef<HTMLDivElement>(null);
  useSimpleTooltips(listRef, [summary, activeTab, density]);

  return (
    // Content-height, not flex-1: with a handful of games this card would
    // otherwise stretch to the (chat-driven) column height and leave a big
    // empty panel. Cap it so a long list scrolls internally instead.
    <Card className="flex flex-col min-h-0 overflow-hidden max-h-[45vh] lg:max-h-full">
      <CardHeader className="flex items-center justify-between gap-2">
        <CardTitle>Games List</CardTitle>
        <DensityToggle value={density} onChange={setDensity} />
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
            {games.map((game) =>
              density === 'detailed' ? (
                <DetailedGameCard key={game.name} game={game} player={player} />
              ) : (
                <GameRow key={game.name} game={game} player={player} showSeatRow={tab.showSeatRow} />
              ),
            )}
          </ul>
        )}
      </div>
    </Card>
  );
}
