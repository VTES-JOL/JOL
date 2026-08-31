import { useCallback } from 'react';
import { useQueryClient, type QueryKey } from '@tanstack/react-query';

/**
 * `() => queryClient.invalidateQueries({ queryKey })` as a callback — the
 * `const refresh = …` line nearly every page that owns a list query repeats
 * after a mutation. Pass the key, get back a zero-arg refresher. Stable when
 * the caller's `queryKey` is stable (a module const or a memoised array).
 */
export function useInvalidate(queryKey: QueryKey): () => void {
  const queryClient = useQueryClient();
  return useCallback(() => {
    void queryClient.invalidateQueries({ queryKey });
  }, [queryClient, queryKey]);
}
