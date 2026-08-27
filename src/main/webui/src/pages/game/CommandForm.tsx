import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { QuickCommandModal } from './QuickCommandModal';
import { QuickChatModal } from './QuickChatModal';
import { confirmDialog } from '../../components/dialog';
import { runRequest } from '../../api/mutate';

// Mirrors commands.jsp/doSubmit()/doEndTurn() plus the quick-command/
// quick-chat modals (Phase 3) — free-text Command, Chat, Phase, Ping,
// submitted together, plus ending your own turn and the canned-button
// shortcuts for both. Card-click actions (card-modal.js) are Phase 2, see
// GamePage.
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
    <div className="card shadow commands">
      <div className="card-header bg-body-secondary">Commands</div>
      <div className="card-body p-2">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            submit();
          }}
          autoComplete="off"
        >
          {canPlay && (
            <>
              <label htmlFor="phase">Phase</label>
              <select
                id="phase"
                className="form-select form-select-sm mb-2"
                value={phase}
                disabled={!isMyTurn}
                onChange={(e) => setPhase(e.target.value)}
              >
                {game.phases.map((p) => (
                  <option key={p} value={p}>
                    {p}
                  </option>
                ))}
              </select>
              <label htmlFor="command">Command</label>
              <div className="input-group input-group-sm mb-2">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  tabIndex={-1}
                  disabled={submitting}
                  onClick={() => setShowQuickCommand(true)}
                >
                  ...
                </button>
                <input
                  type="text"
                  className="form-control form-control-sm"
                  id="command"
                  placeholder="Enter game commands"
                  value={command}
                  onChange={(e) => setCommand(e.target.value)}
                />
              </div>
            </>
          )}
          <label htmlFor="chat">Chat</label>
          <div className="input-group input-group-sm mb-2">
            <button
              type="button"
              className="btn btn-outline-secondary"
              tabIndex={-1}
              disabled={!canChat || submitting}
              onClick={() => setShowQuickChat(true)}
            >
              ...
            </button>
            <input
              type="text"
              className="form-control form-control-sm"
              id="chat"
              placeholder="Chat to other players"
              value={chat}
              disabled={!canChat}
              onChange={(e) => setChat(e.target.value)}
            />
          </div>
          {canPlay && (
            <>
              <label htmlFor="ping">Ping</label>
              <select id="ping" className="form-select form-select-sm mb-2" value={ping} onChange={(e) => setPing(e.target.value)}>
                <option value="" />
                {game.pingOptions.map((p) => (
                  <option key={p} value={p}>
                    {p}
                  </option>
                ))}
              </select>
              <div className="mt-2 d-flex justify-content-between">
                <button className="btn btn-secondary btn-sm" type="submit" disabled={submitting}>
                  {submitting ? 'Submitting…' : 'Submit'}
                </button>
                <button className="btn btn-warning btn-sm" type="button" disabled={!isMyTurn || submitting} onClick={endTurn}>
                  End Turn
                </button>
              </div>
            </>
          )}
          {!canPlay && (
            <button className="btn btn-secondary btn-sm mt-2" type="submit" disabled={submitting || !canChat}>
              {submitting ? 'Submitting…' : 'Submit'}
            </button>
          )}
        </form>
        {status && <div className="text-danger small mt-2">{status}</div>}
      </div>
      {showQuickCommand && <QuickCommandModal onSend={sendQuickCommand} onClose={() => setShowQuickCommand(false)} />}
      {showQuickChat && <QuickChatModal onSend={sendQuickChat} onClose={() => setShowQuickChat(false)} />}
    </div>
  );
}
