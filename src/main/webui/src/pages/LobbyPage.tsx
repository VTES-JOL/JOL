import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Gamepad2 } from 'lucide-react';
import { api } from '../api/client';
import type { GameStatusBean } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { GameList } from './lobby/GameList';
import { GameCreateForm } from './lobby/GameCreateForm';
import { GameSettings, GameDeckPanel } from './lobby/GameDetail';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { MasterDetailView, type PanelConfig } from '../components/ui/MasterDetailView';
import { useInvalidate } from '../api/useInvalidate';

type View = { mode: 'create' } | { mode: 'detail'; gameName: string } | null;
type Pane = 'list' | 'detail' | 'deck';

const GAMES_QUERY_KEY = ['lobby', 'games'];

// Three-pane master/detail: the games list, the selected game's settings
// (seats / invites / deck registration), and — once the viewer has a deck
// registered — a read-only preview of it in its own column, mirroring the
// deck editor's list/editor/analytics split. Below `lg` the columns collapse
// to a Games · Details · My Deck tab strip.
//
//  - each widget fetches its own slice (see lobby/*.tsx); registering a deck
//    doesn't force the create form's reference data to refetch, and vice versa
//  - no manual WS subscription — useQueryInvalidation (mounted near the app
//    root) invalidates ['lobby'] on the backend's push
//  - mutations invalidate the games query rather than applying a returned page
export function LobbyPage() {
  const { player } = useAuth();
  const [view, setView] = useState<View>(null);
  // Which pane the collapsed (mobile) layout shows. Ignored at lg+ where all
  // panes render side by side.
  const [pane, setPane] = useState<Pane>('list');

  const { data: games } = useQuery({
    queryKey: GAMES_QUERY_KEY,
    queryFn: () => api.get<GameStatusBean[]>('/lobby/player/games'),
  });

  const refresh = useInvalidate(GAMES_QUERY_KEY);

  if (!games) {
    return (
      <div className="flex flex-1 min-h-0 items-center justify-center bg-base">
        <Spinner />
      </div>
    );
  }

  const selectedGame = view?.mode === 'detail' ? (games.find((g) => g.name === view.gameName) ?? null) : null;
  const hasSelection = view?.mode === 'create' || !!selectedGame;

  // The deck pane only exists once the viewer has actually registered a deck
  // for the selected game — otherwise it's a wide empty column / a dead tab.
  const myDeckName = selectedGame?.registrations.find((r) => r.player === player)?.deckName ?? null;
  const showDeckPane = !!selectedGame && !!myDeckName;

  const openGame = (gameName: string) => {
    setView({ mode: 'detail', gameName });
    setPane('detail');
  };
  const backToList = () => {
    setView(null);
    setPane('list');
  };

  let settings;
  if (view?.mode === 'create') {
    settings = (
      <GameCreateForm
        onCancel={backToList}
        onCreated={(gameName) => {
          refresh();
          openGame(gameName);
        }}
      />
    );
  } else if (selectedGame) {
    // key by name so transient local state (start-error, invite input) resets
    // when the user switches to a different game.
    settings = (
      <GameSettings
        key={selectedGame.name}
        game={selectedGame}
        onClose={backToList}
        onChanged={refresh}
        onDeckRegistered={() => setPane('deck')}
      />
    );
  } else {
    settings = <EmptyState icon={Gamepad2} title="Select a game or create a new one" />;
  }

  const panels: [PanelConfig, PanelConfig, ...PanelConfig[]] = [
    {
      key: 'list',
      label: 'Games',
      content: (
        <GameList
          games={games}
          selectedName={selectedGame?.name ?? null}
          onSelect={(game) => openGame(game.name)}
          onNew={() => {
            setView({ mode: 'create' });
            setPane('detail');
          }}
        />
      ),
    },
    { key: 'detail', label: 'Details', content: settings },
  ];
  if (showDeckPane) {
    panels.push({
      key: 'deck',
      label: 'My Deck',
      content: <GameDeckPanel key={selectedGame.name} game={selectedGame} />,
    });
  }

  // Guard against a stale pane selection (e.g. still 'deck' after the deck
  // pane went away, or 'detail' with nothing selected).
  const activeKey = !hasSelection ? 'list' : pane === 'deck' && !showDeckPane ? 'detail' : pane;

  return (
    <div className="flex flex-col flex-1 min-h-0 p-4 bg-base text-ink">
      <MasterDetailView
        breakpoint="lg"
        columns={
          showDeckPane
            ? '320px minmax(340px, 460px) minmax(340px, 1fr)'
            : '340px minmax(360px, 1fr)'
        }
        activeKey={activeKey}
        onActiveKeyChange={(k) => {
          setPane(k as Pane);
          if (k === 'list') setView(null);
        }}
        panels={panels}
      />
    </div>
  );
}
