import type { TournamentMetadata } from '../../api/types';
import { Panel } from '../../components/ui/Panel';

export function FinalsTournamentDetail({ tournament }: { tournament: TournamentMetadata }) {
  return (
    <Panel title={`${tournament.name} — Finals`}>
      <div className="jt:flex-1 jt:min-h-0 jt:overflow-y-auto jt:p-4">
        <p className="jt:text-xs jt:text-ink-muted jt:mb-3">
          You have been selected for the final table. Seeding order below — seat selection will open in turn.
        </p>
        <ol className="jt:list-decimal jt:pl-5 jt:space-y-1 jt:text-sm jt:text-ink">
          {(tournament.finalsSeeding ?? []).map((player) => (
            <li key={player}>{player}</li>
          ))}
        </ol>
      </div>
    </Panel>
  );
}
