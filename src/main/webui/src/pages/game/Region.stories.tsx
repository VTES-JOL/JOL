import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { Region } from './Region';
import { region, minion, card, hiddenCard, faceDownBack, gun, retainer } from './__fixtures__/gameFixtures';
import { withBoardDensity, withSeatFrame } from './__fixtures__/decorators';

// One region of a seat's board. READY / TORPOR / UNCONTROLLED render as a
// wrapping tile grid; other regions as card rows. Collapse state is local; a
// region that gains a card auto-expands. Reads layout off BoardDensityContext.
const meta = {
  title: 'Game/Board/Region',
  component: Region,
  decorators: [withSeatFrame, withBoardDensity('tiles', 2)],
  args: {
    defaultCollapsed: false,
    controller: 'Player2',
    controllerPool: 12,
    isOwnRegion: false,
    isSeatedPlayer: true,
    onTableCardClick: fn(),
    onQuickCommand: fn(),
    onCounterBump: fn(),
    onPlayCardClick: fn(),
  },
} satisfies Meta<typeof Region>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Ready: Story = {
  args: {
    region: region('READY', [
      minion('Muaziz, Archon of Ulugh Beg', { counters: 4, locked: true }),
      minion('Antara', { counters: 3, cards: [gun(), retainer()] }),
      card('Information Highway', { typeClass: 'master', minion: false }),
    ]),
  },
};

export const ReadyTextDensity: Story = {
  args: { region: region('READY', [minion('Muaziz, Archon of Ulugh Beg', { counters: 4 }), minion('Antara', { counters: 3 })]) },
  decorators: [withBoardDensity('text', 2)],
};

export const TorporEmpty: Story = {
  name: 'Torpor (empty — quiet label)',
  args: { region: region('TORPOR', []) },
};

export const Torpor: Story = {
  args: { region: region('TORPOR', [minion('Nakhthorheb', { counters: 0 })]) },
};

export const UncontrolledMixed: Story = {
  name: 'Uncontrolled (hidden collapse)',
  args: {
    region: region('UNCONTROLLED', [
      minion('Anvil', { counters: 2 }),
      faceDownBack(),
      ...Array.from({ length: 5 }, () => hiddenCard()),
    ]),
  },
};

export const AshHeapPile: Story = {
  args: {
    region: region('ASH_HEAP', [card('Govern the Unaligned'), card('Villein', { typeClass: 'master' }), card('Deflection', { typeClass: 'reaction' })]),
  },
};

export const OwnDockNarrow: Story = {
  name: 'Own board, 1-up (mobile)',
  args: {
    isOwnRegion: true,
    controller: 'Player1',
    region: region('READY', [minion('Lucita, Anarch Sympathizer', { counters: 5 }), minion('Cristo', { counters: 2 })]),
  },
  decorators: [withBoardDensity('tiles', 1)],
};
