import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { PilesFooter } from './PilesFooter';
import { region, card, hiddenCard } from './__fixtures__/gameFixtures';
import { withBoardDensity, withSeatFrame } from './__fixtures__/decorators';

// The archival-pile count row at the foot of a PlayerBoard — ash heap / RFG /
// library / crypt / hand collapsed into one line of tappable "ABBR n" chips.
// Each chip expands its pile inline (state persisted per game in localStorage).
const meta = {
  title: 'Game/Board/PilesFooter',
  component: PilesFooter,
  decorators: [withSeatFrame, withBoardDensity('tiles', 2)],
  args: {
    gameId: 'sb-piles',
    controller: 'Player2',
    controllerPool: 12,
    isOwnRegion: false,
    isSeatedPlayer: true,
    onTableCardClick: fn(),
    onQuickCommand: fn(),
    onPlayCardClick: fn(),
    regions: [
      region('ASH_HEAP', [card('Govern the Unaligned'), card('Deflection', { typeClass: 'reaction' })]),
      region('REMOVED_FROM_GAME', [hiddenCard()]),
      region('LIBRARY', Array.from({ length: 34 }, () => hiddenCard())),
      region('CRYPT', Array.from({ length: 7 }, () => hiddenCard())),
      region('HAND', Array.from({ length: 5 }, () => hiddenCard())),
    ],
  },
} satisfies Meta<typeof PilesFooter>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Collapsed: Story = {};

export const OwnHandOpen: Story = {
  name: 'Own seat (open hand shown)',
  args: {
    isOwnRegion: true,
    controller: 'Player1',
    regions: [
      region('ASH_HEAP', [card('Villein', { typeClass: 'master' })]),
      region('LIBRARY', Array.from({ length: 28 }, () => hiddenCard())),
      region('HAND', [card('Deflection', { typeClass: 'reaction' }), card('.44 Magnum', { typeClass: 'equipment' })], { openHand: true }),
    ],
  },
};
