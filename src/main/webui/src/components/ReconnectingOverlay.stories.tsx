import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, userEvent, within } from 'storybook/test';
import { ReconnectingOverlay } from './ReconnectingOverlay';

const meta = {
  title: 'Components/ReconnectingOverlay',
  component: ReconnectingOverlay,
  parameters: { layout: 'fullscreen' },
  decorators: [
    (Story) => (
      <div style={{ position: 'relative', height: 300 }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof ReconnectingOverlay>;

export default meta;
type Story = StoryObj<typeof meta>;

export const ConnectionLost: Story = {
  args: { everConnected: true },
};

export const NeverConnected: Story = {
  args: { everConnected: false },
};

export const RetryButtonIsClickable: Story = {
  args: { everConnected: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    // checkNow() (see api/connectivity.ts) fires a real fetch and swallows
    // any failure — this just confirms the button is wired up and doesn't
    // throw, not that the app actually reconnects.
    await userEvent.click(canvas.getByRole('button', { name: 'Retry now' }));
    await expect(canvas.getByText('Connection lost')).toBeInTheDocument();
  },
};
