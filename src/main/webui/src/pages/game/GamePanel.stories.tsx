import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { History } from 'lucide-react';
import { GamePanel } from './GamePanel';

// Shared chrome for the toggling side panels (History / Notes / Game Chat /
// Deck): a framed card with a title, optional header extras, an optional
// panel-swap pill, and a body.
const meta = {
  title: 'Game/Panels/GamePanel',
  component: GamePanel,
  parameters: { layout: 'padded' },
  decorators: [(Story) => <div className="flex h-[320px] w-[420px] max-w-full flex-col"><Story /></div>],
} satisfies Meta<typeof GamePanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Basic: Story = {
  args: {
    title: 'Notes',
    children: <p className="p-3 text-sm text-ink-secondary">Panel body content.</p>,
  },
};

export const WithToggleAndExtra: Story = {
  args: {
    title: 'Game Chat',
    headerExtra: <span className="rounded-full border border-line px-2 py-0.5 text-xs text-ink-muted">All · Talk</span>,
    toggle: { icon: <History size={12} />, label: 'History', onClick: fn() },
    children: <p className="p-3 text-sm text-ink-secondary">Chat log goes here.</p>,
  },
};
