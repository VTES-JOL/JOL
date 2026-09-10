import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { QuickCommandModal } from './QuickCommandModal';

// Canned command buttons (unlock / edge / draw / pool ± …) — each just types
// the same string into the Command field and submits.
const meta = {
  title: 'Game/Modals/QuickCommandModal',
  component: QuickCommandModal,
  parameters: { layout: 'fullscreen' },
  args: { onSend: fn(), onClose: fn() },
} satisfies Meta<typeof QuickCommandModal>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
