import { Card, CardHeader } from '../../components/Card';
import type { TournamentMetadata } from '../../api/types';

export function FinalsTournamentDetail({ tournament }: { tournament: TournamentMetadata }) {
  return (
    <Card className="flex-fill d-flex flex-column">
      <CardHeader>
        <span className="fw-semibold">{tournament.name} — Finals</span>
      </CardHeader>
      <div className="card-body flex-fill overflow-auto min-h-0">
        <p className="text-muted small mb-2">
          You have been selected for the final table. Seeding order below — seat selection will open in turn.
        </p>
        <ol className="list-group list-group-numbered">
          {(tournament.finalsSeeding ?? []).map((player) => (
            <li key={player} className="list-group-item d-flex justify-content-between align-items-center">
              <span>{player}</span>
            </li>
          ))}
        </ol>
      </div>
    </Card>
  );
}
