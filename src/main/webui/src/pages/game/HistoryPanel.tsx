import { useEffect, useState } from 'react';
import { Clock, Terminal } from 'lucide-react';
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
  onToggleChat,
}: {
  gameId: string;
  game: GameSnapshot;
  viewerName: string | null;
  onToggleChat: () => void;
}) {
  const [turn, setTurn] = useState(game.turns[game.turns.length - 1] ?? '');
  const [lines, setLines] = useState<ChatData[]>([]);
  const [errors, setErrors] = useState<CommandError[]>([]);
  const [showCommands, toggleCommands] = useShowCommands();
  const judgeCommands = game.judge && showCommands;

  useEffect(() => {
    if (!turn) return;
    api
      .get<ChatData[]>(`/game/${gameId}/history?turn=${encodeURIComponent(turn)}`)
      .then(setLines)
      .catch((err) => console.error('Failed to load turn history', err));
  }, [gameId, turn]);

  useEffect(() => {
    if (!judgeCommands || !turn) {
      setErrors([]);
      return;
    }
    api
      .get<CommandError[]>(`/game/${gameId}/command-errors?turn=${encodeURIComponent(turn)}`)
      .then(setErrors)
      .catch(() => setErrors([]));
  }, [judgeCommands, gameId, turn]);

  return (
    <GamePanel
      id="historyCard"
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
              showCommands
                ? 'border-line-accent bg-hover text-ink'
                : 'border-line text-ink-muted hover:bg-hover'
            }`}
          >
            <Terminal size={12} />
            Commands
          </button>
        )
      }
      toggle={{ icon: <Clock size={13} />, label: 'Game Chat', onClick: onToggleChat }}
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
      <GameChatLog lines={lines} viewerName={viewerName} showCommands={judgeCommands} errors={errors} />
    </GamePanel>
  );
}
