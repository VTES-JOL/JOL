import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';
import { QuickCommandModal } from './QuickCommandModal';
import { QuickChatModal } from './QuickChatModal';

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
}: {
  gameId: string;
  game: GameSnapshot;
  viewerName: string | null;
  onUpdated: (updated: GameSnapshot) => void;
}) {
  const [phase, setPhase] = useState(game.phases[0] ?? '');
  const [command, setCommand] = useState('');
  const [chat, setChat] = useState('');
  const [ping, setPing] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [showQuickCommand, setShowQuickCommand] = useState(false);
  const [showQuickChat, setShowQuickChat] = useState(false);

  useEffect(() => setPhase(game.phases[0] ?? ''), [game.phases]);

  // player-only controls (Phase/Command/Ping/End Turn) vs can-chat (Chat +
  // global notes, also open to judges) — see callbackGame's playerControls/
  // chatControls toggling.
  const canPlay = game.player;
  const canChat = game.player || game.judge;
  const isMyTurn = viewerName === game.currentPlayer;

  const submit = () => {
    if (!command && !chat && !phase) return;
    setSubmitting(true);
    api
      .post<GameSnapshot>(`/game/${gameId}/view/submit`, {
        phase: phase || null,
        command: command || null,
        chat: chat || null,
        ping: ping || null,
      })
      .then((updated) => {
        setCommand('');
        setChat('');
        setPing('');
        onUpdated(updated);
      })
      .catch((err) => console.error('Failed to submit', err))
      .finally(() => setSubmitting(false));
  };

  const sendQuickCommand = (quickCommand: string) => {
    api
      .post<GameSnapshot>(`/game/${gameId}/view/submit`, { phase: null, command: quickCommand, chat: null, ping: null })
      .then(onUpdated)
      .catch((err) => console.error('Failed to submit', err));
  };

  const sendQuickChat = (message: string) => {
    api
      .post<GameSnapshot>(`/game/${gameId}/view/submit`, { phase: null, command: null, chat: message, ping: null })
      .then(onUpdated)
      .catch((err) => console.error('Failed to submit', err));
  };

  const endTurn = () => {
    if (!confirm('Are you sure you want to end your turn?')) return;
    api
      .post<GameSnapshot>(`/game/${gameId}/view/end-turn`)
      .then(onUpdated)
      .catch((err) => console.error('Failed to end turn', err));
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
                <button type="button" className="btn btn-outline-secondary" tabIndex={-1} onClick={() => setShowQuickCommand(true)}>
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
            <button type="button" className="btn btn-outline-secondary" tabIndex={-1} disabled={!canChat} onClick={() => setShowQuickChat(true)}>
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
                  Submit
                </button>
                <button className="btn btn-warning btn-sm" type="button" disabled={!isMyTurn} onClick={endTurn}>
                  End Turn
                </button>
              </div>
            </>
          )}
          {!canPlay && (
            <button className="btn btn-secondary btn-sm mt-2" type="submit" disabled={submitting || !canChat}>
              Submit
            </button>
          )}
        </form>
        {game.status && <div className="text-danger small mt-2">{game.status}</div>}
      </div>
      {showQuickCommand && <QuickCommandModal onSend={sendQuickCommand} onClose={() => setShowQuickCommand(false)} />}
      {showQuickChat && <QuickChatModal onSend={sendQuickChat} onClose={() => setShowQuickChat(false)} />}
    </div>
  );
}
