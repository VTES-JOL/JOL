import { useCallback, useState } from 'react';
import type { GameSnapshot } from '../../api/types';

/**
 * Holds the engine's feedback for a rejected / no-op command (e.g.
 * "No amount given use +/-", "that minion is already locked").
 *
 * The engine reports this by returning HTTP 200 with `status` set on the
 * GameSnapshot in the submit response — it is never persisted. It CANNOT be
 * read off the `game` prop: saving game state pushes a WebSocket signal to
 * every tab including the submitter, and the `GET /view` refetch that signal
 * triggers always carries `status: null` and typically lands in the same tick
 * as the submit response. So the message has to be lifted into local
 * component state the instant the POST resolves, where the self-triggered
 * refetch can't clobber it. (See CommandForm's regression test.)
 *
 * Both CommandForm (free-text / quick-command / quick-chat / end-turn) and
 * GamePage (context menu, card-action modal, quick lock/unlock, target picks,
 * play-card modal) run their submissions through this so every path that can
 * issue a command surfaces the same rejection feedback — previously only
 * CommandForm did, and a rejected right-click action gave no feedback at all.
 */
export function useCommandStatus() {
  const [status, setStatus] = useState('');

  // Capture the rejection off a fresh submit / end-turn response and pass the
  // snapshot straight through, so it slots into an existing success chain
  // without restructuring it: `(updated) => { captureStatus(updated); applyUpdate(updated); }`.
  //
  // The rejection is keyed on `rejected` (D8 / backend relay) — `status` is
  // just the human message and is cleared on a non-rejected response. Older
  // responses without `rejected` fall back to "message present ⇒ rejected".
  const captureStatus = useCallback((snapshot: GameSnapshot): GameSnapshot => {
    const rejected = snapshot.rejected ?? !!snapshot.status;
    setStatus(rejected ? (snapshot.status || 'Command rejected.') : '');
    return snapshot;
  }, []);

  const clearStatus = useCallback(() => setStatus(''), []);

  return { status, captureStatus, clearStatus };
}
