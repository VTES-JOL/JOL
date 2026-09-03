import { useEffect, useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Droplet, Gamepad2, Search, Swords } from 'lucide-react';
import { api } from '../../api/client';
import type { ActiveGamePlayer, GameSummary } from '../../api/types';
import { pathForGame } from '../../routes';
import { parseGameTitle } from '../../utils/gameTitle';
import { relativeTime } from '../../utils/relativeTime';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { EmptyState } from '../../components/ui/EmptyState';

// Active games' turn/pool/VP fields update on every in-game action, but this
// tab never joins any individual game's WS room (it's watching many games at
// once) — so those per-game pushes never reach it. Poll to keep the live
// state reasonably fresh; the payload is all in-memory reads server-side.
const ACTIVE_GAMES_POLL_MS = 20_000;
// ~250 active games during a tournament; render a screenful and grow on scroll.
const PAGE_SIZE = 48;
const LOAD_AHEAD_PX = 900;

type SortKey = 'active' | 'name' | 'round';

const SORT_OPTIONS: { value: SortKey; label: string }[] = [
  { value: 'active', label: 'Recently active' },
  { value: 'name', label: 'Name' },
  { value: 'round', label: 'Furthest along' },
];

function msValue(iso: string): number {
  const t = new Date(iso).getTime();
  return Number.isNaN(t) ? 0 : t;
}

function formatVp(vp: number): string {
  return Number.isInteger(vp) ? String(vp) : vp.toFixed(1);
}

function PlayerRow({
  player,
  seat,
  active,
  hasEdge,
}: {
  player: ActiveGamePlayer;
  seat: number;
  active: boolean;
  hasEdge: boolean;
}) {
  return (
    <div
      className={[
        'grid grid-cols-[1.4rem_minmax(0,1fr)_auto_auto] items-center gap-2 px-3 py-1.5 text-xs border-t border-line/50 first:border-t-0',
        active ? 'bg-arcane/10' : player.ousted ? 'text-ink-muted' : '',
      ].join(' ')}
    >
      <span className="justify-self-center tabular-nums text-ink-muted">{seat}</span>

      <span className={`truncate ${active ? 'font-semibold text-arcane-soft' : ''}`} title={player.name}>
        {player.name}
        {hasEdge && <Swords size={11} className="ml-1 inline align-[-1px] text-gold-soft" aria-label="has the edge" />}
      </span>

      <span className="whitespace-nowrap tabular-nums text-right">
        {player.ousted ? (
          <span className="rounded bg-blood/15 px-1.5 py-0.5 text-[10px] font-bold text-blood-soft">OUT</span>
        ) : (
          <span className="inline-flex items-center gap-0.5">
            <Droplet size={11} className="text-blood-soft" />
            {player.pool}
          </span>
        )}
      </span>

      <span className="w-12 whitespace-nowrap text-right tabular-nums">
        {player.vp > 0 ? <span className="font-semibold text-gold-soft">{formatVp(player.vp)} VP</span> : <span className="text-ink-muted">–</span>}
      </span>
    </div>
  );
}

function ActiveGameCard({ game }: { game: GameSummary }) {
  const { tournament, title, sub } = parseGameTitle(game.gameName);

  return (
    <Link
      to={pathForGame(game.gameId)}
      className="flex flex-col rounded-lg border border-line bg-panel/40 overflow-hidden transition-colors hover:border-line-accent"
    >
      <div className="px-3 py-2 bg-panel/60 border-b border-line">
        <div className="flex items-start justify-between gap-2">
          <div className="text-sm font-semibold text-ink leading-tight">
            {title}
            {sub && <span className="font-normal text-ink-muted"> · {sub}</span>}
          </div>
          <Badge variant={tournament ? 'format' : 'muted'}>{tournament ? 'Tournament' : 'Casual'}</Badge>
        </div>
        <div className="mt-1 flex flex-wrap items-center gap-x-2 text-[11px] text-ink-muted">
          <Badge variant="format">{game.format}</Badge>
          <span>Turn {game.round || '–'}</span>
          <span title={new Date(game.timestamp).toISOString()}>· active {relativeTime(game.timestamp)}</span>
        </div>
      </div>

      <div className="flex flex-col">
        {game.players.map((p, i) => (
          <PlayerRow
            key={p.name || i}
            player={p}
            seat={i + 1}
            active={p.name === game.activePlayer}
            hasEdge={!!game.edge && p.name === game.edge}
          />
        ))}
      </div>
    </Link>
  );
}

