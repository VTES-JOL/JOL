import type { Meta, StoryObj } from '@storybook/react-vite';
import { NotesPanel } from './NotesPanel';
import { gameSnapshot } from './__fixtures__/gameFixtures';

// The two notes textareas (global — everyone; private — only you). Saves on
// blur; the PUT will not resolve in isolation.
const meta = {
  title: 'Game/Panels/NotesPanel',
  component: NotesPanel,
  parameters: { layout: 'padded' },
  decorators: [
    (Story) => (
      <div className="flex h-[420px] w-[420px] max-w-full flex-col rounded border border-line-accent bg-surface">
        <Story />
      </div>
    ),
  ],
  args: { gameId: 'g1' },
} satisfies Meta<typeof NotesPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const SeatedPlayer: Story = {
  args: { game: gameSnapshot() },
};

export const Spectator: Story = {
  name: 'Spectator (global read-only, no private)',
  args: { game: gameSnapshot({ player: false, judge: false, privateNotes: null }) },
};

export const Empty: Story = {
  args: { game: gameSnapshot({ globalNotes: '', privateNotes: '' }) },
};
