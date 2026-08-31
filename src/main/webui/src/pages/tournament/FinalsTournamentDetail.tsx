import type { TournamentMetadata } from '../../api/types';
import { Panel } from '../../components/ui/Panel';

export function FinalsTournamentDetail({ tournament }: { tournament: TournamentMetadata }) {
  return (
    <Panel title={`${tournament.name} — Finals`}>
      <div className="flex-1 min-h-0 overflow-y-auto p-4">
        <p className="text-xs text-ink-muted mb-3">
          You have been selected for the final table. Seeding order below — seat selection will open in turn.
        </p>
        <ol className="list-decimal pl-5 space-y-1 text-sm text-ink">
          {(tournament.finalsSeeding ?? []).map((player) => (
            <li key={player}>{player}</li>
          ))}
        </ol>
      </div>
    </Panel>
  );
}
