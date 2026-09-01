import { useCallback, useRef, useState } from 'react';

/**
 * Serializes game-mutating requests one-at-a-time across every source that
 * can issue one for a game — the command form's text/quick-command/quick-chat/
 * end-turn buttons AND card-click actions (play, table actions, target picks)
 * — so a double-click, a double Enter, or a card click landing while a text
 * command is still in flight can't fire two requests for what the player
 * meant as one action. `inFlight` is a ref (checked synchronously) rather
 * than relying on the `submitting` state alone: two calls arriving in the
 * same tick (e.g. a fast double-click) would both still see stale state
 * before React re-renders and disables anything, but the ref check closes
 * that window regardless of render timing.
 */
export function useSubmitGuard() {
  const inFlight = useRef(false);
  const [submitting, setSubmitting] = useState(false);

  // Stable identity so callers can list `guard` in useCallback deps without
  // recreating their handlers every render (keeps the memoized game board from
  // re-rendering on every unrelated snapshot refetch).
  const guard = useCallback(<T>(run: () => Promise<T>): Promise<T | undefined> => {
    if (inFlight.current) return Promise.resolve(undefined);
    inFlight.current = true;
    setSubmitting(true);
    return run().finally(() => {
      inFlight.current = false;
      setSubmitting(false);
    });
  }, []);

  return { submitting, guard };
}
