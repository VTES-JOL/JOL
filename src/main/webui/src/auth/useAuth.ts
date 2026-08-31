import { useNav } from './useNav';

// The React app never renders AuthenticatedApp for a genuinely logged-out
// visitor — App.tsx's AuthGate holds it back until /nav confirms a session,
// and api/client.ts hard-redirects to /jol/login the moment any request
// comes back 401 (e.g. a token expiring mid-session). This hook isn't a
// gate itself; it's the single, semantically-named place components
// *inside* the gate read "who's currently logged in" from, backed by the
// same shared /nav fetch AuthGate already made — no extra request.
export interface AuthState {
  // False only in the brief window before the initial /nav fetch resolves.
  authenticated: boolean;
  player: string | null;
}

export function useAuth(): AuthState {
  const nav = useNav();
  return { authenticated: nav !== null, player: nav?.player ?? null };
}
