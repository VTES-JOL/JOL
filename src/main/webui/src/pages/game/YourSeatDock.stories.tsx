import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { YourSeatDock } from './YourSeatDock';
import { selfPlayer } from './__fixtures__/gameFixtures';
import { withBoardDensity, withDockFrame } from './__fixtures__/decorators';

// The viewer's own seat, pinned at the foot of the table column above the
// docked hand + command band. Scrolls internally when tall.
const meta = {
  title: 'Game/Board/YourSeatDock',
  component: YourSeatDock,
  decorators: [
    withDockFrame,
    withBoardDensity('tiles', 2),
    (Story) => (
      <div className="flex h-[560px] flex-col rounded-lg border border-line-accent bg-base">
        <Story />
      </div>
    ),
  ],
  args: {
    gameId: 'sb-dock',
    edgeColor: '#8b1a1a',
    edgeTextColor: 'white',
    viewerName: 'Player1',
    onTableCardClick: fn(),
    onQuickCommand: fn(),
    onCounterBump: fn(),
    onPlayCardClick: fn(),
  },
} satisfies Meta<typeof YourSeatDock>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { player: selfPlayer('Player1', { active: true, pool: 22 }) },
};

export const InfluencePriority: Story = {
  args: { player: selfPlayer('Player1', { active: true }), influencePriority: true },
};
