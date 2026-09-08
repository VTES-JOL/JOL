import { useState } from 'react';
import { MessagesSquare, Terminal } from 'lucide-react';
import type { GameSnapshot } from '../../api/types';
import { GameChatLog } from './GameChatLog';
import { GamePanel } from './GamePanel';
import { useShowCommands } from './useShowCommands';
import { useChatSeenMarker } from './useChatSeenMarker';

// The live chat log. Notes / History / Call Judge moved to the HUD (D26); the
// header keeps the judge-only "Commands" toggle (raw command behind each line)
// and an All / Talk filter — "Talk" drops the mechanical `move` lines and the
// per-turn `phase` markers, leaving what players actually said to the table
// plus the game-event `system` lines (turn, oust, timeout, contest).
export function GameChatPanel({
  game,
  gameId,
  viewerName,
}: {
  game: GameSnapshot;
  gameId: string;
  viewerName: string | null;
}) {
  const [showCommands, toggleCommands] = useShowCommands();
  const [talkOnly, setTalkOnly] = useState(() => {
    try {
      return localStorage.getItem('jol:chatTalkOnly') === '1';
    } catch {
      return false;
    }
  });
  const judgeCommands = game.judge && showCommands;

  // The current turn's lines and failed-command attempts ride on the game
  // snapshot itself (GameSnapshotFactory), so they refresh in the same round
  // trip as the board. HistoryPanel still fetches for browsing older turns.
  const lines = game.chat;
  const errors = judgeCommands ? game.commandErrors : [];

  // Marker frozen from all lines (so it tracks the true newest); the filter
  // only changes what's shown.
  const newSince = useChatSeenMarker(gameId, lines);
  const shown = talkOnly ? lines.filter((l) => l.kind === 'talk' || l.kind === 'system') : lines;

  const setTalk = (v: boolean) => {
    setTalkOnly(v);
    try {
      localStorage.setItem('jol:chatTalkOnly', v ? '1' : '0');
    } catch {
      /* ignore */
    }
  };

  return (
    <GamePanel
      id="gameChatCard"
      className="chat flex-1 min-h-0"
      bodyClassName="p-0 overflow-hidden"
      title="Game Chat"
      headerExtra={
        <div className="flex items-center gap-1">
          <div className="inline-flex overflow-hidden rounded-full border border-line text-xs">
            <button
              type="button"
              aria-pressed={!talkOnly}
              onClick={() => setTalk(false)}
              className={`px-2 py-0.5 ${!talkOnly ? 'bg-hover text-ink' : 'text-ink-muted hover:bg-hover'}`}
            >
              All
            </button>
            <button
              type="button"
              aria-pressed={talkOnly}
              onClick={() => setTalk(true)}
              title="Hide the mechanical move log and phase markers"
              className={`inline-flex items-center gap-1 border-l border-line px-2 py-0.5 ${
                talkOnly ? 'bg-hover text-ink' : 'text-ink-muted hover:bg-hover'
              }`}
            >
              <MessagesSquare size={12} />
              Talk
            </button>
          </div>
          {game.judge && (
            <button
              type="button"
              aria-pressed={showCommands}
              onClick={toggleCommands}
              title="Show the raw command behind each line, and mistyped attempts"
              className={`inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs ${
                showCommands ? 'border-line-accent bg-hover text-ink' : 'border-line text-ink-muted hover:bg-hover'
              }`}
            >
              <Terminal size={12} />
              Commands
            </button>
          )}
        </div>
      }
    >
      <GameChatLog
        lines={shown}
        viewerName={viewerName}
        showCommands={judgeCommands}
        errors={errors}
        seating={game.seating}
        newSince={newSince}
      />
    </GamePanel>
  );
}
