import type { Decorator } from '@storybook/react-vite';
import { BoardDensityContext, type BoardDensity } from '../boardDensity';

// Region / MinionTile / PlayerBoard read layout choices off BoardDensityContext
// (GamePage is the real provider). Stories that render any of those must supply
// one — `density` ('tiles' | 'text') and `cardsPerRow` (1 on a narrow seat, 2
// on a normal column).
export function withBoardDensity(density: BoardDensity = 'tiles', cardsPerRow: 1 | 2 = 2): Decorator {
  return (Story) => (
    <BoardDensityContext.Provider
      value={{ density, override: null, setDensity: () => {}, cardsPerRow }}
    >
      <Story />
    </BoardDensityContext.Provider>
  );
}

// Frame a single-seat component at a realistic opponent-column width so the
// tile grid, truncation and wrapping behave the way they do on the real table.
export const withSeatFrame: Decorator = (Story) => (
  <div className="mx-auto w-[360px] max-w-full p-3">
    <Story />
  </div>
);

// A wider frame for the viewer's own dock board / multi-seat compositions.
export const withDockFrame: Decorator = (Story) => (
  <div className="mx-auto w-[560px] max-w-full p-3">
    <Story />
  </div>
);

// Wrap a tile component (MinionTile / PermanentChip) in the wrapping grid
// Region lays them out in. `cols` matches the seat width being simulated.
export function withTileGrid(cols = 2): Decorator {
  return (Story) => (
    <div className="w-[360px] max-w-full rounded border border-line-accent bg-surface p-2">
      <ol
        className="region list-none grid gap-1.5 p-1.5"
        style={{ gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))` }}
      >
        <Story />
      </ol>
    </div>
  );
}

// Wrap a card row component in the <ol> its parent Region would render it into,
// so list styling (divide-y, list-none) and the <li> markup are valid.
export const withCardList: Decorator = (Story) => (
  <div className="w-[340px] max-w-full rounded border border-line-accent bg-surface p-2">
    <ol className="region list-none divide-y divide-line/40">
      <Story />
    </ol>
  </div>
);
