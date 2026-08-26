import type { TournamentMetadata } from '../../api/types';

function StatusBadge({ status }: { status: string }) {
  if (status === 'ACTIVE') return <span className="badge text-bg-success">Active</span>;
  if (status === 'STARTING') return <span className="badge text-bg-secondary">Starting</span>;
  return <span className="badge text-bg-warning">Draft</span>;
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
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">Tournaments</span>
        <button className="btn btn-sm btn-outline-secondary" onClick={onNew}>
          New <i className="bi-plus" />
        </button>
      </div>
      <div className="flex-fill overflow-auto min-h-0">
        <ul className="list-group list-group-flush">
          {tournaments.map((t) => (
            <li
              key={t.name}
              className="list-group-item d-flex justify-content-between align-items-center"
              style={{ cursor: 'pointer' }}
              onClick={() => onSelect(t)}
            >
              <span>{t.name}</span>
              <StatusBadge status={t.status} />
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
