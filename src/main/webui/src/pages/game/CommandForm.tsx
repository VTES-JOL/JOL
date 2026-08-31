import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { QuickCommandModal } from './QuickCommandModal';
import { QuickChatModal } from './QuickChatModal';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';

const FIELD_LABEL = 'block text-xs text-ink-muted mb-0.5';
const CMD_INPUT =
  'flex-1 min-w-0 rounded-r border border-l-0 border-line bg-surface/70 px-2 py-1 text-sm text-ink outline-none focus:border-accent/60';

// Mirrors commands.jsp/doSubmit()/doEndTurn() plus the quick-command/
// quick-chat modals — free-text Command, Chat, Phase, Ping, submitted
// together, plus ending your own turn and the canned-button shortcuts for
// both. Card-click actions (card-modal.js) are handled in GamePage.
export function CommandForm({
  gameId,
  game,
  viewerName,
  onUpdated,
  submitting,
  guard,
}: {
  gameId: string;
  game: GameSnapshot;
  viewerName: string | null;
  onUpdated: (updated: GameSnapshot) => void;
  // Shared with GamePage's card-click submissions (see useSubmitGuard) so
  // only one game-mutating request is ever in flight at a time, regardless
  // of which control fired it — closes the double-submit window a plain
  // per-button `submitting` flag can't (two clicks/Enters in the same tick,
  // before React re-renders and disables anything, would both still pass).
  submitting: boolean;
  guard: <T>(run: () => Promise<T>) => Promise<T | undefined>;
}) {
  const [phase, setPhase] = useState(game.phases[0] ?? '');
  const [command, setCommand] = useState('');
  const [chat, setChat] = useState('');
  const [ping, setPing] = useState('');
  const [showQuickCommand, setShowQuickCommand] = useState(false);
  const [showQuickChat, setShowQuickChat] = useState(false);
  // Deliberately local, not read off `game.status` — the submit response's
  // status is transient (server never persists it), but `game` itself gets
  // clobbered by the very next refresh this same submit triggers: saving
  // game state always fires a WebSocket push to everyone in the room,
  // including the submitter, and GamePage's plain GET /view refetch in
  // response to it always carries status: null. That refetch typically lands
  // within the same tick as this request's own response, so a status read
  // from `game.status` would flash (or never render at all) regardless of
  // who is watching. Keeping it here instead means it survives until this
  // player's own next submit.
  const [status, setStatus] = useState('');

  useEffect(() => setPhase(game.phases[0] ?? ''), [game.phases]);

  // player-only controls (Phase/Command/Ping/End Turn) vs can-chat (Chat +
  // global notes, also open to judges) — see callbackGame's playerControls/
  // chatControls toggling.
  const canPlay = game.player;
  const canChat = game.player || game.judge;
  const isMyTurn = viewerName === game.currentPlayer;

  const submit = () => {
    if (!command && !chat && !phase) return;
    guard(() =>
      runRequest(
        api.post<GameSnapshot>(`/game/${gameId}/view/submit`, {
          phase: phase || null,
          command: command || null,
          chat: chat || null,
          ping: ping || null,
        }),
        'Failed to submit',
        (updated) => {
          setCommand('');
          setChat('');
          setPing('');
          setStatus(updated.status ?? '');
          onUpdated(updated);
        },
      ),
    );
  };

  const sendQuickCommand = (quickCommand: string) => {
    guard(() =>
      runRequest(
        api.post<GameSnapshot>(`/game/${gameId}/view/submit`, { phase: null, command: quickCommand, chat: null, ping: null }),
        'Failed to submit',
        (updated) => {
          setStatus(updated.status ?? '');
          onUpdated(updated);
        },
      ),
    );
  };

  const sendQuickChat = (message: string) => {
    guard(() =>
      runRequest(
        api.post<GameSnapshot>(`/game/${gameId}/view/submit`, { phase: null, command: null, chat: message, ping: null }),
        'Failed to submit',
        (updated) => {
          setStatus(updated.status ?? '');
          onUpdated(updated);
        },
      ),
    );
  };

  const endTurn = async () => {
    if (!(await confirmDialog('Are you sure you want to end your turn?'))) return;
    guard(() => runRequest(api.post<GameSnapshot>(`/game/${gameId}/view/end-turn`), 'Failed to end turn', onUpdated));
  };

  return (
    <div className="commands flex flex-col min-h-0 rounded-lg border border-line-accent bg-surface/85 shadow-lg overflow-hidden">
      <div className="px-3 py-1.5 border-b border-line bg-panel/60 text-sm font-semibold text-ink shrink-0">
        Commands
      </div>
      <div className="flex-1 min-h-0 overflow-y-auto p-2">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            submit();
          }}
          autoComplete="off"
        >
          {canPlay && (
            <>
              <Select
                id="phase"
                label="Phase"
                size="sm"
                className="mb-2"
                value={phase}
                disabled={!isMyTurn}
                onChange={(e) => setPhase(e.target.value)}
              >
                {game.phases.map((p) => (
                  <option key={p} value={p}>
                    {p}
                  </option>
                ))}
              </Select>
              <label htmlFor="command" className={FIELD_LABEL}>
                Command
              </label>
              <div className="flex mb-2">
                <Button
                  variant="secondary"
                  size="sm"
                  type="button"
                  tabIndex={-1}
                  disabled={submitting}
                  className="rounded-r-none"
                  onClick={() => setShowQuickCommand(true)}
                >
                  ...
                </Button>
                <input
                  type="text"
                  className={CMD_INPUT}
                  id="command"
                  placeholder="Enter game commands"
                  value={command}
                  onChange={(e) => setCommand(e.target.value)}
                />
              </div>
            </>
          )}
          <label htmlFor="chat" className={FIELD_LABEL}>
            Chat
          </label>
          <div className="flex mb-2">
            <Button
              variant="secondary"
              size="sm"
              type="button"
              tabIndex={-1}
              disabled={!canChat || submitting}
              className="rounded-r-none"
              onClick={() => setShowQuickChat(true)}
            >
              ...
            </Button>
            <input
              type="text"
              className={CMD_INPUT}
              id="chat"
              placeholder="Chat to other players"
              value={chat}
              disabled={!canChat}
              onChange={(e) => setChat(e.target.value)}
            />
          </div>
          {canPlay && (
            <>
              <Select
                id="ping"
                label="Ping"
                size="sm"
                className="mb-2"
                value={ping}
                onChange={(e) => setPing(e.target.value)}
              >
                <option value="" />
                {game.pingOptions.map((p) => (
                  <option key={p} value={p}>
                    {p}
                  </option>
                ))}
              </Select>
              <div className="mt-2 flex justify-between">
                <Button variant="secondary" size="sm" type="submit" disabled={submitting}>
                  {submitting ? 'Submitting…' : 'Submit'}
                </Button>
                <Button
                  size="sm"
                  type="button"
                  disabled={!isMyTurn || submitting}
                  className="bg-gold text-surface hover:bg-gold-soft"
                  onClick={endTurn}
                >
                  End Turn
                </Button>
              </div>
            </>
          )}
          {!canPlay && (
            <Button variant="secondary" size="sm" type="submit" className="mt-2" disabled={submitting || !canChat}>
              {submitting ? 'Submitting…' : 'Submit'}
            </Button>
          )}
        </form>
        {status && <div className="text-blood text-sm mt-2">{status}</div>}
      </div>
      {showQuickCommand && <QuickCommandModal onSend={sendQuickCommand} onClose={() => setShowQuickCommand(false)} />}
      {showQuickChat && <QuickChatModal onSend={sendQuickChat} onClose={() => setShowQuickChat(false)} />}
    </div>
  );
}
