import { Gamepad2, Globe, Lock, PlusCircle, Users } from 'lucide-react';
import type { GameStatusBean } from '../../api/types';
import type { BadgeVariant } from '../../components/ui/Badge';
import { relativeTime } from '../../utils/relativeTime';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';

// GameCleanUp.STALE_LOBBY_DAYS — a public lobby game is removed this many days
// after its last activity (game.updated). Deck registration / invites bump it.
const STALE_LOBBY_DAYS = 5;

const REL_BADGE: Record<string, { label: string; variant: BadgeVariant }> = {
  OWNER: { label: 'Owner', variant: 'accent' },
  REGISTERED: { label: 'Registered', variant: 'online' },
  INVITED: { label: 'Invited', variant: 'gold' },
  OPEN: { label: 'Open', variant: 'muted' },
};

function GameListItem({ game, selected, onSelect }: { game: GameStatusBean; selected: boolean; onSelect: () => void }) {
  const rel = game.playerRelationship ? REL_BADGE[game.playerRelationship] : undefined;
  const registeredCount = game.registrations.filter((r) => r.registered).length;
  const totalCount = game.registrations.length;
  const isPublic = game.visibility === 'PUBLIC';
  const closesAt =
    isPublic && game.updated
      ? new Date(new Date(game.updated).getTime() + STALE_LOBBY_DAYS * 86_400_000).toISOString()
      : null;

  return (
    <button
      type="button"
      onClick={onSelect}
      className={`w-full text-left px-3 py-2.5 border-b border-line transition-colors ${
        selected ? 'bg-accent/10' : 'hover:bg-hover'
      }`}
    >
      <div className="flex items-start justify-between gap-2">
        <span className="font-semibold text-ink break-words">{game.name}</span>
        {rel && <Badge variant={rel.variant}>{rel.label}</Badge>}
      </div>
      <div className="mt-1 flex flex-wrap items-center gap-x-2 gap-y-0.5 text-xs text-ink-muted">
        <Badge variant="format">{game.format}</Badge>
        <span className="inline-flex items-center gap-1">
          {isPublic ? <Globe size={11} /> : <Lock size={11} />}
          {isPublic ? 'Public' : 'Private'}
        </span>
        {totalCount > 0 && (
          <span className="inline-flex items-center gap-1">
            <Users size={11} />
            {registeredCount}/{totalCount} registered
          </span>
        )}
        {closesAt && <span>· closes {relativeTime(closesAt)}</span>}
      </div>
    </button>
  );
}

export function GameList({
  games,
  selectedName,
  onSelect,
  onNew,
}: {
  games: GameStatusBean[];
  selectedName: string | null;
  onSelect: (game: GameStatusBean) => void;
  onNew: () => void;
}) {
  return (
    <Panel
      title="Games"
      // The MasterDetailView tab strip already labels this pane on mobile.
      titleClassName="hidden lg:block"
      headerClassName="max-lg:justify-end"
      right={
        <Button variant="secondary" size="sm" icon={<PlusCircle size={14} />} onClick={onNew}>
          New
        </Button>
      }
    >
      <div className="flex-1 min-h-0 overflow-y-auto">
        {games.length === 0 ? (
          <EmptyState
            icon={Gamepad2}
            title="No games yet"
            description="Create one, or wait for a public game to open."
          />
        ) : (
          games.map((g) => (
            <GameListItem key={g.name} game={g} selected={g.name === selectedName} onSelect={() => onSelect(g)} />
          ))
        )}
      </div>
    </Panel>
  );
}
