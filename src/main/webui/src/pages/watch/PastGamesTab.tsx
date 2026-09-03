import { useEffect, useMemo, useRef, useState } from 'react';
import { Download, Search, Trophy } from 'lucide-react';
import { api } from '../../api/client';
import type { GameHistory, PlayerResult } from '../../api/types';
import { displayDeckName } from '../../utils/deckName';
import { useQuery } from '@tanstack/react-query';
import { Panel } from '../../components/ui/Panel';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Badge } from '../../components/ui/Badge';
import { EmptyState } from '../../components/ui/EmptyState';
import { relativeTime } from '../../utils/relativeTime';

const EXACT_FORMAT = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'UTC',
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

type SortKey = 'newest' | 'oldest' | 'vp' | 'players';

const SORT_OPTIONS: { value: SortKey; label: string }[] = [
  { value: 'newest', label: 'Newest first' },
  { value: 'oldest', label: 'Oldest first' },
  { value: 'vp', label: 'Biggest win' },
  { value: 'players', label: 'Most players' },
];

const TOURNAMENT_NAME = /^(.*): Round (\d+) - Table (\d+)$/;

function downloadCsv(data: string, filename: string) {
  const blob = new Blob([data], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

const csvCell = (v: string | number): string => {
  const s = String(v ?? '');
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
};

// Built client-side from the history already on the page — the old
// /admin/export/games.csv endpoint is ADMIN-only, so it 403'd for every
// non-admin who could see this button.
function historyToCsv(history: GameHistory[]): string {
  const rows = [['Game', 'Ended', 'Started', 'Rank', 'Player', 'Deck', 'VP', 'Game Win'].join(',')];
  for (const g of history) {
    sortResults(g.results).forEach((r, i) => {
      rows.push(
        [
          g.name,
          g.ended,
          g.started,
          i + 1,
          r.playerName,
          displayDeckName(r.deckName) ?? '',
          r.victoryPoints || 0,
          r.gameWin ? 'Y' : '',
        ]
          .map(csvCell)
          .join(','),
      );
    });
  }
  return rows.join('\n');
}

function parseTitle(name: string): { tournament: boolean; title: string; sub: string } {
  const m = TOURNAMENT_NAME.exec(name ?? '');
  if (m) return { tournament: true, title: m[1], sub: `Round ${m[2]} · Table ${m[3]}` };
  return { tournament: false, title: name || 'Unnamed game', sub: '' };
}

function msValue(iso: string): number {
  const t = new Date(iso).getTime();
  return Number.isNaN(t) ? 0 : t;
}

function topVp(g: GameHistory): number {
  return g.results.reduce((mx, r) => Math.max(mx, r.victoryPoints || 0), 0);
}

/** Human "lasted 2 hours" style span between two instants. */
function formatSpan(startIso: string, endIso: string): string | null {
  const start = msValue(startIso);
  const end = msValue(endIso);
  if (!start || !end || end <= start) return null;
  const mins = Math.round((end - start) / 60_000);
  if (mins < 60) return `${mins} min`;
  const hours = Math.round(mins / 60);
  if (hours < 48) return `${hours} hr`;
  return `${Math.round(hours / 24)} days`;
}

function sortResults(results: PlayerResult[]): PlayerResult[] {
  return [...results].sort(
    (a, b) =>
      Number(b.gameWin) - Number(a.gameWin) ||
      (b.victoryPoints || 0) - (a.victoryPoints || 0) ||
      (a.playerName || '').localeCompare(b.playerName || ''),
  );
}

function PastGameCard({ game }: { game: GameHistory }) {
  const { tournament, title, sub } = parseTitle(game.name);
  const results = sortResults(game.results);
  const endedMs = msValue(game.ended);
  const span = formatSpan(game.started, game.ended);

  return (
    <div className="flex flex-col rounded-lg border border-line bg-panel/40 overflow-hidden">
      <div className="px-3 py-2 bg-panel/60 border-b border-line">
        <div className="flex items-start justify-between gap-2">
          <div className="text-sm font-semibold text-ink leading-tight">
            {title}
            {sub && <span className="font-normal text-ink-muted"> · {sub}</span>}
          </div>
          <Badge variant={tournament ? 'format' : 'muted'}>{tournament ? 'Tournament' : 'Casual'}</Badge>
        </div>
        <div className="mt-1 text-[11px] text-ink-muted">
          <span title={endedMs ? `${EXACT_FORMAT.format(new Date(game.ended))} UTC` : undefined}>
            {endedMs ? `ended ${relativeTime(game.ended)}` : `ended ${game.ended || 'unknown'}`}
          </span>
          {span && <span> · lasted {span}</span>}
          <span> · {results.length === 1 ? '1 player' : `${results.length} players`}</span>
        </div>
      </div>

      <div className="flex flex-col">
        {results.map((r, i) => {
          const vp = r.victoryPoints || 0;
          const winner = r.gameWin;
          const deck = displayDeckName(r.deckName) ?? '';
          return (
            <div
              key={r.playerName || i}
              className={[
                'grid grid-cols-[1.4rem_minmax(0,1fr)_auto] items-center gap-2 px-3 py-1.5 text-xs',
                i > 0 ? 'border-t border-line/50' : '',
                winner ? 'bg-gold/10' : vp === 0 ? 'text-ink-muted' : '',
              ]
                .filter(Boolean)
                .join(' ')}
            >
              {winner ? (
                <Trophy size={14} className="justify-self-center text-gold-soft" />
              ) : (
                <span className="justify-self-center tabular-nums text-ink-muted">{i + 1}</span>
              )}

              <div className="min-w-0 flex flex-col leading-tight">
                <span
                  className={`font-semibold truncate ${winner ? 'text-gold-soft' : ''}`}
                  title={r.playerName}
                >
                  {r.playerName || '—'}
                </span>
                {deck ? (
                  <span className="truncate text-[11px] text-ink-muted" title={deck}>
                    {deck}
                  </span>
                ) : (
                  <span className="truncate text-[11px] italic text-ink-muted/70">no deck recorded</span>
                )}
              </div>

              <span className="whitespace-nowrap text-right tabular-nums">
                {vp > 0 && <span className="font-semibold">{vp} VP</span>}
                {winner && (
                  <span className="ml-1 rounded bg-gold/20 px-1.5 py-0.5 text-[10px] font-bold text-gold-soft">
                    GW
                  </span>
                )}
                {vp === 0 && !winner && <span className="text-ink-muted">–</span>}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// The full history is ~1300 games (~6k player rows); rendering every card up
// front is what makes the first paint slow. Mount a screenful at a time and
// grow the window as the user scrolls toward the bottom.
const PAGE_SIZE = 60;
// Pixels from the bottom of the scroll area at which the next page loads.
const LOAD_AHEAD_PX = 900;

export function PastGamesTab() {
  const { data: history = [] } = useQuery({
    queryKey: ['watch', 'history'],
    queryFn: () => api.get<GameHistory[]>('/watch/history'),
  });

  const [query, setQuery] = useState('');
  const [sort, setSort] = useState<SortKey>('newest');
  const [limit, setLimit] = useState(PAGE_SIZE);

  const scrollRef = useRef<HTMLDivElement>(null);

  const exportCsv = () => downloadCsv(historyToCsv(history), 'past-games.csv');

  const visible = useMemo(() => {
    const needle = query.trim().toLowerCase();
    let games = needle
      ? history.filter((g) => {
          const haystack = [
            g.name,
            ...g.results.map((r) => r.playerName),
            ...g.results.map((r) => r.deckName),
          ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase();
          return haystack.includes(needle);
        })
      : [...history];

    switch (sort) {
      case 'oldest':
        games.sort((a, b) => msValue(a.ended) - msValue(b.ended));
        break;
      case 'newest':
        games.sort((a, b) => msValue(b.ended) - msValue(a.ended));
        break;
      case 'vp':
        games.sort((a, b) => topVp(b) - topVp(a));
        break;
      case 'players':
        games.sort((a, b) => b.results.length - a.results.length);
        break;
    }
    return games;
  }, [history, query, sort]);

  // Reset the window (and scroll position) whenever the filter/sort changes, so
  // a search doesn't leave you scrolled into whitespace past the new results.
  const filterKey = `${query} ${sort}`;
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

  // Grow the window as the user nears the bottom. A plain scroll handler rather
  // than IntersectionObserver — the tab sits several nested flex/overflow
  // containers deep and an element-rooted observer proved unreliable here.
  const visibleLenRef = useRef(0);
  visibleLenRef.current = visible.length;
  const growIfNearBottom = () => {
    const el = scrollRef.current;
    if (!el) return;
    if (el.scrollHeight - el.scrollTop - el.clientHeight < LOAD_AHEAD_PX) {
      setLimit((n) => (n < visibleLenRef.current ? n + PAGE_SIZE : n));
    }
  };
  // Top up after each grow / filter change if the content still doesn't reach
  // past the fold (tall screen).
  useEffect(growIfNearBottom, [shown.length, filterKey]);

  const countLabel =
    query.trim() && history.length
      ? `${visible.length} of ${history.length} games`
      : `${history.length} ${history.length === 1 ? 'game' : 'games'}`;

  return (
    <Panel
      title="Past Games"
      right={
        <Button
          variant="secondary"
          size="sm"
          icon={<Download size={14} />}
          onClick={exportCsv}
          disabled={history.length === 0}
        >
          Export CSV
        </Button>
      }
    >
      <div ref={scrollRef} onScroll={growIfNearBottom} className="flex-1 min-h-0 overflow-auto">
        <div className="sticky top-0 z-10 flex flex-wrap items-center gap-2 border-b border-line bg-panel/80 px-3 py-2 backdrop-blur">
          <div className="flex-1 min-w-[200px] max-w-[320px]">
            <Input
              id="past-games-filter"
              size="sm"
              srLabel="Filter past games"
              placeholder="Filter by game, player or deck…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              right={<Search size={13} className="text-ink-muted" />}
            />
          </div>
          <div className="w-auto">
            <Select
              id="past-games-sort"
              size="sm"
              srLabel="Sort past games"
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
            icon={Trophy}
            title={history.length === 0 ? 'No completed games yet.' : 'No games match your filter.'}
          />
        ) : (
          <>
            <div className="grid gap-3 p-3 [grid-template-columns:repeat(auto-fill,minmax(320px,1fr))] items-start">
              {shown.map((g) => (
                <PastGameCard key={`${g.name}-${g.ended}`} game={g} />
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
