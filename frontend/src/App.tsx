import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from './api/queryClient';
import { useQueryInvalidation } from './ws/useQueryInvalidation';
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
import { LoginPage } from './pages/LoginPage';
import { HelpPage } from './pages/help/HelpPage';
import { HelpSection } from './pages/help/HelpSection';
import { HELP_SECTIONS } from './content/help/meta';
import { pathForHelp } from './routes';
import { ReconnectingOverlay } from './components/ReconnectingOverlay';
import { DialogHost } from './components/DialogHost';
import { ToastHost } from './components/ToastHost';
import { UpdateBanner } from './components/UpdateBanner';
import { ChunkErrorBoundary } from './components/ChunkErrorBoundary';
import { useConnectivity } from './api/useConnectivity';
import { useEffect } from 'react';
import { startUpdateCheck } from './updateCheck';

// The authenticated app shell: TopBar's /nav fetch (useNav) requires a valid
// session (SecurityFilter), so this must never mount for a logged-out
// visitor — see the /jol/login route below, kept outside TopBar entirely so
// it can render before any session exists.
function AuthenticatedApp() {
  const { online, everConnected } = useConnectivity();
  useQueryInvalidation();

  return (
    <>
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
          <Route path={pathForHelp()} element={<HelpPage />}>
            <Route index element={<Navigate to={pathForHelp(HELP_SECTIONS[0].slug)} replace />} />
            <Route path=":section" element={<HelpSection />} />
          </Route>
        </Routes>
        {!online && <ReconnectingOverlay everConnected={everConnected} />}
      </div>
    </>
  );
}

export function App() {
  useEffect(() => {
    startUpdateCheck();
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ChunkErrorBoundary>
          <Routes>
            <Route path="/jol/login" element={<LoginPage />} />
            <Route path="/*" element={<AuthenticatedApp />} />
          </Routes>
        </ChunkErrorBoundary>
        <DialogHost />
        <ToastHost />
        <UpdateBanner />
      </BrowserRouter>
    </QueryClientProvider>
  );
}
