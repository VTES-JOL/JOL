import { Trophy } from 'lucide-react';
import type { TournamentMetadata } from '../../api/types';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { EmptyState } from '../../components/ui/EmptyState';
import { relativeTime } from '../../utils/relativeTime';

export type Selection = { type: 'open' | 'finals'; name: string } | null;

export function TournamentList({
  tournaments,
  finalsInvites,
  selection,
  onSelect,
}: {
  tournaments: TournamentMetadata[];
  finalsInvites: TournamentMetadata[];
  selection: Selection;
  onSelect: (selection: Selection) => void;
}) {
  const isEmpty = tournaments.length === 0 && finalsInvites.length === 0;

  const rowClass = (active: boolean) =>
    `w-full text-left px-3 py-2 border-b border-line flex items-center justify-between gap-2 transition-colors ${
      active ? 'bg-accent/10 text-ink' : 'text-ink-secondary hover:bg-hover'
    }`;

  return (
    <Panel title="Tournaments">
      <div className="flex-1 min-h-0 overflow-y-auto">
        {tournaments.map((t) => {
          const active = selection?.type === 'open' && selection.name === t.name;
          return (
            <button key={t.name} type="button" className={rowClass(active)} onClick={() => onSelect({ type: 'open', name: t.name })}>
              <span className="flex items-center gap-2 min-w-0">
                <Badge variant="format">{t.deckFormat}</Badge>
                <span className="truncate">{t.name}</span>
                <Badge variant={t.registered ? 'online' : 'muted'}>{t.registered ? 'Registered' : 'Open'}</Badge>
              </span>
              <span className="shrink-0 text-xs text-ink-muted">Closes {relativeTime(t.registrationEndTime)}</span>
            </button>
          );
        })}
        {finalsInvites.map((t) => {
          const active = selection?.type === 'finals' && selection.name === t.name;
          return (
            <button key={t.name} type="button" className={rowClass(active)} onClick={() => onSelect({ type: 'finals', name: t.name })}>
              <span className="flex items-center gap-2 min-w-0">
                <Badge variant="blood">Finals</Badge>
                <span className="truncate">{t.name}</span>
              </span>
            </button>
          );
        })}
        {isEmpty && (
          <EmptyState
            icon={Trophy}
            title="No tournaments available"
            description="Published tournaments open for registration appear here."
          />
        )}
      </div>
    </Panel>
  );
}
