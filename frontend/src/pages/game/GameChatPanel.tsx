import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { ChatData, GameSnapshot } from '../../api/types';
import { GameChatLog } from './GameChatLog';

export function GameChatPanel({
  gameId,
  game,
  viewerName,
  onToggleHistory,
}: {
  gameId: string;
  game: GameSnapshot;
  viewerName: string | null;
  onToggleHistory: () => void;
}) {
  const [lines, setLines] = useState<ChatData[]>([]);

  useEffect(() => {
    api
      .get<ChatData[]>(`/game/${gameId}/history?turn=${encodeURIComponent(game.turnLabel)}`)
      .then(setLines)
      .catch((err) => console.error('Failed to load game chat', err));
    // Refetch on every fresh snapshot (game.stamp), not just turn changes —
    // matches legacy pulling forward new lines on every refresh.
  }, [gameId, game.turnLabel, game.stamp]);

  return (
    <div className="card shadow chat" id="gameChatCard">
      <div className="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>Game Chat</span>
        <span>
          <span className="px-2">{`${game.turnLabel} - ${game.phase}`}</span>
          <button className="border-0 shadow rounded-pill bg-light" onClick={onToggleHistory}>
            <i className="bi bi-clock-history me-2" />
            History
          </button>
        </span>
      </div>
      <div className="card-body p-0 game-chat">
        <GameChatLog lines={lines} viewerName={viewerName} />
      </div>
    </div>
  );
}
