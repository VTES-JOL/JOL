import { useState } from 'react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { QuickCommandModal } from './QuickCommandModal';
import { Button } from '../../components/ui/Button';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { submitHeaders } from './submitId';

// The command controls as a single horizontal band — quick-command · input ·
// ping · Submit · End Turn — sitting level with the hand chips in the dock
// (Main.dc.html). Seated-player only; Call Judge / Notes / History moved to the
// HUD (D26), table talk to ChatCompose (D24), phase to the HUD stepper (C4).
export function CommandForm({
  gameId,
  game,
  viewerName,
  onUpdated,
  captureStatus,
  submitting,
  guard,
}: {
  gameId: string;
  game: GameSnapshot;
  viewerName: string | null;
  onUpdated: (updated: GameSnapshot) => void;
  // Lifts the engine's rejection message off the POST response into GamePage's
  // shared useCommandStatus, which renders it once in the TableHud (D5).
  captureStatus: (updated: GameSnapshot) => GameSnapshot;
  // Shared with GamePage's card-click submissions (see useSubmitGuard) so only
  // one game-mutating request is ever in flight at a time, regardless of which
  // control fired it.
  submitting: boolean;
  guard: <T>(run: () => Promise<T>) => Promise<T | undefined>;
}) {
  const [command, setCommand] = useState('');
  const [showQuickCommand, setShowQuickCommand] = useState(false);
  // Dedicated pending flag for End Turn so it reads "Ending turn…" the instant
  // it's clicked (optimistic, D15).
  const [endingTurn, setEndingTurn] = useState(false);

  const isMyTurn = viewerName === game.currentPlayer;

  const submit = () => {
    if (!command) return;
    guard(() =>
      runRequest(
        api.post<GameSnapshot>(
          `/game/${gameId}/view/submit`,
          { phase: null, command, chat: null, ping: null },
          submitHeaders(),
        ),
        'Failed to submit',
        (updated) => {
          setCommand('');
          captureStatus(updated);
          onUpdated(updated);
        },
      ),
    );
  };

  const sendQuickCommand = (quickCommand: string) => {
    guard(() =>
      runRequest(
        api.post<GameSnapshot>(
          `/game/${gameId}/view/submit`,
          { phase: null, command: quickCommand, chat: null, ping: null },
          submitHeaders(),
        ),
        'Failed to submit',
        (updated) => {
          captureStatus(updated);
          onUpdated(updated);
        },
      ),
    );
  };

  const endTurn = async () => {
    // Advisory warning (rules R1): a response window you opened that still has
    // seats owing input closes silently on turn end unless you're reminded.
    const pa = game.pendingAction;
    const owed = pa && pa.actor === viewerName ? pa.awaiting.length : 0;
    const message = owed
      ? `You declared a ${pa!.label}${pa!.targetPlayer ? ` vs ${pa!.targetPlayer}` : ''}; ` +
        `${owed} player${owed > 1 ? 's have' : ' has'} not responded. End turn anyway?`
      : 'Play passes to the next player.';
    if (
      !(await confirmDialog(message, {
        title: 'End your turn?',
        confirmLabel: 'End turn',
      }))
    )
      return;
    setEndingTurn(true);
    guard(() =>
      runRequest(
        api.post<GameSnapshot>(`/game/${gameId}/view/end-turn`),
        'Failed to end turn',
        (updated) => {
          captureStatus(updated);
          onUpdated(updated);
        },
      ),
    ).finally(() => setEndingTurn(false));
  };

  if (!game.player) return null;

  return (
    <>
      <form
        id="commandForm"
        onSubmit={(e) => {
          e.preventDefault();
          submit();
        }}
        autoComplete="off"
        className="flex flex-wrap items-center gap-1.5"
      >
        <button
          type="button"
          aria-label="Quick command shortcuts"
          title="Quick commands"
          disabled={submitting}
          onClick={() => setShowQuickCommand(true)}
          className="inline-flex min-h-11 min-w-11 md:min-h-0 md:min-w-0 shrink-0 items-center justify-center rounded border border-line-accent px-2 text-sm leading-none text-ink-muted hover:text-ink disabled:opacity-40"
        >
          …
        </button>
        <input
          id="command"
          type="text"
          placeholder="Enter game commands"
          value={command}
          onChange={(e) => setCommand(e.target.value)}
          className="min-h-11 min-w-[10rem] flex-1 rounded border border-line bg-surface/70 px-2 py-1.5 text-sm text-ink outline-none focus:border-accent/60 md:min-h-0"
        />
        <Button variant="primary" size="sm" type="submit" disabled={submitting} className="min-h-11 md:min-h-0">
          {submitting ? 'Submitting…' : 'Submit'}
        </Button>
        <Button
          variant="secondary"
          size="sm"
          type="button"
          disabled={!isMyTurn || submitting || endingTurn}
          onClick={endTurn}
          className="min-h-11 md:min-h-0"
        >
          {endingTurn ? 'Ending turn…' : 'End Turn'}
        </Button>
      </form>
      {showQuickCommand && <QuickCommandModal onSend={sendQuickCommand} onClose={() => setShowQuickCommand(false)} />}
    </>
  );
}
