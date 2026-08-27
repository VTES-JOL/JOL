import type { Meta, StoryObj } from '@storybook/react-vite';
import { SectionLabel } from './SectionLabel';

const meta = {
  title: 'Components/SectionLabel',
  component: SectionLabel,
  parameters: { layout: 'padded' },
} satisfies Meta<typeof SectionLabel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { children: 'Players' },
};
