import { useEffect, useState } from 'react';
import { Info } from 'lucide-react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { runRequest } from '../../api/mutate';
import { GamePanel } from './GamePanel';

const NOTES_TEXTAREA =
  'w-full bg-surface/70 text-sm text-ink placeholder:text-ink-muted p-2 outline-none resize-none scrollable disabled:opacity-60';

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
    <GamePanel
      id="notesCard"
      bodyClassName="flex flex-col"
      title="Notes"
      toggle={game.player ? { icon: <Info size={13} />, label: 'Deck', onClick: onToggleDeck } : undefined}
    >
      <textarea
        id="globalNotes"
        className={NOTES_TEXTAREA}
        placeholder="Global Notes"
        disabled={!(game.player || game.judge)}
        value={globalNotes}
        onChange={(e) => setGlobalNotes(e.target.value)}
        onBlur={saveGlobal}
      />
      {game.player && (
        <textarea
          id="privateNotes"
          className={`${NOTES_TEXTAREA} border-t border-line`}
          placeholder="Private Notes"
          value={privateNotes}
          onChange={(e) => setPrivateNotes(e.target.value)}
          onBlur={savePrivate}
        />
      )}
    </GamePanel>
  );
}
