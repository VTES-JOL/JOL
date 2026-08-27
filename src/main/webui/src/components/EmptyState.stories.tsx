import type { Meta, StoryObj } from '@storybook/react-vite';
import { EmptyState } from './EmptyState';

const meta = {
  title: 'Components/EmptyState',
  component: EmptyState,
  parameters: { layout: 'padded' },
} satisfies Meta<typeof EmptyState>;

export default meta;
type Story = StoryObj<typeof meta>;

export const NoDeckSelected: Story = {
  args: { icon: 'bi-collection', message: 'Select a deck to preview it.' },
  decorators: [(Story) => <div style={{ height: 240 }}><Story /></div>],
};

export const NoGameSelected: Story = {
  args: { icon: 'bi-controller', message: 'Select a game to view its details.' },
  decorators: [(Story) => <div style={{ height: 240 }}><Story /></div>],
};
