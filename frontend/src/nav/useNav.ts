import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { NavBean } from '../api/types';

const NAV_QUERY_KEY = ['nav'];

/**
 * Query-cache-backed replacement for the old NavContext/NavProvider —
 * TanStack Query's cache already dedups this same queryKey across every
 * simultaneous caller (this hook, useAuth, GlobalChat's mention detection),
 * so no Provider is needed to get the single-shared-fetch behavior the old
 * comment on NavProvider described. Kept as a hook (not a raw useQuery call
 * at each site) so callers don't need to know the query key.
 */
export function useNav(): NavBean | null {
  const { data } = useQuery({ queryKey: NAV_QUERY_KEY, queryFn: () => api.get<NavBean>('/nav') });
  return data ?? null;
}

// For pages that mutate something /nav reflects (e.g. profile's country,
// which the TopBar shows as a flag) and want it to update immediately
// rather than waiting for the next 'invalidate' WS push (see
// ws/useQueryInvalidation.ts and WebSocketRegistry.notifyInvalidate).
export function useNavRefresh(): () => void {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: NAV_QUERY_KEY });
}
