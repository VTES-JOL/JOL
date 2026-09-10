import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { CardAttrEditor } from './CardAttrEditor';
import { minion } from './__fixtures__/gameFixtures';

// The clan / path / sect select trio, shown in a collapsible row inside the
// card menu / sheet for a minion on your own ready region.
const meta = {
  title: 'Game/Menus/CardAttrEditor',
  component: CardAttrEditor,
  parameters: { layout: 'centered' },
  decorators: [(Story) => <div className="w-[340px] max-w-full rounded border border-line-accent bg-surface p-3"><Story /></div>],
  args: { onChange: fn() },
} satisfies Meta<typeof CardAttrEditor>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Toreador: Story = {
  args: { card: minion('Lucita, Anarch Sympathizer', { clan: 'Lasombra', sect: 'Anarch', path: undefined }) },
};

export const WithPath: Story = {
  args: { card: minion('Nakhthorheb', { clan: 'Follower of Set', sect: 'Independent', path: 'Death and the Soul' }) },
};

export const Unset: Story = {
  args: { card: minion('Anarch Convert', { clan: undefined, sect: undefined, path: undefined }) },
};
