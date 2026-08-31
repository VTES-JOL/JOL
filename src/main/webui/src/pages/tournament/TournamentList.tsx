import type { TournamentMetadata } from '../../api/types';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
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
    `jt:w-full jt:text-left jt:px-3 jt:py-2 jt:border-b jt:border-line jt:flex jt:items-center jt:justify-between jt:gap-2 jt:transition-colors ${
      active ? 'jt:bg-accent/10 jt:text-ink' : 'jt:text-ink-secondary jt:hover:bg-hover'
    }`;

  return (
    <Panel title="Tournaments">
      <div className="jt:flex-1 jt:min-h-0 jt:overflow-y-auto">
        {tournaments.map((t) => {
          const active = selection?.type === 'open' && selection.name === t.name;
          return (
            <button key={t.name} type="button" className={rowClass(active)} onClick={() => onSelect({ type: 'open', name: t.name })}>
              <span className="jt:flex jt:items-center jt:gap-2 jt:min-w-0">
                <Badge variant="format">{t.deckFormat}</Badge>
                <span className="jt:truncate">{t.name}</span>
                <Badge variant={t.registered ? 'online' : 'muted'}>{t.registered ? 'Registered' : 'Open'}</Badge>
              </span>
              <span className="jt:shrink-0 jt:text-xs jt:text-ink-muted">Closes {relativeTime(t.registrationEndTime)}</span>
            </button>
          );
        })}
        {finalsInvites.map((t) => {
          const active = selection?.type === 'finals' && selection.name === t.name;
          return (
            <button key={t.name} type="button" className={rowClass(active)} onClick={() => onSelect({ type: 'finals', name: t.name })}>
              <span className="jt:flex jt:items-center jt:gap-2 jt:min-w-0">
                <Badge variant="blood">Finals</Badge>
                <span className="jt:truncate">{t.name}</span>
              </span>
            </button>
          );
        })}
        {isEmpty && <p className="jt:px-3 jt:py-2 jt:text-xs jt:text-ink-muted">No tournaments available.</p>}
      </div>
    </Panel>
  );
}
