import type { Meta, StoryObj } from '@storybook/react-vite';
import { useState } from 'react';
import { expect, userEvent, within } from 'storybook/test';
import { Switch } from './Switch';

const meta: Meta = {
  title: 'Components/Switch',
  component: Switch as never,
  parameters: { layout: 'padded' },
};

export default meta;
type Story = StoryObj;

function Harness({ disabled }: { disabled?: boolean }) {
  const [on, setOn] = useState(false);
  return (
    <Switch id="pref" label="Enable image tooltips" checked={on} disabled={disabled} onChange={(e) => setOn(e.target.checked)} />
  );
}

export const Default: Story = { render: () => <Harness /> };
export const Disabled: Story = { render: () => <Harness disabled /> };

export const TogglesOnClick: Story = {
  render: () => <Harness />,
  play: async ({ canvasElement }) => {
    const cb = within(canvasElement).getByRole('switch');
    await expect(cb).not.toBeChecked();
    await userEvent.click(cb);
    await expect(cb).toBeChecked();
  },
};
