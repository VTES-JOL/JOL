import { useMemo, useRef, useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Gavel } from 'lucide-react';
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

type JudgeTab = 'open' | 'history';

const TS_FORMAT = new Intl.DateTimeFormat('en-GB', {
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
});

function ts(iso: string | null): string {
  return iso ? TS_FORMAT.format(new Date(iso)) : '';
}

export function JudgePage() {
  const [tab, setTab] = useState<JudgeTab>('open');
  const [categoryFilter, setCategoryFilter] = useState<JudgeRequestCategory | 'ALL'>('ALL');

  const { data, isLoading } = useQuery({
    queryKey: ['judge', 'requests'],
    queryFn: () => api.get<JudgeQueue>('/judge/requests'),
  });

  const history = useMemo(() => {
    const rows = data?.history ?? [];
    return categoryFilter === 'ALL' ? rows : rows.filter((r) => r.category === categoryFilter);
  }, [data?.history, categoryFilter]);

  const openCount = data?.open.length ?? 0;

  const TABS: TabDef<JudgeTab>[] = [
    { id: 'open', label: 'Outstanding', badge: openCount || undefined },
    { id: 'history', label: 'Ruling history' },
  ];

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
          <Panel title="Outstanding requests">
            <div className="flex-1 min-h-0 overflow-auto p-3 space-y-3">
              {(data?.open ?? []).length === 0 ? (
                <EmptyState icon={Gavel} title="No outstanding requests" description="Requests players raise from a game appear here, oldest first." />
              ) : (
                data!.open.map((r) => <OpenRow key={r.id} row={r} />)
              )}
            </div>
          </Panel>
        ) : (
          <Panel
            title="Ruling history"
            right={
              <Select
                size="sm"
                aria-label="Filter by category"
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value as JudgeRequestCategory | 'ALL')}
              >
                <option value="ALL">All categories</option>
                {JUDGE_CATEGORY_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </Select>
            }
          >
            <div className="flex-1 min-h-0 overflow-auto p-3 space-y-3">
              {history.length === 0 ? (
                <EmptyState icon={Gavel} title="No rulings yet" description="Resolved requests are kept here, most recent first." />
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
          <span className="font-medium text-ink">{row.gameName}</span>
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

function Details({ message }: { message: string }) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [message]);
  return (
    <div ref={ref} className="mt-2 rounded border border-line bg-surface/60 p-2 text-sm whitespace-pre-wrap">
      <MessageContent message={message} viewer={null} />
    </div>
  );
}

function OpenRow({ row }: { row: JudgeQueueEntry }) {
  return (
    <RowShell row={row}>
      <Details message={row.details} />
      <div className="mt-2 flex items-center justify-between gap-2">
        {row.canRule ? (
          <Link
            to={row.gameId ? pathForGame(row.gameId) : '#'}
            className="text-xs text-accent underline"
          >
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
      <Details message={row.details} />
      <div className="mt-2 rounded border border-online/30 bg-online/5 p-2 text-sm">
        <div className="text-xs text-ink-muted mb-1">
          resolved by <span className="text-ink-secondary">{row.resolvedBy}</span> · {ts(row.resolvedAt)}
        </div>
        {row.resolution ? (
          <MessageContent message={row.resolution} viewer={null} />
        ) : (
          <span className="text-ink-muted italic">No notes recorded.</span>
        )}
      </div>
    </RowShell>
  );
}
