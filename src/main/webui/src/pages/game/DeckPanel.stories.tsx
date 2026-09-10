import type { Meta, StoryObj } from '@storybook/react-vite';
import { DeckPanel } from './DeckPanel';

// The registered-deck view inside NotesDeckDrawer's Deck tab. Fetches
// GET /game/{id}/deck once on mount — that request will not resolve in
// isolation, so this shows the empty pre-load state.
const meta = {
  title: 'Game/Panels/DeckPanel',
  component: DeckPanel,
  parameters: { layout: 'padded' },
  decorators: [(Story) => <div className="flex h-[420px] w-[340px] max-w-full flex-col rounded border border-line-accent bg-surface"><Story /></div>],
  args: { gameId: 'g1' },
} satisfies Meta<typeof DeckPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const BeforeLoad: Story = {};
