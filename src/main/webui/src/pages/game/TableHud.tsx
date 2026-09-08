import { Clock, History, MessageSquare, WifiOff, X } from 'lucide-react';
import type { GameSnapshot } from '../../api/types';
import { accentFor } from './chatLogStyle';
import { useSocketConnected } from '../../api/useSocketStatus';
import { useConnectivity } from '../../api/useConnectivity';
import { useIsMobile } from '../../hooks/useMediaQuery';
import { PhaseStepper } from './PhaseStepper';
import { PendingActionBar } from './PendingActionBar';
import { CallJudgeButton } from './CallJudgeButton';
import { NotesToggleButton } from './NotesToggleButton';
import type { NotesIndicator } from './useNotesIndicator';

// Terse elapsed-since for the "waiting" chip — a duration ("4d", "3h", "12m"),
// NOT relativeTime's "4 days ago" which reads wrong as time-waited.
function shortDuration(iso: string): string {
  const ms = Date.now() - new Date(iso).getTime();
  if (!Number.isFinite(ms) || ms < 60_000) return 'just now';
  const mins = Math.floor(ms / 60_000);
  if (mins < 60) return `${mins}m`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h`;
  return `${Math.floor(hrs / 24)}d`;
}

const CHIP = 'inline-flex items-center gap-1 rounded-full border border-line-accent px-2 py-0.5 text-xs text-ink-secondary hover:bg-hover';

// Persistent turn/phase ribbon (Main.dc.html HUD): game · turn · active seat ·
// phase stepper · waiting · edge, then a right-hand cluster of Call Judge /
// Notes / History (moved here from the panel headers, D26). Sticky — never
// inside a scroller. Also the single home for the rejected-command message (D5)
// and the push-channel connection cue.
export function TableHud({
  game,
  gameId,
  viewerName,
  onSubmitPhase,
  canSubmitPhase,
  commandStatus,
  onClearStatus,
  onPendingCommand,
  onPendingRespond,
  onUpdated,
  submitting,
  guard,
  onOpenNotes,
  notesIndicator,
  showHistory,
  onToggleHistory,
}: {
  game: GameSnapshot;
  gameId: string;
  viewerName: string | null;
  onSubmitPhase: (phase: string) => void;
  canSubmitPhase: boolean;
  commandStatus: string;
  onClearStatus: () => void;
  onPendingCommand: (command: string) => void;
  onPendingRespond: () => void;
  onUpdated: (updated: GameSnapshot) => void;
  submitting: boolean;
  guard: <T>(run: () => Promise<T>) => Promise<T | undefined>;
  onOpenNotes: () => void;
  notesIndicator: NotesIndicator;
  showHistory: boolean;
  onToggleHistory: () => void;
}) {
  const socketConnected = useSocketConnected();
  const { online } = useConnectivity();
  const stale = !socketConnected || !online;
  const isMobile = useIsMobile();

  const isMyTurn = !!viewerName && viewerName === game.currentPlayer;
  const activeAccent = accentFor(game.currentPlayer, game.seating);

  const activePlayer = game.players.find((p) => p.name === game.currentPlayer);
  const lastActionAt = activePlayer?.lastActionAt;
  const waiting = lastActionAt ? shortDuration(lastActionAt) : null;

  // `game.edgePlayer` serialises the literal "no one" when unclaimed (truthy),
  // so guard for it explicitly alongside empty/undefined.
  const rawEdge = game.edgePlayer?.trim();
  const edgeHolder = rawEdge && rawEdge.toLowerCase() !== 'no one' ? rawEdge : null;

  const titleEl = <h1 className="truncate text-sm font-semibold text-ink select-all">{game.name}</h1>;

  const turnEls = (
    <>
      <span className="text-xs text-ink-secondary" title={game.turnLabel}>
        {game.turnLabel}
      </span>
      {isMyTurn ? (
        <span className="inline-flex items-center rounded-full border border-accent/40 bg-accent/15 px-2 py-0.5 text-xs font-medium text-accent">
          Your turn
        </span>
      ) : (
        <span className="text-xs font-medium" style={activeAccent ? { color: activeAccent } : undefined}>
          {game.currentPlayer}
          <span className="text-ink-muted">’s turn</span>
        </span>
      )}
      <PhaseStepper
        current={game.phase}
        selectablePhases={game.phases}
        canSelect={canSubmitPhase}
        active={isMyTurn}
        onSelect={onSubmitPhase}
      />
      {waiting && !isMyTurn && (
        <span className="inline-flex items-center gap-1 text-xs text-gold" title={lastActionAt ?? undefined}>
          <Clock size={11} />
          waiting {waiting}
        </span>
      )}
      <span className="inline-flex items-center gap-1 text-xs text-ink-muted">
        <span aria-hidden>◆</span>
        {edgeHolder ? (
          <span style={{ color: game.edgeColor }}>Edge {edgeHolder}</span>
        ) : (
          <span>Edge unclaimed</span>
        )}
      </span>
    </>
  );

  const reconnectingChip = stale && (
    <span
      className="inline-flex items-center gap-1 rounded-full border border-gold/50 bg-gold/15 px-2 py-0.5 text-xs text-gold"
      title={online ? 'Reconnecting to live updates…' : 'Reconnecting to the server…'}
    >
      <WifiOff size={11} />
      Reconnecting…
    </span>
  );

  const metaEls = (
    <>
      {reconnectingChip}
      <CallJudgeButton gameId={gameId} game={game} onUpdated={onUpdated} submitting={submitting} guard={guard} />
      <NotesToggleButton indicator={notesIndicator} onClick={onOpenNotes} />
      <button type="button" onClick={onToggleHistory} className={CHIP} title={showHistory ? 'Back to live chat' : 'Browse older turns'}>
        {showHistory ? <MessageSquare size={11} /> : <History size={11} />}
        {showHistory ? 'Chat' : 'History'}
      </button>
    </>
  );

  // <md the right cluster is two icons only (Mobile.dc.html). Call Judge moves
  // to the Act sheet (it's a seated-player control, GamePage renders it there).
  const metaElsMobile = (
    <>
      {reconnectingChip}
      <NotesToggleButton indicator={notesIndicator} onClick={onOpenNotes} compact />
      <button
        type="button"
        onClick={onToggleHistory}
        aria-label={showHistory ? 'Back to live chat' : 'Browse older turns'}
        title={showHistory ? 'Back to live chat' : 'Browse older turns'}
        className="inline-flex h-8 w-8 items-center justify-center rounded-full border border-line-accent bg-surface text-ink-secondary hover:bg-hover"
      >
        {showHistory ? <MessageSquare size={15} /> : <History size={15} />}
      </button>
    </>
  );

  return (
    <div className="sticky top-0 z-30 shrink-0 border-b border-line bg-base/95 backdrop-blur-sm">
      {isMobile ? (
        <div className="flex flex-col gap-1 px-3 py-1.5">
          <div className="flex items-center justify-between gap-x-3">
            {titleEl}
            <span className="flex shrink-0 items-center gap-2">{metaElsMobile}</span>
          </div>
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1">{turnEls}</div>
        </div>
      ) : (
        <div className="flex flex-wrap items-center gap-x-3 gap-y-1 px-3 py-1.5">
          {titleEl}
          {turnEls}
          <span className="ml-auto flex items-center gap-2">{metaEls}</span>
        </div>
      )}

      {game.pendingAction && (
        <PendingActionBar
          pending={game.pendingAction}
          viewerName={viewerName}
          onCommand={onPendingCommand}
          onRespond={onPendingRespond}
        />
      )}

      {commandStatus && (
        <div className="flex items-center gap-2 border-t border-blood/30 bg-blood/10 px-3 py-1 text-xs text-blood">
          <span className="flex-1">{commandStatus}</span>
          <button
            type="button"
            onClick={onClearStatus}
            title="Dismiss"
            aria-label="Dismiss message"
            className="shrink-0 rounded p-0.5 hover:bg-blood/15"
          >
            <X size={12} />
          </button>
        </div>
      )}
    </div>
  );
}
