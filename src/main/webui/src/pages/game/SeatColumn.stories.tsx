import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { SeatColumn } from './SeatColumn';
import { player, region, card, hiddenCard } from './__fixtures__/gameFixtures';
import { withBoardDensity, withSeatFrame } from './__fixtures__/decorators';

// One opponent column in the table scroller. A live seat is just the
// PlayerBoard; an ousted seat collapses to a one-line strip (exit kind + VP
// recipient from the persisted per-exit record) that expands on click.
const meta = {
  title: 'Game/Board/SeatColumn',
  component: SeatColumn,
  decorators: [withSeatFrame, withBoardDensity('tiles', 2)],
  args: {
    gameId: 'sb-seat',
    edgeColor: '#8b1a1a',
    edgeTextColor: 'white',
    isSeatedPlayer: true,
    viewerName: 'Player1',
    relation: 'prey',
    onTableCardClick: fn(),
    onQuickCommand: fn(),
    onCounterBump: fn(),
    onPlayCardClick: fn(),
  },
} satisfies Meta<typeof SeatColumn>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Live: Story = {
  args: { player: player('Player2', { pool: 13 }) },
};

export const OustedCollapsed: Story = {
  args: {
    player: player('Player5', {
      pool: 0,
      victoryPoints: 1,
      exitKind: 'OUST',
      exitVpRecipient: 'Player1',
      regions: [region('ASH_HEAP', Array.from({ length: 18 }, () => hiddenCard()))],
    }),
  },
};

export const WithdrewCollapsed: Story = {
  args: {
    player: player('Player4', { pool: 0, victoryPoints: 0, exitKind: 'WITHDRAW', regions: [region('ASH_HEAP', [card('Withdraw')])] }),
  },
};
