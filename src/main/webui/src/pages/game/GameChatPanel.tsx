import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { ChatData, GameSnapshot } from '../../api/types';
import { GameChatLog } from './GameChatLog';
import { GamePanel } from './GamePanel';

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
    <GamePanel
      id="gameChatCard"
      className="chat"
      bodyClassName="p-0 game-chat overflow-hidden"
      title="Game Chat"
      headerExtra={<span className="px-2">{`${game.turnLabel} - ${game.phase}`}</span>}
      toggle={{ icon: 'bi-clock-history', label: 'History', onClick: onToggleHistory }}
    >
      <GameChatLog lines={lines} viewerName={viewerName} />
    </GamePanel>
  );
}
