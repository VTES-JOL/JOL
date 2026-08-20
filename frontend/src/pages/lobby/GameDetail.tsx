import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Deck, GameStatusBean, LobbyPage } from '../../api/types';
import { useAuth } from '../../nav/useAuth';
import { useSimpleDropdown } from '../../hooks/useSimpleDropdown';
import { DeckPreview } from '../../components/DeckPreview';

export function GameDetail({
  game,
  players,
  decks,
  message,
  onClose,
  onChanged,
}: {
  game: GameStatusBean;
  players: string[];
  decks: LobbyPage['decks'];
  message: string | null;
  onClose: () => void;
  onChanged: (updated: LobbyPage) => void;
}) {
  const { player } = useAuth();
  const [inviteInput, setInviteInput] = useState('');
  const [deckSearch, setDeckSearch] = useState('');
  const [preview, setPreview] = useState<Deck | null>(null);
  const deckDropdown = useSimpleDropdown<HTMLDivElement>();

  const encodedName = encodeURIComponent(game.name);

  const startGame = () => {
    if (!confirm('Start Game?')) return;
    api.post<LobbyPage>(`/lobby/player/games/${encodedName}/start`).then(onChanged).catch((err) => console.error('Failed to start game', err));
  };

  const closeGame = () => {
    if (!confirm('Close Game?')) return;
    api.del<LobbyPage>(`/lobby/player/games/${encodedName}`).then(onChanged).catch((err) => console.error('Failed to close game', err));
  };

  const joinGame = () => {
    if (!player) return;
    api
      .post<LobbyPage>(`/lobby/player/games/${encodedName}/invite`, { player })
      .then(onChanged)
      .catch((err) => console.error('Failed to join game', err));
  };

  const leaveGame = () => {
    if (!player || !confirm('Leave Game?')) return;
    api
      .del<LobbyPage>(`/lobby/player/games/${encodedName}/invite/${encodeURIComponent(player)}`)
      .then(onChanged)
      .catch((err) => console.error('Failed to leave game', err));
  };

  const invitePlayer = () => {
    const p = inviteInput.trim();
    if (!p) return;
    api
      .post<LobbyPage>(`/lobby/player/games/${encodedName}/invite`, { player: p })
      .then(onChanged)
      .catch((err) => console.error('Failed to invite player', err));
    setInviteInput('');
  };

  const removeInvite = (p: string) => {
    api
      .del<LobbyPage>(`/lobby/player/games/${encodedName}/invite/${encodeURIComponent(p)}`)
      .then(onChanged)
      .catch((err) => console.error('Failed to remove invite', err));
  };

  const registerDeck = (deckName: string) => {
    api
      .post<LobbyPage>(`/lobby/player/games/${encodedName}/deck`, { deckName })
      .then(onChanged)
      .catch((err) => console.error('Failed to register deck', err));
    deckDropdown.setOpen(false);
  };

  const myRegistration = game.registrations.find((r) => r.player === player);
  const playerInRegistrations = myRegistration !== undefined;

  const [visibleMessage, setVisibleMessage] = useState<string | null>(null);
  useEffect(() => {
    if (!message) return;
    setVisibleMessage(message);
    const timeout = setTimeout(() => setVisibleMessage(null), 4000);
    return () => clearTimeout(timeout);
  }, [message]);

  useEffect(() => {
    if (myRegistration?.deckName) {
      api
        .get<Deck>(`/lobby/player/games/${encodedName}/deck`)
        .then(setPreview)
        .catch(() => setPreview(null));
    } else {
      setPreview(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [game.name, myRegistration?.deckName]);

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
