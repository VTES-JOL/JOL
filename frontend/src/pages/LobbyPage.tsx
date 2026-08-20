import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { GameStatusBean, LobbyPage as LobbyPageData } from '../api/types';
import { GameList } from './lobby/GameList';
import { GameCreateForm } from './lobby/GameCreateForm';
import { GameDetail } from './lobby/GameDetail';

type View = { mode: 'create' } | { mode: 'detail'; gameName: string } | null;

export function LobbyPage() {
  const [data, setData] = useState<LobbyPageData | null>(null);
  const [view, setView] = useState<View>(null);

  const refresh = () => {
    api
      .get<LobbyPageData>('/lobby/player/games')
      .then(setData)
      .catch((err) => console.error('Failed to load lobby', err));
  };

  useEffect(refresh, []);

  const applyUpdate = (updated: LobbyPageData) => setData(updated);

  const selectGame = (game: GameStatusBean) => setView({ mode: 'detail', gameName: game.name });

  if (!data) return null;

  const selectedGame = view?.mode === 'detail' ? data.games.find((g) => g.name === view.gameName) : null;

  return (
    <div className="row g-2 flex-fill align-items-stretch min-h-0 p-3">
      <div className="col-lg-4 d-flex flex-column">
        <GameList
          games={data.games}
          selectedName={selectedGame?.name ?? null}
          onSelect={selectGame}
          onNew={() => setView({ mode: 'create' })}
        />
      </div>
      <div className="col-lg-8 d-flex flex-column">
        {view?.mode === 'create' && (
          <GameCreateForm
            players={data.players}
            gameFormats={data.gameFormats}
            onCancel={() => setView(null)}
            onCreated={(updated, gameName) => {
              applyUpdate(updated);
              setView({ mode: 'detail', gameName });
            }}
          />
        )}
        {selectedGame && (
          <GameDetail
            game={selectedGame}
            players={data.players}
            decks={data.decks}
            message={data.message}
            onClose={() => setView(null)}
            onChanged={applyUpdate}
          />
        )}
        {!view && (
          <div className="d-flex flex-column flex-fill align-items-center justify-content-center text-muted min-h-0">
            <i className="bi bi-controller fs-1 mb-2" />
            <span>Select a game or create a new one</span>
          </div>
        )}
      </div>
    </div>
  );
}
