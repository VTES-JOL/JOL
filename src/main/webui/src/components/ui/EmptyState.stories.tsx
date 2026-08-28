import type { Meta, StoryObj } from '@storybook/react-vite';
import { FolderOpen } from 'lucide-react';
import { EmptyState } from './EmptyState';
import { Button } from './Button';

const meta = {
  title: 'UI/EmptyState',
  component: EmptyState,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof EmptyState>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { icon: FolderOpen, title: 'No deck selected', description: 'Choose a deck from the list to start editing.' },
};

export const TitleOnly: Story = {
  args: { title: 'No decks match the current filter.' },
};

export const WithAction: Story = {
  args: {
    icon: FolderOpen,
    title: 'No decks yet.',
    action: (
      <Button variant="accent-ghost" size="sm">
        Create your first deck →
      </Button>
    ),
  },
};
