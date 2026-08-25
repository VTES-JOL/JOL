import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../api/client';
import type { Deck, DeckInfoBean, GameStatusBean } from '../../api/types';
import { useAuth } from '../../nav/useAuth';
import { useSimpleDropdown } from '../../hooks/useSimpleDropdown';
import { DeckPreview } from '../../components/DeckPreview';
import { confirmDialog } from '../../components/dialog';
import { runRequest } from '../../api/mutate';

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
  const [deckSearch, setDeckSearch] = useState('');
  const deckDropdown = useSimpleDropdown<HTMLDivElement>();

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
    runRequest(
      api.post<{ message: string | null }>(`/lobby/player/games/${encodedName}/deck`, { deckName }),
      'Failed to register deck',
      (result) => {
        onChanged();
        setVisibleMessage(result.message);
      },
    );
    deckDropdown.setOpen(false);
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

  return (
    <div className="card shadow flex-fill d-flex flex-column min-h-0">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="d-flex align-items-center gap-2">
          <span className="fw-semibold">{game.name}</span>
          <span className="badge bg-secondary">{game.format}</span>
          <span className={`badge ${game.visibility === 'PUBLIC' ? 'bg-success' : 'bg-secondary'}`}>
            {game.visibility === 'PUBLIC' ? 'Public' : 'Private'}
          </span>
        </span>
        <span className="d-flex gap-1">
          {game.playerRelationship === 'OWNER' && game.gameStatus === 'Inviting' && (
            <button className="btn btn-sm btn-outline-secondary" onClick={startGame}>
              Start <i className="bi-play-circle" />
            </button>
          )}
          {game.playerRelationship === 'OWNER' && (
            <button className="btn btn-sm btn-outline-secondary" onClick={closeGame}>
              Close <i className="bi-x-circle" />
            </button>
          )}
          {game.playerRelationship === 'OPEN' && (
            <button className="btn btn-sm btn-outline-secondary" onClick={joinGame}>
              Join <i className="bi-box-arrow-in-right" />
            </button>
          )}
          {(game.playerRelationship === 'REGISTERED' || game.playerRelationship === 'INVITED') && (
            <button className="btn btn-sm btn-outline-secondary" onClick={leaveGame}>
              Leave <i className="bi-box-arrow-left" />
            </button>
          )}
          <button className="btn btn-sm btn-outline-secondary" onClick={onClose}>
            <i className="bi-x" />
          </button>
        </span>
      </div>
      <div className="card-body p-0 d-flex flex-column overflow-auto min-h-0">
        <div className="p-3 border-bottom">
          <div className="fw-semibold small text-muted mb-2">Players</div>
          <table className="table table-sm table-hover mb-0">
            <tbody>
              {game.registrations.map((reg) => (
                <tr key={reg.player}>
                  <td>{reg.player}</td>
                  <td className="text-center">
                    {reg.registered ? (
                      <i className="bi bi-check-circle text-success" />
                    ) : (
                      <i className="bi bi-hourglass text-muted" />
                    )}
                  </td>
                  <td className="text-end">
                    {game.playerRelationship === 'OWNER' && (
                      <button
                        className="btn btn-sm btn-outline-danger py-0 px-1"
                        onClick={() => removeInvite(reg.player)}
                      >
                        <i className="bi bi-x" />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {game.playerRelationship === 'OWNER' && (
          <div className="p-3 border-bottom">
            <div className="fw-semibold small text-muted mb-2">Invite Player</div>
            <div className="d-flex gap-2">
              <input
                className="form-control form-control-sm"
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
              <button className="btn btn-sm btn-outline-secondary text-nowrap" onClick={invitePlayer}>
                Invite <i className="bi-person-plus" />
              </button>
            </div>
          </div>
        )}

        {playerInRegistrations && (
          <div className="p-3 border-bottom">
            <div className="fw-semibold small text-muted mb-2">Register Deck</div>
            <div className="d-flex align-items-center gap-2">
              <div className={`dropdown ${deckDropdown.open ? 'show' : ''}`} ref={deckDropdown.rootRef}>
                <button
                  className="btn btn-sm btn-outline-secondary dropdown-toggle"
                  type="button"
                  onClick={() => deckDropdown.setOpen((prev) => !prev)}
                >
                  Choose Deck
                </button>
                <ul className={`dropdown-menu ${deckDropdown.open ? 'show' : ''}`}>
                  <li>
                    <input
                      className="form-control form-control-sm mx-2"
                      style={{ width: 'calc(100% - 1rem)' }}
                      type="text"
                      placeholder="Search..."
                      value={deckSearch}
                      onChange={(e) => setDeckSearch(e.target.value)}
                    />
                  </li>
                  {decks
                    .filter((d) => d.gameFormats.includes(game.format))
                    .filter((d) => d.name.toUpperCase().includes(deckSearch.toUpperCase()))
                    .map((d) => (
                      <li key={d.name}>
                        <a className="dropdown-item" role="button" onClick={() => registerDeck(d.name)}>
                          {d.name}
                        </a>
                      </li>
                    ))}
                </ul>
              </div>
              <span className="text-muted small">{myRegistration?.deckName ?? ''}</span>
            </div>
          </div>
        )}

        {preview && (
          <div className="p-3 flex-fill overflow-auto min-h-0">
            <div className="fw-semibold small text-muted mb-2">Registered Deck</div>
            <DeckPreview deck={preview} />
          </div>
        )}

        {visibleMessage && <div className="p-3 small text-success">{visibleMessage}</div>}
      </div>
    </div>
  );
}
