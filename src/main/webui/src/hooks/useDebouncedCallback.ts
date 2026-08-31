import { useCallback, useEffect, useRef } from 'react';

/**
 * Debounced wrapper around `fn`, plus `flush()` (run any pending call now) and
 * `cancel()` (drop it). A pending call is also flushed automatically on
 * unmount, so a component that schedules a save and then disappears — a deck
 * switch, a tab change — never silently drops the last edit. This is the
 * timer-in-a-ref + latest-closure-in-a-ref dance that was copy-pasted into
 * DeckComments and DeckEditorPane.
 *
 * `fn` is read through a ref, so the returned `call`/`flush`/`cancel`
 * identities are stable across renders and always invoke the latest closure.
 * Only the most recent arguments are remembered — a burst of calls collapses
 * to one invocation with the last args.
 */
export function useDebouncedCallback<A extends unknown[]>(
  fn: (...args: A) => void,
  delayMs: number,
): { call: (...args: A) => void; flush: () => void; cancel: () => void } {
  const fnRef = useRef(fn);
  useEffect(() => {
    fnRef.current = fn;
  });

  const timerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const pendingRef = useRef<A | null>(null);

  const cancel = useCallback(() => {
    clearTimeout(timerRef.current);
    timerRef.current = undefined;
    pendingRef.current = null;
  }, []);

  const flush = useCallback(() => {
    clearTimeout(timerRef.current);
    timerRef.current = undefined;
    if (pendingRef.current !== null) {
      const args = pendingRef.current;
      pendingRef.current = null;
      fnRef.current(...args);
    }
  }, []);

  const call = useCallback(
    (...args: A) => {
      pendingRef.current = args;
      clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => {
        timerRef.current = undefined;
        const pending = pendingRef.current;
        pendingRef.current = null;
        if (pending !== null) fnRef.current(...pending);
      }, delayMs);
    },
    [delayMs],
  );

  // Flush on unmount only — `flush` is stable, so this runs once.
  useEffect(() => () => flush(), [flush]);

  return { call, flush, cancel };
}
