import { createContext, useContext } from 'react';

/**
 * "Text mode" — the game screen renders cards without any image dependency:
 * no hover-preview tooltip, no card art in the action / play modals. The
 * minion tile and card rows already carry every identity field
 * (name, capacity, disciplines, clan, sect, votes), so nothing is lost.
 *
 * On when EITHER:
 *  - the player turned off "image tooltips" in Preferences
 *    (`NavBean.imageTooltipPreference === false`), or
 *  - the viewport is below `md` (768px) — touch has no hover, so an
 *    image-tooltip affordance is dead weight there regardless of preference.
 *
 * Provided once by GamePage; consumed by useCardTooltips (board + game chat),
 * CardImage, and the minion tiles.
 */
export const TextModeContext = createContext(false);

export function useTextMode(): boolean {
  return useContext(TextModeContext);
}
