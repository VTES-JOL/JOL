import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { ChatData, GameSnapshot } from '../../api/types';
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
    <GamePanel id="historyCard" bodyClassName="p-2 overflow-hidden" title="History" toggle={{ icon: 'bi-chat', label: 'Game Chat', onClick: onToggleChat }}>
      <label htmlFor="historySelect">History:</label>
      <select id="historySelect" className="form-select form-select-sm mb-1" value={turn} onChange={(e) => setTurn(e.target.value)}>
        {game.turns.map((t) => (
          <option key={t} value={t}>
            {t}
          </option>
        ))}
      </select>
      <GameChatLog lines={lines} viewerName={viewerName} />
    </GamePanel>
  );
}
