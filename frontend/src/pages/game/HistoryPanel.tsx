import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { ChatData, GameSnapshot } from '../../api/types';
import { GameChatLog } from './GameChatLog';

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
    <div className="card shadow" id="historyCard">
      <div className="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>History</span>
        <button className="border-0 shadow rounded-pill bg-light" onClick={onToggleChat}>
          <i className="bi bi-chat me-2" />
          Game Chat
        </button>
      </div>
      <div className="card-body p-2 overflow-hidden">
        <label htmlFor="historySelect">History:</label>
        <select id="historySelect" className="form-select form-select-sm mb-1" value={turn} onChange={(e) => setTurn(e.target.value)}>
          {game.turns.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <GameChatLog lines={lines} viewerName={viewerName} />
      </div>
    </div>
  );
}
