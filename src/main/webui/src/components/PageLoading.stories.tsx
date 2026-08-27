import type { Meta, StoryObj } from '@storybook/react-vite';
import { PageLoading } from './PageLoading';

const meta = {
  title: 'Components/PageLoading',
  component: PageLoading,
  parameters: { layout: 'fullscreen' },
  decorators: [
    (Story) => (
      <div style={{ height: 300 }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof PageLoading>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
