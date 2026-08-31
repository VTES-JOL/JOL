import type { Meta, StoryObj } from '@storybook/react-vite';
import { SummaryStats } from './SummaryStats';

const meta = {
  title: 'UI/SummaryStats',
  component: SummaryStats,
  parameters: { layout: 'padded' },
} satisfies Meta<typeof SummaryStats>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Valid: Story = {
  args: { summary: { crypt: 12, library: 80, groups: '4/5' }, validate: true },
};

export const InvalidCrypt: Story = {
  args: { summary: { crypt: 8, library: 80, groups: '4/5' }, validate: true },
};

export const InvalidLibraryAndGroups: Story = {
  args: { summary: { crypt: 12, library: 42, groups: '2/5' }, validate: true },
};

export const NoValidation: Story = {
  args: { summary: { crypt: 8, library: 42, groups: '2/5' }, validate: false },
};

export const NoGroups: Story = {
  args: { summary: { crypt: 12, library: 75, groups: null }, validate: true },
};
