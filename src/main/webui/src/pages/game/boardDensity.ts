import { createContext, useCallback, useContext, useEffect, useState } from 'react';

/**
 * "Board density" — how the READY / TORPOR / UNCONTROLLED regions lay their
 * card tiles out:
 *
 *  - `tiles`  (default) — a 2-up grid of compact minion tiles.
 *  - `text`   — one card row per line (CardSimple style), no image dependency.
 *
 * `text` is forced on when {@link useTextMode} is on (image tooltips off, or
 * touch width). Otherwise the player can toggle it, and the choice is
 * remembered per game in `localStorage['jol-density:<gameId>']` — matching the
 * client-owned GameView model (nothing round-trips to the server). A private
 * window / cleared storage just falls back to `tiles`.
 *
 * `cardsPerRow` is the tile grid's column count for the CURRENT container — 2
 * on a normal seat column, 1 when the seat is very narrow (mobile pager, a
 * dragged-small dock). Regions read it off the context so opponents and your
 * own board stay in step.
 */
export type BoardDensity = 'tiles' | 'text';

export interface BoardDensityValue {
  density: BoardDensity;
  /** null while the viewer hasn't toggled — density is following `textMode`. */
  override: BoardDensity | null;
  setDensity: (d: BoardDensity | null) => void;
  cardsPerRow: 1 | 2;
}

export const BoardDensityContext = createContext<BoardDensityValue>({
  density: 'tiles',
  override: null,
  setDensity: () => {},
  cardsPerRow: 2,
});

export function useBoardDensity(): BoardDensityValue {
  return useContext(BoardDensityContext);
}

/**
 * Owns the density state for one game. GamePage calls this once and feeds the
 * result into {@link BoardDensityContext}. `textMode` is the forced-text
 * signal; `narrow` drops the tile grid to a single column.
 */
export function useBoardDensityState(
  gameId: string | undefined,
  textMode: boolean,
  narrow: boolean,
): BoardDensityValue {
  const key = gameId ? `jol-density:${gameId}` : null;

  const [override, setOverride] = useState<BoardDensity | null>(() => {
    if (!key) return null;
    try {
      const raw = localStorage.getItem(key);
      return raw === 'tiles' || raw === 'text' ? raw : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    if (!key) return;
    try {
      if (override) localStorage.setItem(key, override);
      else localStorage.removeItem(key);
    } catch {
      /* storage disabled — the toggle just won't persist */
    }
  }, [key, override]);

  const setDensity = useCallback((d: BoardDensity | null) => setOverride(d), []);

  // The viewer's explicit choice always wins — "images off" seeds text density
  // but doesn't lock it, since dense tiles don't imply hover art. No override:
  // follow textMode (images-off / touch width), else the default.
  const density: BoardDensity = override ?? (textMode ? 'text' : 'tiles');

  return { density, override, setDensity, cardsPerRow: narrow ? 1 : 2 };
}
