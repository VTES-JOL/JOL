import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { MobileTabBar } from './MobileTabBar';

// The <md thumb-zone tab bar (Table · Hand · Log · Act). In-flow at the foot of
// the table column. Hand carries a count badge; Log a "new" dot.
const meta = {
  title: 'Game/Mobile/MobileTabBar',
  component: MobileTabBar,
  parameters: { layout: 'fullscreen' },
  decorators: [(Story) => <div className="mx-auto w-[380px] max-w-full"><Story /></div>],
  args: {
    active: 'table',
    onSelect: fn(),
    showHand: true,
    showAct: true,
    handCount: 5,
    logUnread: true,
  },
} satisfies Meta<typeof MobileTabBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const TableActive: Story = {};

export const HandActive: Story = { args: { active: 'hand' } };

export const SpectatorNoActNoHand: Story = {
  args: { showHand: false, showAct: false, active: 'log', handCount: 0, logUnread: false },
};
