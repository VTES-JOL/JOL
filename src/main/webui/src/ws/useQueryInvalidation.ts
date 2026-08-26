import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { subscribe } from './socket';

/**
 * Bridges the WS "invalidate" message (see WebSocketRegistry.notifyInvalidate)
 * straight into TanStack Query's cache — no per-scope translation table,
 * since the backend already sends the query key itself. Mount once near the
 * app root; every TanStack Query consumer benefits without needing its own
 * subscribe() call.
 */
export function useQueryInvalidation() {
  const queryClient = useQueryClient();

  useEffect(
    () =>
      subscribe('invalidate', (msg) => {
        const key = msg.key;
        if (Array.isArray(key)) {
          queryClient.invalidateQueries({ queryKey: key });
        }
      }),
    [queryClient],
  );
}
