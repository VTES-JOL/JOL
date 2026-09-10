import type { Meta, StoryObj } from '@storybook/react-vite';
import { GameChatPanel } from './GameChatPanel';
import { gameSnapshot } from './__fixtures__/gameFixtures';

// The framed live-chat panel: All / Talk filter, judge-only Commands toggle,
// over GameChatLog fed the current turn's lines off the game snapshot.
const meta = {
  title: 'Game/Chat/GameChatPanel',
  component: GameChatPanel,
  parameters: { layout: 'padded' },
  decorators: [
    (Story) => (
      <div className="flex h-[460px] w-[480px] max-w-full flex-col">
        <Story />
      </div>
    ),
  ],
  args: { gameId: 'g1', viewerName: 'Player1' },
} satisfies Meta<typeof GameChatPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const SeatedPlayer: Story = {
  args: { game: gameSnapshot() },
};

export const JudgeWithCommands: Story = {
  args: {
    game: gameSnapshot({
      judge: true,
      player: false,
      commandErrors: [{ timestamp: '10-Feb 21:03', player: 'Player2', command: 'blok', error: 'Unknown verb' }],
    }),
  },
};
