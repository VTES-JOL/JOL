import { PlusCircle, User } from 'lucide-react';
import type { GameStatusBean } from '../../api/types';
import { relativeTime } from '../../utils/relativeTime';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';

const REL_LABEL: Record<string, string> = { OWNER: 'Owner', REGISTERED: 'Registered', INVITED: 'Invited', OPEN: 'Open' };
const REL_CLASS: Record<string, string> = {
  OWNER: 'text-accent',
  REGISTERED: 'text-online',
  INVITED: 'text-gold',
  OPEN: 'text-ink-muted',
};

function GameListItem({ game, selected, onSelect }: { game: GameStatusBean; selected: boolean; onSelect: () => void }) {
  const relLabel = (game.playerRelationship && REL_LABEL[game.playerRelationship]) || '';
  const relClass = (game.playerRelationship && REL_CLASS[game.playerRelationship]) || 'text-ink-muted';
  const registeredCount = game.registrations.filter((r) => r.registered).length;
  const totalCount = game.registrations.length;

  return (
    <button
      type="button"
      onClick={onSelect}
      className={`w-full text-left px-3 py-2 border-b border-line transition-colors ${
        selected ? 'bg-accent/10 text-ink' : 'text-ink-secondary hover:bg-hover'
      }`}
    >
      <div className="flex justify-between items-start gap-2">
        <span className="font-semibold text-ink break-words">{game.name}</span>
        <Badge variant={game.visibility === 'PUBLIC' ? 'online' : 'muted'}>{game.visibility}</Badge>
      </div>
      <div className="flex justify-between items-center mt-1">
        <Badge variant="format">{game.format}</Badge>
        <span className={`text-xs ${relClass}`}>{relLabel}</span>
      </div>
      {(game.visibility === 'PUBLIC' || totalCount > 0) && (
        <div className="flex justify-between items-center mt-1 text-xs text-ink-muted">
          <span className="flex items-center gap-1">
            {totalCount > 0 && (
              <>
                {registeredCount}/{totalCount} <User size={12} />
              </>
            )}
          </span>
          {game.visibility === 'PUBLIC' && game.created && (
            <span>
              closes {relativeTime(new Date(new Date(game.created).getTime() + 5 * 24 * 60 * 60 * 1000).toISOString())}
            </span>
          )}
        </div>
      )}
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
      right={
        <Button variant="secondary" size="sm" icon={<PlusCircle size={14} />} onClick={onNew}>
          New
        </Button>
      }
    >
      <div className="flex-1 min-h-0 overflow-y-auto">
        {games.map((g) => (
          <GameListItem key={g.name} game={g} selected={g.name === selectedName} onSelect={() => onSelect(g)} />
        ))}
      </div>
    </Panel>
  );
}
