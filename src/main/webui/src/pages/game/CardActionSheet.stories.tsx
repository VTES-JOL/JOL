import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { CardActionSheet } from './CardActionSheet';
import { minion, card, tableCtx } from './__fixtures__/gameFixtures';

// The <md card-action surface — the touch replacement for CardContextMenu and
// the card inspector on touch. Opens on identity + image + counters; the action
// list and editors are behind "Actions". Renders as a bottom sheet (portal).
// The card image will not resolve in isolation.
const meta = {
  title: 'Game/Menus/CardActionSheet',
  component: CardActionSheet,
  parameters: { layout: 'fullscreen' },
  args: {
    viewerName: 'Player1',
    onSubmit: fn(),
    onCounterBump: fn(),
    onRequestTarget: fn(),
    onClose: fn(),
  },
} satisfies Meta<typeof CardActionSheet>;

export default meta;
type Story = StoryObj<typeof meta>;

export const OwnReadyMinion: Story = {
  args: { ctx: tableCtx(minion('Lucita, Anarch Sympathizer', { counters: 5 })), phase: 'Minion' },
};

export const OpponentMinion: Story = {
  args: {
    ctx: tableCtx(minion('Anson', { counters: 3, owner: 'Player2' }), { controller: 'Player2', controlledByViewer: false }),
    phase: 'Minion',
  },
};

export const AshHeapCard: Story = {
  args: {
    ctx: tableCtx(card('Govern the Unaligned'), { regionType: 'ASH_HEAP', regionCommandKey: 'ashheap' }),
    phase: 'Master',
  },
};
