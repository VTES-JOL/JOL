import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CheckCircle2, Clock, FileText, LogIn, LogOut, PlayCircle, UserPlus, Users, X, XCircle } from 'lucide-react';
import { api, ApiError } from '../../api/client';
import type { DeckInfoBean, EnrichedDeck, GameStatusBean, RegistrationStatus } from '../../api/types';
import { useAuth } from '../../auth/useAuth';
import { DeckView } from '../../components/DeckView';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { showError } from '../../stores/toast';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { EmptyState } from '../../components/ui/EmptyState';
import { InlineAlert } from '../../components/ui/FormFeedback';
import { SectionLabel } from '../../components/ui/SectionLabel';

const myRegistrationFor = (game: GameStatusBean, player: string | null): RegistrationStatus | undefined =>
  game.registrations.find((r) => r.player === player);

/** name + format/visibility badges — shared header for both lobby detail panes. */
function GameTitle({ game }: { game: GameStatusBean }) {
  return (
    <span className="flex items-center gap-2">
      {game.name}
      <Badge variant="format">{game.format}</Badge>
      <Badge variant={game.visibility === 'PUBLIC' ? 'online' : 'muted'}>
        {game.visibility === 'PUBLIC' ? 'Public' : 'Private'}
      </Badge>
    </span>
  );
}

/**
 * Left detail pane: the game's people and settings — seat list, invites, and
 * the deck-registration control. The registered deck itself is previewed in
 * the separate {@link GameDeckPanel} (its own column on wide screens / its own
 * tab on mobile), so this pane stays narrow.
 */
