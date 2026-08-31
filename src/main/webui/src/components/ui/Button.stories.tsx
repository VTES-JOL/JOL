import type { Meta, StoryObj } from '@storybook/react-vite';
import { Plus } from 'lucide-react';
import { Button } from './Button';

const meta = {
  title: 'Components/Button',
  component: Button,
  parameters: { layout: 'padded' },
  args: { children: 'Confirm' },
} satisfies Meta<typeof Button>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Primary: Story = { args: { variant: 'primary' } };
export const Secondary: Story = { args: { variant: 'secondary' } };
export const Ghost: Story = { args: { variant: 'ghost' } };
export const AccentGhost: Story = { args: { variant: 'accent-ghost' } };
export const Danger: Story = { args: { variant: 'danger', children: 'Delete' } };
export const Loading: Story = { args: { loading: true } };
export const WithIcon: Story = { args: { icon: <Plus size={14} />, children: 'New game' } };

export const Sizes: Story = {
  render: (args) => (
    <div className="flex items-center gap-2">
      <Button {...args} size="sm">
        Small
      </Button>
      <Button {...args} size="md">
        Medium
      </Button>
      <Button {...args} size="lg">
        Large
      </Button>
    </div>
  ),
};
