import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { TopBar } from './components/TopBar';
import { MainPage } from './pages/MainPage';
import { ProfilePage } from './pages/ProfilePage';
import { AdminPage } from './pages/AdminPage';
import { TournamentAdminPage } from './pages/TournamentAdminPage';
import { TournamentPage } from './pages/TournamentPage';
import { WatchPage } from './pages/WatchPage';
import { LobbyPage } from './pages/LobbyPage';
import { DeckPage } from './pages/DeckPage';
import { GamePage } from './pages/GamePage';
import { NavProvider } from './nav/NavContext';
import { ReconnectingOverlay } from './components/ReconnectingOverlay';
import { useConnectivity } from './api/useConnectivity';

export function App() {
  const { online, everConnected } = useConnectivity();

  return (
    <BrowserRouter>
      <NavProvider>
        <TopBar />
        {/*
          minHeight: 0 overrides flex items' default min-height:auto, which
          otherwise refuses to shrink below its content size and breaks the
          "scroll internally instead of growing past the viewport" pattern
          every flex-column child below needs. Bootstrap has no utility class
          for this specific override. position-relative scopes the overlay
          below to this content area rather than the whole viewport.
        */}
        <div id="content" className="flex-grow-1 d-flex flex-column position-relative" style={{ minHeight: 0 }}>
          {/*
            Routes always render, even while offline — MainPage and its
            children stay mounted so their local state (chat scroll position,
            selected games tab, in-progress input) survives a connectivity
            blip instead of being torn down and rebuilt from scratch. The
            overlay below just sits on top while offline; each widget already
            catches its own fetch failures independently, so nothing crashes
            underneath, it just harmlessly keeps retrying on its own triggers.
          */}
          <Routes>
            <Route path="/jol/" element={<MainPage />} />
            <Route path="/jol/main" element={<MainPage />} />
            <Route path="/jol/main.jsp" element={<MainPage />} />
            <Route path="/jol/profile" element={<ProfilePage />} />
            <Route path="/jol/admin" element={<AdminPage />} />
            <Route path="/jol/tournamentAdmin" element={<TournamentAdminPage />} />
            <Route path="/jol/tournament" element={<TournamentPage />} />
            <Route path="/jol/active" element={<WatchPage />} />
            <Route path="/jol/lobby" element={<LobbyPage />} />
            <Route path="/jol/deck" element={<DeckPage />} />
            <Route path="/jol/game/:gameId" element={<GamePage />} />
          </Routes>
          {!online && <ReconnectingOverlay everConnected={everConnected} />}
        </div>
      </NavProvider>
    </BrowserRouter>
  );
}
