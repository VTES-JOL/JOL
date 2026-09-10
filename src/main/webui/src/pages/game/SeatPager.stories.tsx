import type { Meta, StoryObj } from '@storybook/react-vite';
import { SeatPager } from './SeatPager';
import { PlayerBoard } from './PlayerBoard';
import { fn } from 'storybook/test';
import { player, selfPlayer } from './__fixtures__/gameFixtures';
import { withBoardDensity } from './__fixtures__/decorators';

// The <md replacement for the opponent grid + dock: one full-width seat at a
// time, scroll-snap, dot row + ‹ › buttons. Page 0 is the viewer's own seat.
const meta = {
  title: 'Game/Board/SeatPager',
  component: SeatPager,
  parameters: { layout: 'fullscreen' },
  decorators: [
    withBoardDensity('tiles', 1),
    (Story) => (
      <div className="mx-auto flex h-[600px] w-[380px] max-w-full flex-col border border-line-accent bg-base">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof SeatPager>;

export default meta;
type Story = StoryObj<typeof meta>;

const seats = [
  selfPlayer('Player1', { active: true }),
  player('Player2', { pool: 12, prey: 'Player3', predator: 'Player1' }),
  player('Player3', { pool: 8, prey: 'Player1', predator: 'Player2' }),
];

export const ThreeSeats: Story = {
  args: {
    seats,
    renderSeat: (seat) => (
      <PlayerBoard
        player={seat}
        gameId="sb-pager"
        edgeColor="#8b1a1a"
        edgeTextColor="white"
        isSeatedPlayer
        viewerName="Player1"
        relation={seat.name === 'Player1' ? null : seat.name === 'Player2' ? 'prey' : 'predator'}
        onTableCardClick={fn()}
        onQuickCommand={fn()}
        onCounterBump={fn()}
        onPlayCardClick={fn()}
      />
    ),
  },
};
