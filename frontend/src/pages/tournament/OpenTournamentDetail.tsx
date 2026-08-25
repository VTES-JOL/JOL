import { Card, CardHeader } from '../../components/Card';
import { api } from '../../api/client';
import type { DeckInfoBean, TournamentBean, TournamentInviteStatus, TournamentMetadata } from '../../api/types';
import { relativeTime } from '../../lib/relativeTime';
import { DeckPreview } from '../../components/DeckPreview';
import { useSimpleDropdown } from '../../hooks/useSimpleDropdown';
import { confirmDialog } from '../../components/dialog';
import { runRequest } from '../../api/mutate';

export function OpenTournamentDetail({
  tournament,
  veknLinked,
  registeredGames,
  decks,
  onChanged,
}: {
  tournament: TournamentMetadata;
  veknLinked: boolean;
  registeredGames: TournamentInviteStatus[];
  decks: DeckInfoBean[];
  onChanged: (updated: TournamentBean) => void;
}) {
  const join = () => {
    runRequest(api.post<TournamentBean>(`/tournament/${encodeURIComponent(tournament.name)}/player/join`), 'Failed to join tournament', onChanged);
  };

  const leave = async () => {
    if (!(await confirmDialog('Leave Tournament?'))) return;
    runRequest(api.post<TournamentBean>(`/tournament/${encodeURIComponent(tournament.name)}/player/leave`), 'Failed to leave tournament', onChanged);
  };

  const chooseDeck = (deckName: string) => {
    runRequest(
      api.post<TournamentBean>(`/tournament/${encodeURIComponent(tournament.name)}/player/deck`, { deckName }),
      'Failed to register tournament deck',
      onChanged,
    );
  };

  const registration = registeredGames.find((g) => g.name === tournament.name);
  const format = registration ? registration.format : tournament.deckFormat;
  const deckDropdown = useSimpleDropdown<HTMLDivElement>();

  return (
    <Card className="flex-fill d-flex flex-column">
      <CardHeader>
        <span className="d-flex justify-content-between align-items-center w-100">
          <span className="fw-semibold">{tournament.name}</span>
          {veknLinked ? (
            tournament.registered ? (
              <button className="btn btn-outline-secondary btn-sm" onClick={leave}>
                Leave
              </button>
            ) : (
              <button className="btn btn-outline-secondary btn-sm" onClick={join}>
                Join
              </button>
            )
          ) : (
            <span className="badge bg-warning-subtle text-black">Requires VEKN #</span>
          )}
        </span>
      </CardHeader>
      <div className="card-body flex-fill overflow-auto min-h-0">
        <div className="mb-3">
          <p className="text-muted small mb-1">Registration closes {relativeTime(tournament.registrationEndTime)}</p>
          {tournament.rules.length > 0 && (
            <>
              <strong>Rules</strong>
              <ul>
                {tournament.rules.map((r, i) => (
                  <li key={i}>{r}</li>
                ))}
              </ul>
            </>
          )}
          {tournament.conditions && (
            <>
              <strong>Special Rules: </strong>
              <span>{tournament.conditions}</span>
              <ul>
                {(tournament.specialRules ?? []).map((r, i) => (
                  <li key={i}>{r}</li>
                ))}
              </ul>
            </>
          )}
        </div>

        {tournament.registered && (
          <div>
            <h6 className="fw-semibold mb-2">Deck Selection</h6>
            <div className="d-flex align-items-center gap-2 mb-3">
              <span className="text-muted small">
                {registration?.deck ? `Current deck: ${registration.deck.name}` : 'No deck selected'}
              </span>
              <div className={`dropdown ${deckDropdown.open ? 'show' : ''}`} ref={deckDropdown.rootRef}>
                <button
                  className="btn btn-outline-secondary btn-sm dropdown-toggle"
                  type="button"
                  onClick={() => deckDropdown.setOpen((prev) => !prev)}
                >
                  Choose Deck
                </button>
                <ul
                  className={`dropdown-menu dropdown-menu-end ${deckDropdown.open ? 'show' : ''}`}
                  style={{ right: 0, left: 'auto' }}
                >
                  {decks
                    .filter((d) => d.gameFormats.includes(format))
                    .map((d) => (
                      <li key={d.name}>
                        <a
                          className="dropdown-item"
                          role="button"
                          onClick={() => {
                            chooseDeck(d.name);
                            deckDropdown.setOpen(false);
                          }}
                        >
                          {d.name}
                        </a>
                      </li>
                    ))}
                </ul>
              </div>
            </div>
            {registration?.deck && <DeckPreview deck={registration.deck} />}
          </div>
        )}
      </div>
    </Card>
  );
}
