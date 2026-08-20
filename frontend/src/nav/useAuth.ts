import { useNav } from './useNav';

// The React app never renders for a genuinely logged-out visitor —
// MainServlet gates auth server-side before /react/index.html is ever
// served, and api/client.ts hard-redirects to /jol/login the moment a
// request comes back 401 (e.g. a token expiring mid-session). This hook
// isn't a gate itself; it's the single, semantically-named place components
// read "who's currently logged in" from, backed by the same shared /nav
// fetch NavProvider already makes — no extra request.
export interface AuthState {
  // False only in the brief window before the initial /nav fetch resolves.
  authenticated: boolean;
  player: string | null;
}

export function useAuth(): AuthState {
  const nav = useNav();
  return { authenticated: nav !== null, player: nav?.player ?? null };
}
