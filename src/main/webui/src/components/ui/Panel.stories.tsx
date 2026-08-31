import type { Meta, StoryObj } from '@storybook/react-vite';
import { Panel } from './Panel';
import { Button } from './Button';

const meta = {
  title: 'UI/Panel',
  component: Panel,
  parameters: { layout: 'fullscreen' },
  decorators: [
    (Story) => (
      <div className="h-[400px] p-4 bg-base">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof Panel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    title: 'Decks',
    children: <div className="p-4 text-sm text-ink-secondary">Panel body content.</div>,
  },
};

export const WithRightSlotAndFooter: Story = {
  args: {
    title: 'Editor',
    right: (
      <Button variant="accent-ghost" size="sm">
        + New
      </Button>
    ),
    children: <div className="p-4 text-sm text-ink-secondary overflow-y-auto">Scrollable body.</div>,
    footer: <div className="px-4 py-2 text-xs text-ink-muted">Footer</div>,
  },
};

export const Compact: Story = {
  args: {
    size: 'compact',
    title: 'Analytics',
    children: <div className="p-4 text-sm text-ink-secondary">Compact header.</div>,
  },
};