export function GameSettings({
  game,
  onClose,
  onChanged,
  onDeckRegistered,
}: {
  game: GameStatusBean;
  onClose: () => void;
  onChanged: () => void;
  /** Called after a successful deck registration so the mobile view can jump
   *  to the deck-preview tab (no-op on desktop, where it's already visible). */
  onDeckRegistered: () => void;
}) {
  const { player } = useAuth();
  const [inviteInput, setInviteInput] = useState('');
  // Kept in local state rather than shown as a toast: the start request can
  // fail for a fixable reason ("no one's registered a deck yet") the owner
  // needs to keep reading while they act on it.
  const [startError, setStartError] = useState<string | null>(null);

  const { data: players = [] } = useQuery({
    queryKey: ['lobby', 'players'],
    queryFn: () => api.get<string[]>('/lobby/players'),
  });
  // registrable=true excludes LEGACY-format decks, matching the old
  // server-side LobbyPageBean.decks filter — legacy decks can't be
  // registered to a new game (see JolAdmin.registerDeck).
  const { data: decks = [] } = useQuery({
    queryKey: ['decks', 'registrable'],
    queryFn: () => api.get<DeckInfoBean[]>('/decks?registrable=true'),
  });

  const encodedName = encodeURIComponent(game.name);

  const startGame = async () => {
    if (
      !(await confirmDialog('Once the game starts, players can’t be added or swapped except by an admin.', {
        title: 'Start this game?',
        confirmLabel: 'Start game',
      }))
    )
      return;
    setStartError(null);
    try {
      await api.post(`/lobby/player/games/${encodedName}/start`);
      onChanged();
    } catch (err) {
      setStartError(err instanceof ApiError && err.message ? err.message : 'Failed to start game.');
    }
  };

  const closeGame = async () => {
    if (
      !(await confirmDialog('This ends the game for everyone and can’t be undone.', {
        title: 'Close this game?',
        confirmLabel: 'Close game',
        danger: true,
      }))
    )
      return;
    runRequest(api.del(`/lobby/player/games/${encodedName}`), 'Failed to close game', onChanged);
  };

  const joinGame = () => {
    if (!player) return;
    runRequest(api.post(`/lobby/player/games/${encodedName}/invite`, { player }), 'Failed to join game', onChanged);
  };

  const leaveGame = async () => {
    if (
      !player ||
      !(await confirmDialog('Your seat and deck registration are released.', {
        title: 'Leave this game?',
        confirmLabel: 'Leave',
      }))
    )
      return;
    runRequest(
      api.del(`/lobby/player/games/${encodedName}/invite/${encodeURIComponent(player)}`),
      'Failed to leave game',
      onChanged,
    );
  };

  const invitePlayer = () => {
    const p = inviteInput.trim();
    if (!p) return;
    setStartError(null);
    if (!players.includes(p)) {
      showError(`No such player: ${p}`);
      return;
    }
    runRequest(api.post(`/lobby/player/games/${encodedName}/invite`, { player: p }), 'Failed to invite player', onChanged);
    setInviteInput('');
  };

  const removeInvite = (p: string) => {
    runRequest(
      api.del(`/lobby/player/games/${encodedName}/invite/${encodeURIComponent(p)}`),
      'Failed to remove invite',
      onChanged,
    );
  };

  const [visibleMessage, setVisibleMessage] = useState<string | null>(null);
  useEffect(() => {
    if (!visibleMessage) return;
    const timeout = setTimeout(() => setVisibleMessage(null), 4000);
    return () => clearTimeout(timeout);
  }, [visibleMessage]);

  const registerDeck = (deckName: string) => {
    if (!deckName) return;
    setStartError(null); // a just-registered deck may have been what was missing
    runRequest(
      api.post<{ message: string | null }>(`/lobby/player/games/${encodedName}/deck`, { deckName }),
      'Failed to register deck',
      (result) => {
        onChanged();
        setVisibleMessage(result.message);
        onDeckRegistered();
      },
    );
  };

  const myRegistration = myRegistrationFor(game, player);
  const playerInRegistrations = myRegistration !== undefined;
  const eligibleDecks = decks.filter((d) => d.gameFormats.includes(game.format));

  const actions = (
    <span className="flex gap-1">
      {game.playerRelationship === 'OWNER' && game.gameStatus === 'Inviting' && (
        <Button variant="secondary" size="sm" icon={<PlayCircle size={14} />} onClick={startGame}>
          Start
        </Button>
      )}
      {game.playerRelationship === 'OWNER' && (
        <Button variant="secondary" size="sm" icon={<XCircle size={14} />} onClick={closeGame}>
          Close
        </Button>
      )}
      {game.playerRelationship === 'OPEN' && (
        <Button variant="secondary" size="sm" icon={<LogIn size={14} />} onClick={joinGame}>
          Join
        </Button>
      )}
      {(game.playerRelationship === 'REGISTERED' || game.playerRelationship === 'INVITED') && (
        <Button variant="secondary" size="sm" icon={<LogOut size={14} />} onClick={leaveGame}>
          Leave
        </Button>
      )}
      <Button variant="ghost" size="sm" aria-label="Close panel" onClick={onClose}>
        <X size={14} />
      </Button>
    </span>
  );

  return (
    <Panel title={<GameTitle game={game} />} right={actions}>
      <div className="flex-1 min-h-0 overflow-y-auto text-sm text-ink">
        <div className="max-w-2xl">
          {startError && (
            <div className="p-4 pb-0">
              <InlineAlert kind="danger">{startError}</InlineAlert>
            </div>
          )}
          <div className="p-4 border-b border-line">
            <SectionLabel>Players</SectionLabel>
            {game.registrations.length === 0 ? (
              <EmptyState
                icon={Users}
                title="No one’s registered yet"
                description={
                  game.playerRelationship === 'OWNER'
                    ? 'Invite players below, then register your own deck.'
                    : 'Waiting for players to join.'
                }
              />
            ) : (
              <table className="w-full">
                <tbody>
                  {game.registrations.map((reg) => (
                    <tr key={reg.player} className="border-b border-line/50 last:border-b-0">
                      <td className="py-1">{reg.player}</td>
                      <td className="py-1 text-center">
                        {reg.registered ? (
                          <CheckCircle2 size={14} className="inline text-online" />
                        ) : (
                          <Clock size={14} className="inline text-ink-muted" />
                        )}
                      </td>
                      <td className="py-1 text-right">
                        {game.playerRelationship === 'OWNER' && (
                          <Button
                            variant="danger"
                            size="sm"
                            aria-label={`Remove ${reg.player}`}
                            onClick={() => removeInvite(reg.player)}
                          >
                            <X size={12} />
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {game.playerRelationship === 'OWNER' && (
            <div className="p-4 border-b border-line">
              <SectionLabel>Invite Player</SectionLabel>
              <div className="flex gap-2">
                <Input
                  srLabel="Player name"
                  size="sm"
                  list="lobby-detail-players"
                  placeholder="Start typing a player name"
                  value={inviteInput}
                  onChange={(e) => setInviteInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') invitePlayer();
                  }}
                />
                <datalist id="lobby-detail-players">
                  {players.map((p) => (
                    <option key={p} value={p} />
                  ))}
                </datalist>
                <Button variant="secondary" size="sm" icon={<UserPlus size={14} />} onClick={invitePlayer}>
                  Invite
                </Button>
              </div>
            </div>
          )}

          {playerInRegistrations && (
            <div className="p-4">
              <SectionLabel>Register Deck</SectionLabel>
              <Select
                srLabel="Deck"
                size="sm"
                value={myRegistration?.deckName ?? ''}
                onChange={(e) => registerDeck(e.target.value)}
              >
                <option value="">Choose deck…</option>
                {eligibleDecks.map((d) => (
                  <option key={d.name} value={d.name}>
                    {d.name}
                  </option>
                ))}
              </Select>
              {visibleMessage && <p className="mt-2 text-sm text-online">{visibleMessage}</p>}
            </div>
          )}
        </div>
      </div>
    </Panel>
  );
}

/**
 * Right detail pane: a read-only preview of the viewer's own registered deck
 * for the selected game. Its own column on wide screens (like the deck
 * editor's analytics pane), its own tab below the `lg` breakpoint.
 */
export function GameDeckPanel({ game }: { game: GameStatusBean | null }) {
  const { player } = useAuth();
  const registration = game ? myRegistrationFor(game, player) : undefined;

  const { data: preview } = useQuery({
    queryKey: ['lobby', game?.name, 'deck', registration?.deckName],
    queryFn: () => api.get<EnrichedDeck>(`/lobby/player/games/${encodeURIComponent(game!.name)}/deck`),
    enabled: !!game && !!registration?.deckName,
    // A failed preview fetch just means no preview shown, not worth a toast.
    meta: { silent: true },
  });

  return (
    <Panel title="My Deck">
      <div className="flex-1 min-h-0 overflow-y-auto text-sm text-ink">
        {!game ? (
          <div className="p-4">
            <EmptyState
              icon={FileText}
              title="No game selected"
              description="Your registered deck for the selected game shows here."
            />
          </div>
        ) : preview?.deck ? (
          <DeckView deck={preview.deck} details={preview.details} />
        ) : registration?.deckName ? (
          <div className="p-4">
            <EmptyState icon={FileText} title="Loading deck…" />
          </div>
        ) : (
          <div className="p-4">
            <EmptyState
              icon={FileText}
              title="No deck registered"
              description={
                registration
                  ? 'Pick a deck under Details to preview it here.'
                  : 'Join the game and register a deck to preview it here.'
              }
            />
          </div>
        )}
      </div>
    </Panel>
  );
}
