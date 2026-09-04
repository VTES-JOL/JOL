import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { runRequest } from '../../api/mutate';

const NOTES_LABEL = 'block px-2 pt-2 pb-0.5 text-xs font-semibold uppercase tracking-wide text-ink-muted';
const NOTES_TEXTAREA =
  'w-full flex-1 min-h-0 bg-surface/70 text-sm text-ink placeholder:text-ink-muted p-2 outline-none resize-none scrollable disabled:opacity-60';

// Mirrors notes.jsp — saves on blur (not every keystroke), matching legacy's
// updateNotes()/updateNotesHand() triggers. PUT .../notes/global and
// .../notes/private are already dedicated, envelope-free endpoints (return
// void). Rendered inside NotesDeckDrawer (the right-edge drawer), which owns
// the frame and the Notes/Deck tab switch — this component is just the two
// textareas now.
export function NotesPanel({ gameId, game }: { gameId: string; game: GameSnapshot }) {
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
    <div className="flex flex-1 min-h-0 flex-col">
      <label htmlFor="globalNotes" className={NOTES_LABEL}>
        Global — everyone at the table
      </label>
      <textarea
        id="globalNotes"
        className={NOTES_TEXTAREA}
        placeholder="Shared bookkeeping — events in play, table agreements…"
        disabled={!(game.player || game.judge)}
        value={globalNotes}
        onChange={(e) => setGlobalNotes(e.target.value)}
        onBlur={saveGlobal}
      />
      {game.player && (
        <>
          <label htmlFor="privateNotes" className={`${NOTES_LABEL} border-t border-line`}>
            Private — only you
          </label>
          <textarea
            id="privateNotes"
            className={NOTES_TEXTAREA}
            placeholder="Your own reminders."
            value={privateNotes}
            onChange={(e) => setPrivateNotes(e.target.value)}
            onBlur={savePrivate}
          />
        </>
      )}
    </div>
  );
}
