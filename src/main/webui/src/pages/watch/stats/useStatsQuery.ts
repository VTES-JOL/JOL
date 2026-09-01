import { useQuery } from '@tanstack/react-query';
import { api } from '../../../api/client';

export interface StatsFilters {
  fromDate: string;
  toDate: string;
  isTourney: boolean;
  /** "Number of Games" cutoff — only some endpoints honour it. */
  threshold?: number;
}

/**
 * The stats-fetch every tab under watch/stats/ was hand-rolling with
 * `useEffect` + `useState` + `runRequest`: a POST to `/stats/…` carrying the
 * shared date/tournament filter, keyed so a filter change refetches. Now a
 * plain react-query query — the queryClient's global onError covers the
 * failure toast. Note the backend's field is misspelled `threshold`.
 */
export function useStatsQuery<T>(endpoint: string, { fromDate, toDate, isTourney, threshold = 0 }: StatsFilters) {
  return useQuery({
    queryKey: ['stats', endpoint, fromDate, toDate, isTourney, threshold],
    queryFn: () => api.post<T>(endpoint, { threshold: threshold, fromDate, toDate, isTourney }),
  });
}
