import type { Meta, StoryObj } from '@storybook/react-vite';
import { HistoryPanel } from './HistoryPanel';
import { gameSnapshot } from './__fixtures__/gameFixtures';

// Browse any turn's chat. The current turn renders from the snapshot with no
// fetch (shown here); picking an older turn in the select fires a request that
// will not resolve in isolation.
const meta = {
  title: 'Game/Chat/HistoryPanel',
  component: HistoryPanel,
  parameters: { layout: 'padded' },
  decorators: [
    (Story) => (
      <div className="flex h-[460px] w-[480px] max-w-full flex-col">
        <Story />
      </div>
    ),
  ],
  args: { gameId: 'g1', viewerName: 'Player1', game: gameSnapshot() },
} satisfies Meta<typeof HistoryPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const CurrentTurn: Story = {};

export const JudgeView: Story = {
  args: { game: gameSnapshot({ judge: true }) },
};
