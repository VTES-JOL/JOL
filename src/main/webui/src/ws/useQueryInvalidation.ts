import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { subscribe } from '../stores/socket';

/**
 * Bridges the WS "invalidate" message (see WebSocketRegistry.notifyInvalidate)
 * straight into TanStack Query's cache — no per-scope translation table,
 * since the backend already sends the query key itself. Mount once near the
 * app root; every TanStack Query consumer benefits without needing its own
 * subscribe() call.
 *
 * For a ['game', id] frame the backend also stamps it with the game-state
 * version (D8 — an in-memory monotonic counter, epoch-millis-seeded, bumped on
 * anything a refetch would show). When our cached snapshot is already at or
 * past that stamp we skip the refetch entirely — the common "someone nudged a
 * counter, we already have a newer snapshot from our own POST response" case
 * then costs zero HTTP. A negative frame stamp, a missing / non-numeric stamp
 * on either side, or a non-game key always falls through to a normal
 * invalidate, so a mismatched frontend/backend build just always refetches.
 */
export function useQueryInvalidation() {
  const queryClient = useQueryClient();

  useEffect(
    () =>
      subscribe('invalidate', (msg) => {
        const key = msg.key;
        if (!Array.isArray(key)) return;

        if (key[0] === 'game') {
          const frameStamp = Number(msg.stamp);
          if (Number.isFinite(frameStamp) && frameStamp >= 0) {
            const cached = queryClient.getQueryData<{ stamp?: unknown }>(key);
            const cachedStamp = Number(cached?.stamp);
            if (Number.isFinite(cachedStamp) && cachedStamp >= frameStamp) {
              return; // already hold this version or newer
            }
          }
        }

        queryClient.invalidateQueries({ queryKey: key });
      }),
    [queryClient],
  );
}
