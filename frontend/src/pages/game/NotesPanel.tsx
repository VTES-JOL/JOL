import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { showError } from '../../components/toast';

// Mirrors notes.jsp — saves on blur (not every keystroke), matching legacy's
// updateNotes()/updateNotesHand() triggers. PUT .../notes/global and
// .../notes/private are already dedicated, envelope-free endpoints (return
// void) — no new backend surface needed here.
export function NotesPanel({
  gameId,
  game,
  onToggleDeck,
}: {
  gameId: string;
  game: GameSnapshot;
  onToggleDeck: () => void;
}) {
  const [globalNotes, setGlobalNotes] = useState(game.globalNotes ?? '');
  const [privateNotes, setPrivateNotes] = useState(game.privateNotes ?? '');

  useEffect(() => setGlobalNotes(game.globalNotes ?? ''), [game.globalNotes]);
  useEffect(() => setPrivateNotes(game.privateNotes ?? ''), [game.privateNotes]);

  const saveGlobal = () => {
    api.put(`/game/${gameId}/notes/global`, { notes: globalNotes }).catch((err) => {
      console.error('Failed to save notes', err);
      showError('Failed to save notes.');
    });
  };

  const savePrivate = () => {
    api.put(`/game/${gameId}/notes/private`, { notes: privateNotes }).catch((err) => {
      console.error('Failed to save private notes', err);
      showError('Failed to save private notes.');
    });
  };

  return (
    <div className="card shadow notes" id="notesCard">
      <div className="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>Notes</span>
        {game.player && (
          <button className="border-0 shadow rounded-pill bg-light" onClick={onToggleDeck}>
            <i className="bi bi-info-lg me-2" />
            Deck
          </button>
        )}
      </div>
      <div className="card-body p-0">
        <textarea
          id="globalNotes"
          className="form-control scrollable"
          placeholder="Global Notes"
          disabled={!(game.player || game.judge)}
          value={globalNotes}
          onChange={(e) => setGlobalNotes(e.target.value)}
          onBlur={saveGlobal}
        />
        {game.player && (
          <textarea
            id="privateNotes"
            className="form-control scrollable"
            placeholder="Private Notes"
            value={privateNotes}
            onChange={(e) => setPrivateNotes(e.target.value)}
            onBlur={savePrivate}
          />
        )}
      </div>
    </div>
  );
}
