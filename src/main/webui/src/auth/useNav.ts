import { useEffect } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { NavBean } from '../api/types';

const NAV_QUERY_KEY = ['nav'];

// Shared query definition backing both useNav() and useNavAuthState() below —
// TanStack Query's cache dedups this same queryKey across every simultaneous
// caller (these two hooks, GlobalChat's mention detection), so there's only
// ever one /nav fetch in flight regardless of how many components read it.
// retry is disabled at the query-key level (see queryClient.ts) so a
// logged-out visitor's AuthGate check fails fast instead of sitting through
// TanStack Query's default retry/backoff before it can redirect to /login.
function useNavQuery() {
  return useQuery({ queryKey: NAV_QUERY_KEY, queryFn: () => api.get<NavBean>('/nav') });
}

/**
 * Query-cache-backed replacement for the old NavContext/NavProvider — kept
 * as a hook (not a raw useQuery call at each site) so callers don't need to
 * know the query key.
 */
export function useNav(): NavBean | null {
  const { data } = useNavQuery();
  return data ?? null;
}

/**
 * Full auth-check state for AuthGate (see App.tsx) — distinguishes "still
 * waiting on the first /nav response" from "confirmed logged out", which a
 * plain NavBean | null can't (both collapse to null). AuthGate itself
 * doesn't need to redirect on 'unauthenticated': the failed /nav fetch
 * already triggered client.ts's global 401 handler.
 */
export function useNavAuthState(): { status: 'loading' | 'authenticated' | 'unauthenticated' } {
  const { data, isLoading, isError } = useNavQuery();

  // Safety net: a 401 already redirects to /login from client.ts, but any
  // other /nav failure (e.g. a 5xx) would otherwise leave AuthGate rendering
  // nothing — a blank page the user can't get out of. Treat any hard error on
  // the auth check as "send them to login" so they can re-authenticate.
  useEffect(() => {
    if (isError && !window.location.pathname.startsWith('/jol/login')) {
      window.location.href = '/jol/login';
    }
  }, [isError]);

  if (isError) return { status: 'unauthenticated' };
  if (data) return { status: 'authenticated' };
  return { status: isLoading ? 'loading' : 'unauthenticated' };
}

// For pages that mutate something /nav reflects (e.g. profile's country,
// which the TopBar shows as a flag) and want it to update immediately
// rather than waiting for the next 'invalidate' WS push (see
// ws/useQueryInvalidation.ts and WebSocketRegistry.notifyInvalidate).
export function useNavRefresh(): () => void {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: NAV_QUERY_KEY });
}
