import { useMemo, useRef, useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Gamepad2, Gavel } from 'lucide-react';
import { api } from '../api/client';
import type { JudgeQueue, JudgeQueueEntry, JudgeRequestCategory } from '../api/types';
import { pathForGame } from '../routes';
import { TabBar, type TabDef } from '../components/TabBar';
import { Panel } from '../components/ui/Panel';
import { Badge } from '../components/ui/Badge';
import { Select } from '../components/ui/Select';
import { EmptyState } from '../components/ui/EmptyState';
import { Spinner } from '../components/ui/Spinner';
import { MessageContent } from '../components/MessageContent';
import { useCardTooltips } from '../hooks/useCardTooltips';
import { categoryLabel, JUDGE_CATEGORY_OPTIONS } from './game/JudgeRequestModal';
import { adminTimestamp } from './admin/adminFormatting';
import './JudgePage.css';

type JudgeTab = 'open' | 'history';
type CategoryFilter = JudgeRequestCategory | 'ALL';

// Same UTC "3-Aug-26 14:05 UTC" format the admin tables use — judges span
// timezones and the ruling history is a long-lived record.
function ts(iso: string | null): string {
  return iso ? adminTimestamp(iso) : '';
}

export function JudgePage() {
  const [tab, setTab] = useState<JudgeTab>('open');
  const [categoryFilter, setCategoryFilter] = useState<CategoryFilter>('ALL');

  const { data, isLoading } = useQuery({
    queryKey: ['judge', 'requests'],
    queryFn: () => api.get<JudgeQueue>('/judge/requests'),
  });

  const open = useMemo(() => {
    const rows = data?.open ?? [];
    return categoryFilter === 'ALL' ? rows : rows.filter((r) => r.category === categoryFilter);
  }, [data?.open, categoryFilter]);
  const history = useMemo(() => {
    const rows = data?.history ?? [];
    return categoryFilter === 'ALL' ? rows : rows.filter((r) => r.category === categoryFilter);
  }, [data?.history, categoryFilter]);

  // Tab badge tracks the real outstanding count, not the filtered view.
  const openCount = data?.open.length ?? 0;
  const historyCount = data?.history.length ?? 0;
  const filtering = categoryFilter !== 'ALL';

  const TABS: TabDef<JudgeTab>[] = [
    { id: 'open', label: 'Outstanding', badge: openCount || undefined },
    { id: 'history', label: 'Ruling history' },
  ];

  const categorySelect = (
    <Select
      size="sm"
      aria-label="Filter by category"
      value={categoryFilter}
      onChange={(e) => setCategoryFilter(e.target.value as CategoryFilter)}
    >
      <option value="ALL">All categories</option>
      {JUDGE_CATEGORY_OPTIONS.map((o) => (
        <option key={o.value} value={o.value}>
          {o.label}
        </option>
      ))}
    </Select>
  );

  const count = (shown: number, total: number) => (filtering ? `${shown} of ${total}` : `${total}`);

  return (
    <div className="flex flex-col flex-1 min-h-0 p-4 bg-base text-ink">
      <h1 className="flex items-center gap-2 text-lg font-serif mb-3">
        <Gavel size={18} /> Judges
      </h1>
      <TabBar tabs={TABS} active={tab} onChange={setTab} />

      <div className="flex-1 min-h-0 mt-3 flex flex-col">
        {isLoading ? (
          <div className="flex flex-1 items-center justify-center">
            <Spinner />
          </div>
        ) : tab === 'open' ? (
          <Panel title={`Outstanding · ${count(open.length, openCount)}`} right={categorySelect}>
            <div className="flex-1 min-h-0 overflow-auto p-3 space-y-3 max-w-4xl">
              {open.length === 0 ? (
                <EmptyState
                  icon={Gavel}
                  title={filtering ? 'Nothing in this category' : 'No outstanding requests'}
                  description={
                    filtering
                      ? 'Clear the filter to see other outstanding requests.'
                      : 'Requests players raise from a game appear here, oldest first.'
                  }
                />
              ) : (
                open.map((r) => <OpenRow key={r.id} row={r} />)
              )}
            </div>
          </Panel>
        ) : (
          <Panel title={`Ruling history · ${count(history.length, historyCount)}`} right={categorySelect}>
            <div className="flex-1 min-h-0 overflow-auto p-3 space-y-3 max-w-4xl">
              {history.length === 0 ? (
                <EmptyState
                  icon={Gavel}
                  title={filtering ? 'Nothing in this category' : 'No rulings yet'}
                  description={
                    filtering
                      ? 'Clear the filter to see other rulings.'
                      : 'Resolved requests are kept here, most recent first.'
                  }
                />
              ) : (
                history.map((r) => <HistoryRow key={r.id} row={r} />)
              )}
            </div>
          </Panel>
        )}
      </div>
    </div>
  );
}

