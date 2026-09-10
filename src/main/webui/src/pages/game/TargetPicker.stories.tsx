import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { TargetPicker } from './TargetPicker';

// The non-blocking banner shown while a play-card mode with a target is
// pending — the pick completes by clicking an on-table card. Pinned under the
// HUD on desktop, above the tab bar on mobile.
const meta = {
  title: 'Game/Modals/TargetPicker',
  component: TargetPicker,
  parameters: { layout: 'fullscreen' },
  decorators: [(Story) => <div className="relative h-[160px]"><Story /></div>],
  args: { onCancel: fn() },
} satisfies Meta<typeof TargetPicker>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { cardName: '.44 Magnum', prompt: 'Pick a minion you control to equip.' },
};

export const GenericPrompt: Story = {
  args: { cardName: 'Govern the Unaligned' },
};
