/** The literal JOL stores when a registered deck had no name of its own. */
export const NO_DECK_NAME = '-- no deck name --';

/**
 * A deck name fit to display, or `null` when it's missing or the JOL
 * "no name" sentinel. Use everywhere a stored `deckName` is shown to a user.
 */
export function displayDeckName(name: string | null | undefined): string | null {
  const trimmed = name?.trim();
  return trimmed && trimmed !== NO_DECK_NAME ? trimmed : null;
}
