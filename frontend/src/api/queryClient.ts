import { QueryCache, QueryClient } from '@tanstack/react-query';
import { showError } from '../components/toast';

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
