import { showError } from '../stores/toast';

/**
 * Runs a fire-and-forget request (mutation or one-off load), logging and
 * toasting a shared failure message on rejection — the
 * `.then(onSuccess).catch(err => { console.error(...); showError(...) })`
 * shape repeated at nearly every api/client.ts call site. `message` is used
 * for both the console log and the toast (with a trailing period appended
 * to the latter), so callers only write the failure description once.
 */
export function runRequest<T>(promise: Promise<T>, message: string, onSuccess?: (result: T) => void): Promise<void> {
  return promise.then(
    (result) => {
      onSuccess?.(result);
    },
    (err) => {
      console.error(message, err);
      showError(`${message}.`);
    },
  );
}
