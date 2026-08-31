import { Routes, Route } from 'react-router-dom';
import { LoginPage } from '../pages/LoginPage';
import { ChunkErrorBoundary } from '../components/ChunkErrorBoundary';
import { ROUTE_PATHS } from '../routes';
import { AuthGate } from './AuthGate';
import { AppShell } from './AppShell';

// Top-level route split: /jol/login renders on its own, before any session
// exists; everything else goes through AuthGate → AppShell. ChunkErrorBoundary
// wraps both so a stale tab requesting a since-deleted lazy chunk (after a
// deploy) recovers instead of white-screening.
export function AppRoutes() {
  return (
    <ChunkErrorBoundary>
      <Routes>
        <Route path={ROUTE_PATHS.login} element={<LoginPage />} />
        <Route
          path="/*"
          element={
            <AuthGate>
              <AppShell />
            </AuthGate>
          }
        />
      </Routes>
    </ChunkErrorBoundary>
  );
}
