import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { NotesDeckDrawer } from './NotesDeckDrawer';
import { gameSnapshot } from './__fixtures__/gameFixtures';

// The right-edge slide-over: Notes always, a Deck tab for seated players. The
// deck fetch will not resolve in isolation, so the Deck tab shows empty.
const meta = {
  title: 'Game/Panels/NotesDeckDrawer',
  component: NotesDeckDrawer,
  parameters: { layout: 'fullscreen' },
  decorators: [(Story) => <div className="relative h-[560px] w-full overflow-hidden border border-line-accent bg-base"><Story /></div>],
  args: { gameId: 'g1', open: true, onClose: fn() },
} satisfies Meta<typeof NotesDeckDrawer>;

export default meta;
type Story = StoryObj<typeof meta>;

export const SeatedPlayer: Story = {
  args: { game: gameSnapshot() },
};

export const Spectator: Story = {
  name: 'Spectator (Notes only, no Deck tab)',
  args: { game: gameSnapshot({ player: false, judge: true, privateNotes: null }) },
};

export const Closed: Story = {
  args: { game: gameSnapshot(), open: false },
};
