import { useState } from 'react';

export type SaveState = 'idle' | 'saving' | 'saved' | 'error';

/**
 * One save-feedback pattern for every profile card: `run(promise)` drives a
 * `saving → saved → idle` (auto-clearing) or `saving → error` state, surfacing
 * the rejection's message (the REST resources return a plain-text reason) or a
 * caller-supplied fallback. Replaces the three ad-hoc `status`/`message`/toast
 * schemes the cards each had. Pair with <SaveNote/>.
 */
export function useSave() {
  const [state, setState] = useState<SaveState>('idle');
  const [error, setError] = useState<string | null>(null);

  const run = (promise: Promise<unknown>, opts?: { fallbackError?: string }): Promise<void> => {
    setState('saving');
    setError(null);
    return promise.then(
      () => {
        setState('saved');
        window.setTimeout(() => setState((s) => (s === 'saved' ? 'idle' : s)), 2500);
      },
      (e: unknown) => {
        setState('error');
        const msg = e instanceof Error && e.message ? e.message : undefined;
        setError(msg || opts?.fallbackError || 'Something went wrong.');
      },
    );
  };

  const reset = () => {
    setState('idle');
    setError(null);
  };

  return { state, error, run, reset };
}
