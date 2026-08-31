import { Plus } from 'lucide-react';
import type { TournamentMetadata } from '../../api/types';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';

function StatusBadge({ status }: { status: string }) {
  if (status === 'ACTIVE') return <Badge variant="online">Active</Badge>;
  if (status === 'STARTING') return <Badge variant="muted">Starting</Badge>;
  return <Badge variant="format">Draft</Badge>;
}

export function TournamentAdminList({
  tournaments,
  onSelect,
  onNew,
}: {
  tournaments: TournamentMetadata[];
  onSelect: (t: TournamentMetadata) => void;
  onNew: () => void;
}) {
  return (
    <Panel
      title="Tournaments"
      right={
        <Button variant="secondary" size="sm" icon={<Plus size={14} />} onClick={onNew}>
          New
        </Button>
      }
    >
      <div className="flex-1 min-h-0 overflow-y-auto">
        {tournaments.map((t) => (
          <button
            key={t.name}
            type="button"
            className="w-full text-left px-3 py-2 border-b border-line flex justify-between items-center gap-2 text-ink-secondary hover:bg-hover"
            onClick={() => onSelect(t)}
          >
            <span className="truncate">{t.name}</span>
            <StatusBadge status={t.status} />
          </button>
        ))}
      </div>
    </Panel>
  );
}