export function ActiveGamesTab() {
  const { data: games = [] } = useQuery({
    queryKey: ['watch', 'active'],
    queryFn: () => api.get<GameSummary[]>('/watch/active'),
    refetchInterval: ACTIVE_GAMES_POLL_MS,
  });

  const [query, setQuery] = useState('');
  const [sort, setSort] = useState<SortKey>('active');
  const [limit, setLimit] = useState(PAGE_SIZE);
  const scrollRef = useRef<HTMLDivElement>(null);

  const visible = useMemo(() => {
    const needle = query.trim().toLowerCase();
    const list = needle
      ? games.filter((g) => {
          const haystack = [g.gameName, ...g.players.map((p) => p.name)].filter(Boolean).join(' ').toLowerCase();
          return haystack.includes(needle);
        })
      : [...games];

    switch (sort) {
      case 'active':
        list.sort((a, b) => msValue(b.timestamp) - msValue(a.timestamp));
        break;
      case 'name':
        list.sort((a, b) => a.gameName.localeCompare(b.gameName, undefined, { sensitivity: 'base' }));
        break;
      case 'round':
        list.sort((a, b) => b.round - a.round);
        break;
    }
    return list;
  }, [games, query, sort]);

  const filterKey = `${query} ${sort}`;
  const [prevFilterKey, setPrevFilterKey] = useState(filterKey);
  if (filterKey !== prevFilterKey) {
    setPrevFilterKey(filterKey);
    setLimit(PAGE_SIZE);
  }
  useEffect(() => {
    scrollRef.current?.scrollTo?.({ top: 0 });
  }, [filterKey]);

  const shown = visible.slice(0, limit);
  const hasMore = limit < visible.length;

  const visibleLenRef = useRef(0);
  visibleLenRef.current = visible.length;
  const growIfNearBottom = () => {
    const el = scrollRef.current;
    if (!el) return;
    if (el.scrollHeight - el.scrollTop - el.clientHeight < LOAD_AHEAD_PX) {
      setLimit((n) => (n < visibleLenRef.current ? n + PAGE_SIZE : n));
    }
  };
  useEffect(growIfNearBottom, [shown.length, filterKey]);

  const countLabel = query.trim()
    ? `${visible.length} of ${games.length} games`
    : `${games.length} ${games.length === 1 ? 'game' : 'games'} in progress`;

  return (
    <Panel title="Active Games">
      <div ref={scrollRef} onScroll={growIfNearBottom} className="flex-1 min-h-0 overflow-auto">
        <div className="sticky top-0 z-10 flex flex-wrap items-center gap-2 border-b border-line bg-panel/80 px-3 py-2 backdrop-blur">
          <div className="flex-1 min-w-[200px] max-w-[320px]">
            <Input
              id="active-games-filter"
              size="sm"
              srLabel="Filter active games"
              placeholder="Filter by game or player…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              right={<Search size={13} className="text-ink-muted" />}
            />
          </div>
          <div className="w-auto">
            <Select
              id="active-games-sort"
              size="sm"
              srLabel="Sort active games"
              value={sort}
              onChange={(e) => setSort(e.target.value as SortKey)}
            >
              {SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </Select>
          </div>
          <span className="ml-auto whitespace-nowrap text-xs text-ink-muted">{countLabel}</span>
        </div>

        {visible.length === 0 ? (
          <EmptyState
            icon={Gamepad2}
            title={games.length === 0 ? 'No games in progress' : 'No games match your filter.'}
            description={games.length === 0 ? 'Games show up here while they’re being played.' : undefined}
          />
        ) : (
          <>
            <div className="grid gap-3 p-3 [grid-template-columns:repeat(auto-fill,minmax(300px,1fr))] items-start">
              {shown.map((g) => (
                <ActiveGameCard key={g.gameId} game={g} />
              ))}
            </div>
            {hasMore && (
              <div className="p-4 text-center text-xs text-ink-muted">
                <button
                  type="button"
                  className="rounded border border-line px-3 py-1 hover:bg-hover hover:text-ink"
                  onClick={() => setLimit((n) => n + PAGE_SIZE)}
                >
                  Show more
                </button>
                <span className="ml-2">
                  {shown.length} of {visible.length}
                </span>
              </div>
            )}
          </>
        )}
      </div>
    </Panel>
  );
}
