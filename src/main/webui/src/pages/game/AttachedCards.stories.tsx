import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { AttachedCards } from './AttachedCards';
import { gun, retainer, bloodStack, faceDownBack, card } from './__fixtures__/gameFixtures';

// A minion's attachment strip. ≤4 → an always-shown chip strip; ≥5 → an
// "N attached ▸" header that collapses, with a "list ▾" swap to the full
// indented NestedCard list.
const meta = {
  title: 'Game/Board/AttachedCards',
  component: AttachedCards,
  decorators: [
    (Story) => (
      <div className="w-[320px] max-w-full rounded-md border border-line-accent bg-hover/40 p-2">
        <span className="card-name text-sm font-medium">Cristo</span>
        <Story />
      </div>
    ),
  ],
  args: { parentCoordinate: '1', region: 'READY', onAction: fn() },
} satisfies Meta<typeof AttachedCards>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Few: Story = {
  args: { cards: [gun(), retainer()] },
};

export const WithCounterAndFaceDown: Story = {
  args: { cards: [gun(), bloodStack(4), faceDownBack()] },
};

export const ManyCollapsed: Story = {
  name: 'Many (≥5, collapsed)',
  args: {
    cards: [
      gun(),
      gun({ name: 'Ivory Bow' }),
      retainer(),
      retainer({ name: 'Vagabond Mystic' }),
      bloodStack(2),
      card('Flak Jacket', { typeClass: 'equipment' }),
    ],
  },
};

export const NestedAttachment: Story = {
  args: {
    cards: [
      retainer({ name: 'Ambrosius, The Ferryman', cards: [card('Homunculus', { typeClass: 'retainer' })] }),
      gun(),
    ],
  },
};
