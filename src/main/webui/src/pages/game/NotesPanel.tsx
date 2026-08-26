import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { runRequest } from '../../api/mutate';
import { Panel } from './Panel';

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
    runRequest(api.put(`/game/${gameId}/notes/global`, { notes: globalNotes }), 'Failed to save notes');
  };

  const savePrivate = () => {
    runRequest(api.put(`/game/${gameId}/notes/private`, { notes: privateNotes }), 'Failed to save private notes');
  };

  return (
    <Panel
      id="notesCard"
      className="notes"
      bodyClassName="p-0"
      title="Notes"
      toggle={game.player ? { icon: 'bi-info-lg', label: 'Deck', onClick: onToggleDeck } : undefined}
    >
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
    </Panel>
  );
}
