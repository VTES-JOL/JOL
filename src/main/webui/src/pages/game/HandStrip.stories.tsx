import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { HandStrip } from './HandStrip';
import { region, handCard } from './__fixtures__/gameFixtures';

// The viewer's own hand. 'strip' — a sideways-scrolling chip row for the dock
// band; 'list' — a wrapping grid of larger tiles for the mobile hand sheet.
// Colour-banded by card type.
const meta = {
  title: 'Game/HandStrip',
  component: HandStrip,
  parameters: { layout: 'padded' },
  decorators: [
    (Story) => (
      <div className="w-[560px] max-w-full rounded border border-line-accent bg-panel/50 p-2">
        <Story />
      </div>
    ),
  ],
  args: { show: true, onPlayCardClick: fn() },
} satisfies Meta<typeof HandStrip>;

export default meta;
type Story = StoryObj<typeof meta>;

const hand = region('HAND', [
  handCard('Govern the Unaligned', { typeClass: 'action' }),
  handCard('Deflection', { typeClass: 'reaction' }),
  handCard('Villein', { typeClass: 'master' }),
  handCard('.44 Magnum', { typeClass: 'equipment' }),
  handCard('Immortal Grapple', { typeClass: 'combat' }),
  handCard('Kine Resources Contested', { typeClass: 'political' }),
], { openHand: true });

export const Strip: Story = {
  args: { handRegion: hand },
};

export const ListLayout: Story = {
  args: { handRegion: hand, layout: 'list' },
};

export const Empty: Story = {
  args: { handRegion: region('HAND', [], { openHand: true }) },
};

export const Hidden: Story = {
  name: 'Hidden (show=false)',
  args: { handRegion: hand, show: false },
};
