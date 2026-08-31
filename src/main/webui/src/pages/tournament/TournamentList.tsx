import type { TournamentMetadata } from '../../api/types';
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

  return (
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary">
        <span className="fw-semibold">Tournaments</span>
      </div>
      <ul className="list-group list-group-flush flex-fill overflow-auto min-h-0">
        {tournaments.map((t) => (
          <li
            key={t.name}
            className={`list-group-item list-group-item-action d-flex justify-content-between align-items-center ${
              selection?.type === 'open' && selection.name === t.name ? 'active' : ''
            }`}
            style={{ cursor: 'pointer' }}
            onClick={() => onSelect({ type: 'open', name: t.name })}
          >
            <span className="d-flex align-items-center">
              <span className="badge bg-secondary me-2">{t.deckFormat}</span>
              <span>{t.name}</span>
              {t.registered ? (
                <span className="badge text-bg-success ms-2">Registered</span>
              ) : (
                <span className="badge text-bg-secondary ms-2">Open</span>
              )}
            </span>
            <small className="text-muted">Closes {relativeTime(t.registrationEndTime)}</small>
          </li>
        ))}
        {finalsInvites.map((t) => (
          <li
            key={t.name}
            className={`list-group-item list-group-item-action d-flex justify-content-between align-items-center ${
              selection?.type === 'finals' && selection.name === t.name ? 'active' : ''
            }`}
            style={{ cursor: 'pointer' }}
            onClick={() => onSelect({ type: 'finals', name: t.name })}
          >
            <span className="d-flex align-items-center">
              <span className="badge text-bg-danger me-2">Finals</span>
              <span>{t.name}</span>
            </span>
          </li>
        ))}
        {isEmpty && <li className="list-group-item text-muted small">No tournaments available.</li>}
      </ul>
    </div>
  );
}
