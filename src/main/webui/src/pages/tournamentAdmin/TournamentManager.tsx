import type { TournamentMetadata } from '../../api/types';
import { Panel } from '../../components/ui/Panel';
import { Button } from '../../components/ui/Button';
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
      <DraftTableManager tournamentName={tournament.name} roundsConfig={tournament.roundsConfig} onCreatedTables={onChanged} />
    );
  } else if (tournament.status === 'ACTIVE' && now <= playEnd) {
    body = <RoundSummary tournamentName={tournament.name} />;
  } else if (tournament.status === 'ACTIVE') {
    body = <FinalsManager tournamentName={tournament.name} />;
  } else {
    body = null;
  }

  return (
    <Panel
      title={`Tournament Tables — ${tournament.name}`}
      right={
        <Button variant="ghost" size="sm" onClick={onClose}>
          Close
        </Button>
      }
    >
      <div className="flex flex-col flex-1 min-h-0 p-2">{body}</div>
    </Panel>
  );
}
