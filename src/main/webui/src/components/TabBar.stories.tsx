import type { Meta, StoryObj } from '@storybook/react-vite';
import { useState } from 'react';
import { expect, userEvent, within } from 'storybook/test';
import { TabBar, type TabDef } from './TabBar';

// Rendered through a stateful harness (tracks the active tab), so stories
// carry no args of their own.
const meta: Meta = {
  title: 'Components/TabBar',
  component: TabBar as never,
  parameters: { layout: 'padded' },
};

export default meta;
type Story = StoryObj;

const TABS: TabDef<'active' | 'past' | 'stats'>[] = [
  { id: 'active', label: 'Active Games' },
  { id: 'past', label: 'Past Games' },
  { id: 'stats', label: 'Statistics' },
];

function Harness({ withBadges }: { withBadges?: boolean }) {
  const [active, setActive] = useState<'active' | 'past' | 'stats'>('active');
  const tabs = withBadges ? TABS.map((t, i) => ({ ...t, badge: i * 3 })) : TABS;
  return (
    <>
      <TabBar tabs={tabs} active={active} onChange={setActive} />
      <p className="mt-2">
        Selected: <strong>{active}</strong>
      </p>
    </>
  );
}

export const Default: Story = {
  render: () => <Harness />,
};

export const WithBadges: Story = {
  render: () => <Harness withBadges />,
};

export const SwitchesOnClick: Story = {
  render: () => <Harness />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('tab', { name: 'Statistics' }));
    await expect(canvas.getByText('stats')).toBeInTheDocument();
    await expect(canvas.getByRole('tab', { name: 'Statistics' })).toHaveClass('active');
  },
};
