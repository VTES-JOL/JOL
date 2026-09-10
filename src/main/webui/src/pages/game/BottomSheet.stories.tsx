import type { Meta, StoryObj } from '@storybook/react-vite';
import { useState } from 'react';
import { fn } from 'storybook/test';
import { BottomSheet } from './BottomSheet';
import { Button } from '../../components/ui/Button';

// Shared <md sheet chrome — backdrop, rounded top panel, drag-handle pill,
// swipe-down-to-dismiss. Stays mounted when closed (toggled with `hidden`) so a
// child's scroll position survives a close/reopen. Portals to document.body.
const meta = {
  title: 'Game/Mobile/BottomSheet',
  component: BottomSheet,
  parameters: { layout: 'fullscreen' },
  args: { onClose: fn() },
} satisfies Meta<typeof BottomSheet>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Open: Story = {
  args: {
    open: true,
    label: 'Your hand',
    header: <span className="font-semibold">Your hand</span>,
    children: <div className="p-4 text-sm text-ink-secondary">Sheet body content.</div>,
  },
};

export const NoHeader: Story = {
  args: {
    open: true,
    label: 'Table talk',
    children: <div className="p-4 text-sm text-ink-secondary">A sheet with only the drag handle.</div>,
  },
};

function ToggleableDemo() {
  const [open, setOpen] = useState(false);
  return (
    <div className="p-4">
      <Button size="sm" onClick={() => setOpen(true)}>Open sheet</Button>
      <BottomSheet open={open} onClose={() => setOpen(false)} header={<span className="font-semibold">Act</span>}>
        <div className="p-4 text-sm text-ink-secondary">Body — press Escape, swipe the handle down, or tap the backdrop to close.</div>
      </BottomSheet>
    </div>
  );
}

export const Toggleable: Story = {
  args: { open: false, children: null },
  render: () => <ToggleableDemo />,
};
