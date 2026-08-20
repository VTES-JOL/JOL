import type { GameStatusBean } from '../../api/types';
import { relativeTime } from '../../lib/relativeTime';

const REL_LABEL: Record<string, string> = { OWNER: 'Owner', REGISTERED: 'Registered', INVITED: 'Invited', OPEN: 'Open' };
const REL_CLASS: Record<string, string> = {
  OWNER: 'text-primary',
  REGISTERED: 'text-success',
  INVITED: 'text-warning-emphasis',
  OPEN: 'text-muted',
};

function GameListItem({ game, selected, onSelect }: { game: GameStatusBean; selected: boolean; onSelect: () => void }) {
  const visClass =
    game.visibility === 'PUBLIC'
      ? 'bg-success-subtle text-success-emphasis border border-success-subtle'
      : 'bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle';
  const relLabel = (game.playerRelationship && REL_LABEL[game.playerRelationship]) || '';
  const relClass = (game.playerRelationship && REL_CLASS[game.playerRelationship]) || 'text-muted';
  const registeredCount = game.registrations.filter((r) => r.registered).length;
  const totalCount = game.registrations.length;

  return (
    <a
      href="#"
      className={`list-group-item list-group-item-action px-3 py-2 ${selected ? 'active' : ''}`}
      onClick={(e) => {
        e.preventDefault();
        onSelect();
      }}
    >
      <div className="d-flex justify-content-between align-items-start">
        <span className="fw-semibold text-break me-2">{game.name}</span>
        <span className={`badge ${visClass} text-nowrap`}>{game.visibility}</span>
      </div>
      <div className="d-flex justify-content-between align-items-center mt-1">
        <span className="badge bg-secondary">{game.format}</span>
        <small className={relClass}>{relLabel}</small>
      </div>
      {(game.visibility === 'PUBLIC' || totalCount > 0) && (
        <div className="d-flex justify-content-between align-items-center mt-1">
          <span className="small text-muted">
            {totalCount > 0 && (
              <>
                {registeredCount}/{totalCount} <i className="bi bi-person" />
              </>
            )}
          </span>
          {game.visibility === 'PUBLIC' && game.created && (
            <small className="text-muted">
              closes {relativeTime(new Date(new Date(game.created).getTime() + 5 * 24 * 60 * 60 * 1000).toISOString())}
            </small>
          )}
        </div>
      )}
    </a>
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
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">Games</span>
        <button className="btn btn-sm btn-outline-secondary" onClick={onNew}>
          New <i className="bi-plus-circle" />
        </button>
      </div>
      <div className="flex-fill min-h-0" style={{ overflowY: 'auto', overflowX: 'clip' }}>
        <div className="list-group list-group-flush">
          {games.map((g) => (
            <GameListItem key={g.name} game={g} selected={g.name === selectedName} onSelect={() => onSelect(g)} />
          ))}
        </div>
      </div>
    </div>
  );
}
