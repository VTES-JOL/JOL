import type { GameSnapshot, PlayerSnapshot } from '../../api/types';
import { PendingActionBar } from './PendingActionBar';
import { ActQuickBar } from './ActQuickBar';
import { CommandForm } from './CommandForm';
import { CallJudgeButton } from './CallJudgeButton';

// The command controls the dock and the mobile Act sheet both assemble from the
// same pieces — PendingActionBar, ActQuickBar, CommandForm (+ Call Judge on the
// sheet). Two arrangements:
//
//  - variant="dock"  — response window on top (with a Respond→focus hook),
//                      then the quick-action strip, then the command band.
//  - variant="sheet" — command band first (thumb reach), then quick actions,
//                      then Call Judge (which lives in the HUD on desktop).

export function DockCommandStack({
  variant,
  className,
  game,
  gameId,
  viewerName,
  me,
  isMyTurn,
  pendingActionable,
  onCommand,
  onUpdated,
  captureStatus,
  submitting,
  guard,
  onRespondFocus,
}: {
  variant: 'dock' | 'sheet';
  className?: string;
  game: GameSnapshot;
  gameId: string;
  viewerName: string | null;
  me: PlayerSnapshot | null;
  isMyTurn: boolean;
  /** A pending response window that needs THIS viewer to act. */
  pendingActionable: boolean;
  onCommand: (command: string) => void;
  onUpdated: (updated: GameSnapshot) => void;
  captureStatus: (updated: GameSnapshot) => GameSnapshot;
  submitting: boolean;
  guard: <T>(run: () => Promise<T>) => Promise<T | undefined>;
  /** dock only — where "Respond" moves focus (the command input). */
  onRespondFocus?: () => void;
}) {
  const pendingBar = pendingActionable && game.pendingAction && (
    <div className="overflow-hidden rounded border border-line">
      <PendingActionBar
        pending={game.pendingAction}
        viewerName={viewerName}
        onCommand={onCommand}
        onRespond={() => onRespondFocus?.()}
      />
    </div>
  );

  const quickBar = game.player && (
    <ActQuickBar
      phase={game.phase}
      isMyTurn={isMyTurn}
      pending={game.pendingAction}
      me={me}
      players={game.players}
      onCommand={onCommand}
    />
  );

  const commandForm = (
    <CommandForm
      gameId={gameId}
      game={game}
      viewerName={viewerName}
      onUpdated={onUpdated}
      captureStatus={captureStatus}
      submitting={submitting}
      guard={guard}
    />
  );

  if (variant === 'sheet') {
    return (
      <div className={className ?? 'flex flex-col gap-3'}>
        {commandForm}
        {quickBar}
        <CallJudgeButton gameId={gameId} game={game} onUpdated={onUpdated} submitting={submitting} guard={guard} />
      </div>
    );
  }

  return (
    <div className={className ?? 'flex flex-col gap-1.5'}>
      {pendingBar}
      {quickBar}
      {commandForm}
    </div>
  );
}
