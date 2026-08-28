import type { Meta, StoryObj } from '@storybook/react-vite';
import { Panel } from './Panel';
import { Button } from './Button';

const meta = {
  title: 'UI/Panel',
  component: Panel,
  parameters: { layout: 'fullscreen' },
  decorators: [
    (Story) => (
      <div className="jt:h-[400px] jt:p-4 jt:bg-base">
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
    children: <div className="jt:p-4 jt:text-sm jt:text-ink-secondary">Panel body content.</div>,
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
    children: <div className="jt:p-4 jt:text-sm jt:text-ink-secondary jt:overflow-y-auto">Scrollable body.</div>,
    footer: <div className="jt:px-4 jt:py-2 jt:text-xs jt:text-ink-muted">Footer</div>,
  },
};

export const Compact: Story = {
  args: {
    size: 'compact',
    title: 'Analytics',
    children: <div className="jt:p-4 jt:text-sm jt:text-ink-secondary">Compact header.</div>,
  },
};
