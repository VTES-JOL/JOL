import { useEffect, useState } from 'react';
import { Clock } from 'lucide-react';
import { api } from '../../api/client';
import type { ChatData, GameSnapshot } from '../../api/types';
import { Select } from '../../components/ui/Select';
import { GameChatLog } from './GameChatLog';
import { GamePanel } from './GamePanel';

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

  useEffect(() => {
    if (!turn) return;
    api
      .get<ChatData[]>(`/game/${gameId}/history?turn=${encodeURIComponent(turn)}`)
      .then(setLines)
      .catch((err) => console.error('Failed to load turn history', err));
  }, [gameId, turn]);

  return (
    <GamePanel
      id="historyCard"
      bodyClassName="flex flex-col p-2 overflow-hidden"
      title="History"
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
      <GameChatLog lines={lines} viewerName={viewerName} />
    </GamePanel>
  );
}
