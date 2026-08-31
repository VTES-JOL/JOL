import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CheckCircle2, Clock, LogIn, LogOut, PlayCircle, UserPlus, X, XCircle } from 'lucide-react';
import { api } from '../../api/client';
import type { Deck, DeckInfoBean, GameStatusBean } from '../../api/types';
import { useAuth } from '../../auth/useAuth';
import { DeckPreview } from '../../components/DeckPreview';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { showError } from '../../stores/toast';
import { Panel } from '../../components/ui/Panel';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { SectionLabel } from '../../components/ui/SectionLabel';

export function GameDetail({
  game,
  onClose,
  onChanged,
}: {
  game: GameStatusBean;
  onClose: () => void;
  onChanged: () => void;
}) {
  const { player } = useAuth();
  const [inviteInput, setInviteInput] = useState('');

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
    if (!(await confirmDialog('Start Game?'))) return;
    runRequest(api.post(`/lobby/player/games/${encodedName}/start`), 'Failed to start game', onChanged);
  };

  const closeGame = async () => {
    if (!(await confirmDialog('Close Game?', { danger: true }))) return;
    runRequest(api.del(`/lobby/player/games/${encodedName}`), 'Failed to close game', onChanged);
  };

  const joinGame = () => {
    if (!player) return;
    runRequest(api.post(`/lobby/player/games/${encodedName}/invite`, { player }), 'Failed to join game', onChanged);
  };

  const leaveGame = async () => {
    if (!player || !(await confirmDialog('Leave Game?'))) return;
    runRequest(
      api.del(`/lobby/player/games/${encodedName}/invite/${encodeURIComponent(player)}`),
      'Failed to leave game',
      onChanged,
    );
  };

  const invitePlayer = () => {
    const p = inviteInput.trim();
    if (!p) return;
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
    runRequest(
      api.post<{ message: string | null }>(`/lobby/player/games/${encodedName}/deck`, { deckName }),
      'Failed to register deck',
      (result) => {
        onChanged();
        setVisibleMessage(result.message);
      },
    );
  };

  const myRegistration = game.registrations.find((r) => r.player === player);
  const playerInRegistrations = myRegistration !== undefined;

  const { data: preview } = useQuery({
    queryKey: ['lobby', game.name, 'deck', myRegistration?.deckName],
    queryFn: () => api.get<Deck>(`/lobby/player/games/${encodedName}/deck`),
    enabled: !!myRegistration?.deckName,
    // Matches the original silent .catch(() => setPreview(null)) — a failed
    // preview fetch just means no preview shown, not worth a toast.
    meta: { silent: true },
  });

  const eligibleDecks = decks.filter((d) => d.gameFormats.includes(game.format));

  const actions = (
    <span className="jt:flex jt:gap-1">
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
    <Panel
      title={
        <span className="jt:flex jt:items-center jt:gap-2">
          {game.name}
          <Badge variant="format">{game.format}</Badge>
          <Badge variant={game.visibility === 'PUBLIC' ? 'online' : 'muted'}>
            {game.visibility === 'PUBLIC' ? 'Public' : 'Private'}
          </Badge>
        </span>
      }
      right={actions}
    >
      <div className="jt:flex-1 jt:min-h-0 jt:overflow-y-auto jt:text-sm jt:text-ink">
        <div className="jt:p-4 jt:border-b jt:border-line">
          <SectionLabel>Players</SectionLabel>
          <table className="jt:w-full">
            <tbody>
              {game.registrations.map((reg) => (
                <tr key={reg.player} className="jt:border-b jt:border-line/50 jt:last:border-b-0">
                  <td className="jt:py-1">{reg.player}</td>
                  <td className="jt:py-1 jt:text-center">
                    {reg.registered ? (
                      <CheckCircle2 size={14} className="jt:inline jt:text-online" />
                    ) : (
                      <Clock size={14} className="jt:inline jt:text-ink-muted" />
                    )}
                  </td>
                  <td className="jt:py-1 jt:text-right">
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
        </div>

        {game.playerRelationship === 'OWNER' && (
          <div className="jt:p-4 jt:border-b jt:border-line">
            <SectionLabel>Invite Player</SectionLabel>
            <div className="jt:flex jt:gap-2">
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
          <div className="jt:p-4 jt:border-b jt:border-line">
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
          </div>
        )}

        {preview && (
          <div className="jt:p-4">
            <SectionLabel>Registered Deck</SectionLabel>
            <DeckPreview deck={preview} />
          </div>
        )}

        {visibleMessage && <div className="jt:p-4 jt:text-sm jt:text-online">{visibleMessage}</div>}
      </div>
    </Panel>
  );
}
