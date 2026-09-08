// Per-submit idempotency key (D8). The server dedupes repeated
// `(gameId, player)` submissions carrying the same id inside a bounded TTL
// window (under the GameModel lock) and returns the current snapshot on a
// hit — so a retry / double-fire / StrictMode double-invoke can't replay a
// `burn` / `transfer` / `bleed`. One fresh id per user-intended action.
export function submitHeaders(): Record<string, string> {
  return { 'X-Submit-Id': crypto.randomUUID() };
}
