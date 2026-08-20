import type { TournamentMetadata } from '../../api/types';
import { DraftTableManager } from './DraftTableManager';
import { RoundSummary } from './RoundSummary';
import { FinalsManager } from './FinalsManager';

export function TournamentManager({
  tournament,
  onClose,
  onChanged,
}: {
  tournament: TournamentMetadata;
  onClose: () => void;
  onChanged: () => void;
}) {
  const now = new Date();
  const regEnd = new Date(tournament.registrationEndTime);
  const playEnd = new Date(tournament.endTime);

  let body;
  if (tournament.status === 'STARTING' && now > regEnd) {
    body = (
      <DraftTableManager
        tournamentName={tournament.name}
        roundsConfig={tournament.roundsConfig}
        onCreatedTables={onChanged}
      />
    );
  } else if (tournament.status === 'ACTIVE' && now <= playEnd) {
    body = <RoundSummary tournamentName={tournament.name} />;
  } else if (tournament.status === 'ACTIVE') {
    body = <FinalsManager tournamentName={tournament.name} />;
  } else {
    body = null;
  }

  return (
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">Tournament Tables — {tournament.name}</span>
        <button className="btn btn-sm btn-outline-secondary" onClick={onClose}>
          Close
        </button>
      </div>
      <div className="card-body d-flex flex-column p-2 flex-fill min-h-0">{body}</div>
    </div>
  );
}