function RowShell({ row, children }: { row: JudgeQueueEntry; children: ReactNode }) {
  return (
    <div className="rounded-lg border border-line-accent bg-surface/70 p-3">
      <div className="flex items-start justify-between gap-3 flex-wrap">
        <div className="flex items-center gap-2 flex-wrap">
          <span className="flex items-center gap-1 font-medium text-ink">
            <Gamepad2 size={13} className="shrink-0 text-ink-muted" />
            {row.gameName}
          </span>
          {row.tournament && (
            <Badge variant="format" size="xs">
              {row.tournamentName ?? 'Tournament'}
            </Badge>
          )}
          <Badge variant="muted" size="xs">
            {categoryLabel(row.category)}
          </Badge>
        </div>
        <span className="text-xs text-ink-muted">{ts(row.createdAt)}</span>
      </div>
      <div className="mt-1 text-xs text-ink-muted">
        called by <span className="text-ink-secondary">{row.requester}</span>
      </div>
      {children}
    </div>
  );
}

// Renders a parsed message (request details / ruling notes). `judge-prose`
// gives the `[Card Name]` links the same dotted-underline hover affordance the
// Help page uses — this whole page is about card rulings, so the links being
// visibly live matters here.
function Prose({ message, className }: { message: string; className?: string }) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [message]);
  return (
    <div ref={ref} className={`judge-prose whitespace-pre-wrap ${className ?? ''}`}>
      <MessageContent message={message} viewer={null} />
    </div>
  );
}

function OpenRow({ row }: { row: JudgeQueueEntry }) {
  return (
    <RowShell row={row}>
      <Prose message={row.details} className="mt-2 rounded border border-line bg-surface/60 p-2 text-sm" />
      <div className="mt-2 flex items-center justify-between gap-2">
        {row.canRule && row.gameId ? (
          <Link to={pathForGame(row.gameId)} className="text-xs text-accent underline">
            Go to game to rule →
          </Link>
        ) : row.tournament ? (
          <span className="text-xs text-ink-muted/80">Tournament ruling — pending judge assignment support</span>
        ) : (
          <span className="text-xs text-ink-muted/80">You are playing in this game</span>
        )}
      </div>
    </RowShell>
  );
}

function HistoryRow({ row }: { row: JudgeQueueEntry }) {
  return (
    <RowShell row={row}>
      <Prose message={row.details} className="mt-2 rounded border border-line bg-surface/60 p-2 text-sm" />
      <div className="mt-2 rounded border border-online/30 bg-online/5 p-2 text-sm">
        <div className="text-xs text-ink-muted mb-1">
          resolved by <span className="text-ink-secondary">{row.resolvedBy}</span> · {ts(row.resolvedAt)}
        </div>
        {row.resolution ? (
          <Prose message={row.resolution} />
        ) : (
          <span className="text-ink-muted italic">No notes recorded.</span>
        )}
      </div>
    </RowShell>
  );
}
