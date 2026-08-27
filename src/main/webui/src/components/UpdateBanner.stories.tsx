import type { Meta, StoryObj } from '@storybook/react-vite';
import { UpdateBanner } from './UpdateBanner';

const meta = {
  title: 'Components/UpdateBanner',
  component: UpdateBanner,
  parameters: { layout: 'fullscreen' },
  decorators: [
    (Story) => (
      <div style={{ position: 'relative', height: 120 }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof UpdateBanner>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Hidden: Story = {
  args: { visible: false },
};

export const UpdateAvailable: Story = {
  args: { visible: true },
};
