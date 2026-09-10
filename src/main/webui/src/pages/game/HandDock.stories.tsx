import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { HandDock } from './HandDock';
import { region, handCard } from './__fixtures__/gameFixtures';

// The wide-dock hand column — bordered panel, "Your hand" header + count, over a
// scrolling list-layout HandStrip.
const meta = {
  title: 'Game/Board/HandDock',
  component: HandDock,
  parameters: { layout: 'padded' },
  decorators: [(Story) => <div className="flex h-[460px] w-[320px] max-w-full flex-col"><Story /></div>],
  args: { show: true, onPlayCardClick: fn() },
} satisfies Meta<typeof HandDock>;

export default meta;
type Story = StoryObj<typeof meta>;

const hand = region(
  'HAND',
  [
    handCard('Govern the Unaligned', { typeClass: 'action' }),
    handCard('Deflection', { typeClass: 'reaction' }),
    handCard('Villein', { typeClass: 'master' }),
    handCard('.44 Magnum', { typeClass: 'equipment' }),
    handCard('Immortal Grapple', { typeClass: 'combat' }),
  ],
  { openHand: true },
);

export const Full: Story = { args: { handRegion: hand } };

export const Empty: Story = { args: { handRegion: region('HAND', [], { openHand: true }) } };

export const Spectator: Story = {
  name: 'Spectator (show=false — framed but empty)',
  args: { handRegion: hand, show: false },
};
