import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { ChatCompose } from './ChatCompose';

// The "Say something to the table…" box at the foot of the chat log, with the
// quick-chat shortcut button.
const meta = {
  title: 'Game/Chat/ChatCompose',
  component: ChatCompose,
  parameters: { layout: 'padded' },
  decorators: [(Story) => <div className="w-[460px] max-w-full rounded border border-line-accent"><Story /></div>],
  args: { onSend: fn() },
} satisfies Meta<typeof ChatCompose>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Disabled: Story = { args: { disabled: true } };
