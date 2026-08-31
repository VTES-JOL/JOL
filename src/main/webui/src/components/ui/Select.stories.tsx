import type { Meta, StoryObj } from '@storybook/react-vite';
import { Select } from './Select';

const meta = {
  title: 'Components/Select',
  component: Select,
  parameters: { layout: 'padded' },
  args: {
    label: 'Country',
    id: 'country',
    children: (
      <>
        <option value="">— none —</option>
        <option value="ie">Ireland</option>
        <option value="us">United States</option>
      </>
    ),
  },
} satisfies Meta<typeof Select>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
export const Small: Story = { args: { size: 'sm' } };
export const WithHint: Story = { args: { hint: 'Shown next to your name in the lobby.' } };
export const WithError: Story = { args: { error: 'Pick a country.' } };
