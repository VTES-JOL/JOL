import { useQuery } from '@tanstack/react-query';
import { api } from '../../api/client';
import type { DeckInfoBean, TournamentMetadata, TournamentRegistered } from '../../api/types';
import { relativeTime } from '../../utils/relativeTime';
import { DeckPreview } from '../../components/DeckPreview';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';

export function OpenTournamentDetail({
  tournament,
  onJoinedOrLeft,
  onDeckChanged,
}: {
  tournament: TournamentMetadata;
  onJoinedOrLeft: () => void;
  onDeckChanged: () => void;
}) {
  const { data: registered } = useQuery({
    queryKey: ['tournament', 'registered'],
    queryFn: () => api.get<TournamentRegistered>('/tournament/registered'),
  });
  const veknLinked = registered?.veknLinked ?? false;
  const registeredGames = registered?.registeredGames ?? [];
  // registrable=true excludes LEGACY-format decks, matching the old
  // server-side TournamentBean.decks filter — legacy decks can't be
  // registered to a tournament.
  const { data: decks = [] } = useQuery({
    queryKey: ['decks', 'registrable'],
    queryFn: () => api.get<DeckInfoBean[]>('/decks?registrable=true'),
  });

  const join = () => {
    runRequest(api.post(`/tournament/${encodeURIComponent(tournament.name)}/player/join`), 'Failed to join tournament', onJoinedOrLeft);
  };

  const leave = async () => {
    if (
      !(await confirmDialog('Your registration and any submitted deck are withdrawn.', {
        title: 'Leave this tournament?',
        confirmLabel: 'Leave',
      }))
    )
      return;
    runRequest(api.post(`/tournament/${encodeURIComponent(tournament.name)}/player/leave`), 'Failed to leave tournament', onJoinedOrLeft);
  };

  const chooseDeck = (deckName: string) => {
    if (!deckName) return;
    runRequest(
      api.post(`/tournament/${encodeURIComponent(tournament.name)}/player/deck`, { deckName }),
      'Failed to register tournament deck',
      onDeckChanged,
    );
  };

  const registration = registeredGames.find((g) => g.name === tournament.name);
  const format = registration ? registration.format : tournament.deckFormat;
  const eligibleDecks = decks.filter((d) => d.gameFormats.includes(format));

  const action = veknLinked ? (
    tournament.registered ? (
      <Button variant="secondary" size="sm" onClick={leave}>
        Leave
      </Button>
    ) : (
      <Button variant="secondary" size="sm" onClick={join}>
        Join
      </Button>
    )
  ) : (
    <Badge variant="muted">Requires VEKN #</Badge>
  );

  return (
    <Panel title={tournament.name} right={action}>
      <div className="flex-1 min-h-0 overflow-y-auto p-4 text-sm text-ink">
        <p className="text-xs text-ink-muted mb-3">
          Registration closes {relativeTime(tournament.registrationEndTime)}
        </p>

        {tournament.rules.length > 0 && (
          <div className="mb-3">
            <p className="font-semibold mb-1">Rules</p>
            <ul className="list-disc pl-5 space-y-0.5 text-ink-secondary">
              {tournament.rules.map((r, i) => (
                <li key={i}>{r}</li>
              ))}
            </ul>
          </div>
        )}

        {tournament.conditions && (
          <div className="mb-3">
            <p className="font-semibold mb-1">Special Rules</p>
            <p className="text-ink-secondary">{tournament.conditions}</p>
            <ul className="list-disc pl-5 space-y-0.5 text-ink-secondary">
              {(tournament.specialRules ?? []).map((r, i) => (
                <li key={i}>{r}</li>
              ))}
            </ul>
          </div>
        )}

        {tournament.registered && (
          <div>
            <p className="font-semibold mb-2">Deck Selection</p>
            <div className="flex items-end gap-2 mb-3">
              <Select
                id="tournamentDeck"
                size="sm"
                label={registration?.deck ? `Current deck: ${registration.deck.name}` : 'No deck selected'}
                value={registration?.deck?.name ?? ''}
                onChange={(e) => chooseDeck(e.target.value)}
              >
                <option value="">Choose deck…</option>
                {eligibleDecks.map((d) => (
                  <option key={d.name} value={d.name}>
                    {d.name}
                  </option>
                ))}
              </Select>
            </div>
            {registration?.deck && <DeckPreview deck={registration.deck} details={registration.details} />}
          </div>
        )}
      </div>
    </Panel>
  );
}
