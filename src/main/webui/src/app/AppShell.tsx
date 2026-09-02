import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useQueryInvalidation } from '../ws/useQueryInvalidation';
import { TopBar } from '../components/TopBar';
import { MainPage } from '../pages/MainPage';
import { HELP_SECTIONS } from '../content/help/meta';
import { pathForHelp, ROUTE_PATHS } from '../routes';
import { ReconnectingOverlay } from '../components/ReconnectingOverlay';
import { PageLoading } from '../components/PageLoading';
import { useConnectivity } from '../api/useConnectivity';

// Route-level code splitting: MainPage is eager (it's what every session
// lands on right after auth); everything else is a separate chunk fetched on
// first navigation there. ChunkErrorBoundary (wrapping this shell in
// AppRoutes) already catches a stale tab whose chunk no longer exists after
// a deploy — see its own comment — so these lazy chunks share that backstop
// rather than needing one of their own.
const ProfilePage = lazy(() => import('../pages/ProfilePage').then((m) => ({ default: m.ProfilePage })));
const AdminPage = lazy(() => import('../pages/AdminPage').then((m) => ({ default: m.AdminPage })));
const TournamentAdminPage = lazy(() => import('../pages/TournamentAdminPage').then((m) => ({ default: m.TournamentAdminPage })));
const TournamentPage = lazy(() => import('../pages/TournamentPage').then((m) => ({ default: m.TournamentPage })));
const JudgePage = lazy(() => import('../pages/JudgePage').then((m) => ({ default: m.JudgePage })));
const WatchPage = lazy(() => import('../pages/WatchPage').then((m) => ({ default: m.WatchPage })));
const LobbyPage = lazy(() => import('../pages/LobbyPage').then((m) => ({ default: m.LobbyPage })));
const DeckPage = lazy(() => import('../pages/DeckPage').then((m) => ({ default: m.DeckPage })));
const GamePage = lazy(() => import('../pages/GamePage').then((m) => ({ default: m.GamePage })));
const HelpPage = lazy(() => import('../pages/HelpPage').then((m) => ({ default: m.HelpPage })));
const HelpSection = lazy(() => import('../pages/help/HelpSection').then((m) => ({ default: m.HelpSection })));

/**
 * The authenticated app shell: nav bar plus the routed page area. TopBar's
 * /nav fetch (useNav) requires a valid session (SecurityFilter), so this
 * must never mount for a logged-out visitor — AuthGate is what enforces
 * that, holding this back until /nav has confirmed a session. The /jol/login
 * route is kept entirely outside this shell (see AppRoutes) so it can render
 * before any session exists.
 */
export function AppShell() {
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
      <div id="content" className="flex-1 flex flex-col relative min-h-0">
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
            {ROUTE_PATHS.main.map((path) => (
              <Route key={path} path={path} element={<MainPage />} />
            ))}
            <Route path={ROUTE_PATHS.profile} element={<ProfilePage />} />
            <Route path={ROUTE_PATHS.admin} element={<AdminPage />} />
            <Route path={ROUTE_PATHS.tournamentAdmin} element={<TournamentAdminPage />} />
            <Route path={ROUTE_PATHS.tournament} element={<TournamentPage />} />
            <Route path={ROUTE_PATHS.judge} element={<JudgePage />} />
            <Route path={ROUTE_PATHS.watch} element={<WatchPage />} />
            <Route path={ROUTE_PATHS.lobby} element={<LobbyPage />} />
            <Route path={ROUTE_PATHS.deck} element={<DeckPage />} />
            <Route path={ROUTE_PATHS.game} element={<GamePage />} />
            <Route path={ROUTE_PATHS.help} element={<HelpPage />}>
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
