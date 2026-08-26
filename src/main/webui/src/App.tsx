import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { lazy, Suspense, useEffect, type ReactNode } from 'react';
import { queryClient } from './api/queryClient';
import { useQueryInvalidation } from './ws/useQueryInvalidation';
import { TopBar } from './components/TopBar';
import { MainPage } from './pages/MainPage';
import { LoginPage } from './pages/LoginPage';
import { HELP_SECTIONS } from './content/help/meta';
import { pathForHelp } from './routes';
import { ReconnectingOverlay } from './components/ReconnectingOverlay';
import { DialogHost } from './components/DialogHost';
import { ToastHost } from './components/ToastHost';
import { UpdateBanner } from './components/UpdateBanner';
import { ChunkErrorBoundary } from './components/ChunkErrorBoundary';
import { PageLoading } from './components/PageLoading';
import { useConnectivity } from './api/useConnectivity';
import { startUpdateCheck } from './updateCheck';
import { useNavAuthState } from './nav/useNav';

// Route-level code splitting: MainPage/LoginPage are eager (the former is
// what every session lands on right after auth; the latter renders before
// any session exists at all), everything else is a separate chunk fetched
// on first navigation there. ChunkErrorBoundary (wrapping <Routes> below)
// already exists to catch a stale tab whose chunk no longer exists after a
// deploy — see its own comment — so these lazy chunks share that same
// backstop rather than needing one of their own.
const ProfilePage = lazy(() => import('./pages/ProfilePage').then((m) => ({ default: m.ProfilePage })));
const AdminPage = lazy(() => import('./pages/AdminPage').then((m) => ({ default: m.AdminPage })));
const TournamentAdminPage = lazy(() => import('./pages/TournamentAdminPage').then((m) => ({ default: m.TournamentAdminPage })));
const TournamentPage = lazy(() => import('./pages/TournamentPage').then((m) => ({ default: m.TournamentPage })));
const WatchPage = lazy(() => import('./pages/WatchPage').then((m) => ({ default: m.WatchPage })));
const LobbyPage = lazy(() => import('./pages/LobbyPage').then((m) => ({ default: m.LobbyPage })));
const DeckPage = lazy(() => import('./pages/DeckPage').then((m) => ({ default: m.DeckPage })));
const GamePage = lazy(() => import('./pages/GamePage').then((m) => ({ default: m.GamePage })));
const HelpPage = lazy(() => import('./pages/help/HelpPage').then((m) => ({ default: m.HelpPage })));
const HelpSection = lazy(() => import('./pages/help/HelpSection').then((m) => ({ default: m.HelpSection })));

// The authenticated app shell: TopBar's /nav fetch (useNav) requires a valid
// session (SecurityFilter), so this must never mount for a logged-out
// visitor — see the /jol/login route below, kept outside TopBar entirely so
// it can render before any session exists. AuthGate (below) is what
// enforces that: it only renders this once /nav has actually confirmed a
// session, rather than trusting the server to have gated the page already.
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
        <Suspense fallback={<PageLoading />}>
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
        </Suspense>
        {!online && <ReconnectingOverlay everConnected={everConnected} />}
      </div>
    </>
  );
}

// No servlet gates auth server-side before index.html gets served — Tomcat's
// RewriteValve (see rewrite.config) serves it unconditionally for every
// route below /jol/login owns — so this is the actual gate: it holds off
// mounting AuthenticatedApp — and therefore TopBar and every page's own API
// calls — until /nav has confirmed a session. On a failed check it renders
// nothing rather than redirecting itself: the failed /nav fetch already
// triggered client.ts's global 401 handler (window.location.href =
// '/jol/login'), so a second, competing redirect here would be redundant.
function AuthGate({ children }: { children: ReactNode }) {
  const { status } = useNavAuthState();
  if (status === 'loading') return <PageLoading />;
  if (status === 'unauthenticated') return null;
  return <>{children}</>;
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
            <Route
              path="/*"
              element={
                <AuthGate>
                  <AuthenticatedApp />
                </AuthGate>
              }
            />
          </Routes>
        </ChunkErrorBoundary>
        <DialogHost />
        <ToastHost />
        <UpdateBanner />
      </BrowserRouter>
    </QueryClientProvider>
  );
}
