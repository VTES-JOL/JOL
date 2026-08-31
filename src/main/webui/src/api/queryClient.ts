import { QueryCache, QueryClient } from '@tanstack/react-query';
import { showError } from '../stores/toast';

// Prototype-scoped: only LobbyPage uses TanStack Query so far (see
// ws/useQueryInvalidation.ts and LobbyResource.getLobbyAndInvalidate on the
// backend). A single QueryCache-level onError replaces each page's own
// per-fetch .catch(showError(...)) — every query gets the same fallback
// message rather than a bespoke one per endpoint.
export const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error, query) => {
      // A query can opt out via meta: { silent: true } when it handles its
      // own error UI (e.g. AdminPage distinguishing a 403 "not an admin"
      // from a genuine load failure) — this global handler is a fallback,
      // not the only path.
      if (query.meta?.silent) return;
      console.error('Query failed', error);
      showError('Failed to load data.');
    },
  }),
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
});

// AuthGate (App.tsx) reads this query's error state to decide whether to
// redirect to /login — retrying a 401 (the default: 3 attempts w/ backoff)
// would make every logged-out page load sit for several seconds before the
// gate can redirect, instead of failing fast like the old server-side 302 did.
queryClient.setQueryDefaults(['nav'], { retry: false });
