import { useEffect, useState } from 'react';
import { History, Terminal } from 'lucide-react';
import { api } from '../../api/client';
import type { ChatData, CommandError, GameSnapshot } from '../../api/types';
import { GameChatLog } from './GameChatLog';
import { GamePanel } from './GamePanel';
import { NotesToggleButton } from './NotesToggleButton';
import type { NotesIndicator } from './useNotesIndicator';
import { useShowCommands } from './useShowCommands';

export function GameChatPanel({
  gameId,
  game,
  viewerName,
  onToggleHistory,
  notesIndicator,
  onOpenNotes,
}: {
  gameId: string;
  game: GameSnapshot;
  viewerName: string | null;
  onToggleHistory: () => void;
  notesIndicator: NotesIndicator;
  onOpenNotes: () => void;
}) {
  const [lines, setLines] = useState<ChatData[]>([]);
  const [errors, setErrors] = useState<CommandError[]>([]);
  const [showCommands, toggleCommands] = useShowCommands();
  const judgeCommands = game.judge && showCommands;

  useEffect(() => {
    api
      .get<ChatData[]>(`/game/${gameId}/history?turn=${encodeURIComponent(game.turnLabel)}`)
      .then(setLines)
      .catch((err) => console.error('Failed to load game chat', err));
    // Refetch on every fresh snapshot (game.stamp), not just turn changes —
    // matches legacy pulling forward new lines on every refresh.
  }, [gameId, game.turnLabel, game.stamp]);

  useEffect(() => {
    if (!judgeCommands) {
      setErrors([]);
      return;
    }
    api
      .get<CommandError[]>(`/game/${gameId}/command-errors?turn=${encodeURIComponent(game.turnLabel)}`)
      .then(setErrors)
      .catch(() => setErrors([]));
  }, [judgeCommands, gameId, game.turnLabel, game.stamp]);

  return (
    <GamePanel
      id="gameChatCard"
      className="chat"
      bodyClassName="p-0 overflow-hidden"
      title="Game Chat"
      headerExtra={
        <span className="flex items-center gap-2">
          {game.judge && (
            <button
              type="button"
              aria-pressed={showCommands}
              onClick={toggleCommands}
              title="Show the raw command behind each line, and mistyped attempts"
              className={`inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs ${
                showCommands
                  ? 'border-line-accent bg-hover text-ink'
                  : 'border-line text-ink-muted hover:bg-hover'
              }`}
            >
              <Terminal size={12} />
              Commands
            </button>
          )}
          <NotesToggleButton indicator={notesIndicator} onClick={onOpenNotes} />
        </span>
      }
      toggle={{ icon: <History size={13} />, label: 'History', onClick: onToggleHistory }}
    >
      <GameChatLog lines={lines} viewerName={viewerName} showCommands={judgeCommands} errors={errors} seating={game.seating} />
    </GamePanel>
  );
}
