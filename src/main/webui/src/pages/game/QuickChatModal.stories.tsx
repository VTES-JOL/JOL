import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { QuickChatModal } from './QuickChatModal';

// Canned combat / vote call-out buttons ("Block?", "No press", "H1"…) that
// post the message to the table chat.
const meta = {
  title: 'Game/Modals/QuickChatModal',
  component: QuickChatModal,
  parameters: { layout: 'fullscreen' },
  args: { onSend: fn(), onClose: fn() },
} satisfies Meta<typeof QuickChatModal>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
