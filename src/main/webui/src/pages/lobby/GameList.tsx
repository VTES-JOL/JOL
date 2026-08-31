import { PlusCircle, User } from 'lucide-react';
import type { GameStatusBean } from '../../api/types';
import { relativeTime } from '../../utils/relativeTime';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';

const REL_LABEL: Record<string, string> = { OWNER: 'Owner', REGISTERED: 'Registered', INVITED: 'Invited', OPEN: 'Open' };
const REL_CLASS: Record<string, string> = {
  OWNER: 'jt:text-accent',
  REGISTERED: 'jt:text-online',
  INVITED: 'jt:text-gold',
  OPEN: 'jt:text-ink-muted',
};

function GameListItem({ game, selected, onSelect }: { game: GameStatusBean; selected: boolean; onSelect: () => void }) {
  const relLabel = (game.playerRelationship && REL_LABEL[game.playerRelationship]) || '';
  const relClass = (game.playerRelationship && REL_CLASS[game.playerRelationship]) || 'jt:text-ink-muted';
  const registeredCount = game.registrations.filter((r) => r.registered).length;
  const totalCount = game.registrations.length;

  return (
    <button
      type="button"
      onClick={onSelect}
      className={`jt:w-full jt:text-left jt:px-3 jt:py-2 jt:border-b jt:border-line jt:transition-colors ${
        selected ? 'jt:bg-accent/10 jt:text-ink' : 'jt:text-ink-secondary jt:hover:bg-hover'
      }`}
    >
      <div className="jt:flex jt:justify-between jt:items-start jt:gap-2">
        <span className="jt:font-semibold jt:text-ink jt:break-words">{game.name}</span>
        <Badge variant={game.visibility === 'PUBLIC' ? 'online' : 'muted'}>{game.visibility}</Badge>
      </div>
      <div className="jt:flex jt:justify-between jt:items-center jt:mt-1">
        <Badge variant="format">{game.format}</Badge>
        <span className={`jt:text-xs ${relClass}`}>{relLabel}</span>
      </div>
      {(game.visibility === 'PUBLIC' || totalCount > 0) && (
        <div className="jt:flex jt:justify-between jt:items-center jt:mt-1 jt:text-xs jt:text-ink-muted">
          <span className="jt:flex jt:items-center jt:gap-1">
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
      <div className="jt:flex-1 jt:min-h-0 jt:overflow-y-auto">
        {games.map((g) => (
          <GameListItem key={g.name} game={g} selected={g.name === selectedName} onSelect={() => onSelect(g)} />
        ))}
      </div>
    </Panel>
  );
}
