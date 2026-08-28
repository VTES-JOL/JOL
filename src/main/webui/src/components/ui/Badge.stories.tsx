import type { Meta, StoryObj } from '@storybook/react-vite';
import { Badge } from './Badge';

const meta = {
  title: 'UI/Badge',
  component: Badge,
  parameters: { layout: 'padded' },
  args: { children: 'Standard' },
} satisfies Meta<typeof Badge>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Format: Story = { args: { variant: 'format' } };
export const Accent: Story = { args: { variant: 'accent', children: 'New' } };
export const Blood: Story = { args: { variant: 'blood', children: 'Invalid' } };
export const Online: Story = { args: { variant: 'online', children: 'Valid' } };
export const Muted: Story = { args: { variant: 'muted', children: 'Draft' } };

export const AllVariants: Story = {
  render: () => (
    <div className="jt:flex jt:flex-wrap jt:items-center jt:gap-2">
      <Badge variant="format">Standard</Badge>
      <Badge variant="accent">New</Badge>
      <Badge variant="blood">Invalid</Badge>
      <Badge variant="online">Valid</Badge>
      <Badge variant="muted">Draft</Badge>
      <Badge variant="accent" size="sm">
        Larger
      </Badge>
    </div>
  ),
};
