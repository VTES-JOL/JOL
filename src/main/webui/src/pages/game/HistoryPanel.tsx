import { useEffect, useState } from 'react';
import { Terminal } from 'lucide-react';
import { api } from '../../api/client';
import type { ChatData, CommandError, GameSnapshot } from '../../api/types';
import { Select } from '../../components/ui/Select';
import { GameChatLog } from './GameChatLog';
import { GamePanel } from './GamePanel';
import { useShowCommands } from './useShowCommands';

export function HistoryPanel({
  gameId,
  game,
  viewerName,
}: {
  gameId: string;
  game: GameSnapshot;
  viewerName: string | null;
}) {
  const [turn, setTurn] = useState(game.turns[game.turns.length - 1] ?? '');
  const [fetchedLines, setFetchedLines] = useState<ChatData[]>([]);
  const [fetchedErrors, setFetchedErrors] = useState<CommandError[]>([]);
  const [showCommands, toggleCommands] = useShowCommands();
  const judgeCommands = game.judge && showCommands;

  // The current turn's lines already ride on the game snapshot (see
  // GameSnapshotFactory); only older turns need a fetch.
  const isCurrentTurn = turn === game.turnLabel;

  useEffect(() => {
    if (!turn || isCurrentTurn) return;
    api
      .get<ChatData[]>(`/game/${gameId}/history?turn=${encodeURIComponent(turn)}`)
      .then(setFetchedLines)
      .catch((err) => console.error('Failed to load turn history', err));
  }, [gameId, turn, isCurrentTurn]);

  useEffect(() => {
    if (!judgeCommands || !turn || isCurrentTurn) {
      setFetchedErrors([]);
      return;
    }
    api
      .get<CommandError[]>(`/game/${gameId}/command-errors?turn=${encodeURIComponent(turn)}`)
      .then(setFetchedErrors)
      .catch(() => setFetchedErrors([]));
  }, [judgeCommands, gameId, turn, isCurrentTurn]);

  const lines = isCurrentTurn ? game.chat : fetchedLines;
  const errors = isCurrentTurn ? (judgeCommands ? game.commandErrors : []) : fetchedErrors;

  return (
    <GamePanel
      id="historyCard"
      className="flex-1 min-h-0"
      bodyClassName="flex flex-col p-2 overflow-hidden"
      title="History"
      headerExtra={
        game.judge && (
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
        )
      }
    >
      <Select
        id="historySelect"
        srLabel="History turn"
        size="sm"
        className="mb-1"
        value={turn}
        onChange={(e) => setTurn(e.target.value)}
      >
        {game.turns.map((t) => (
          <option key={t} value={t}>
            {t}
          </option>
        ))}
      </Select>
      <GameChatLog lines={lines} viewerName={viewerName} showCommands={judgeCommands} errors={errors} seating={game.seating} />
    </GamePanel>
  );
}
