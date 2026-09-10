import type { Meta, StoryObj } from '@storybook/react-vite';
import { useState } from 'react';
import { BoardDensityToggle } from './BoardDensityToggle';
import type { BoardDensity } from './boardDensity';

// The dock's tiles ⇄ text board-layout switch.
const meta = {
  title: 'Game/Board/BoardDensityToggle',
  component: BoardDensityToggle,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof BoardDensityToggle>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Tiles: Story = {
  args: { density: 'tiles', onToggle: () => {} },
};

export const Text: Story = {
  args: { density: 'text', onToggle: () => {} },
};

function InteractiveDemo() {
  const [density, setDensity] = useState<BoardDensity>('tiles');
  return <BoardDensityToggle density={density} onToggle={() => setDensity((d) => (d === 'text' ? 'tiles' : 'text'))} />;
}

export const Interactive: Story = {
  args: { density: 'tiles', onToggle: () => {} },
  render: () => <InteractiveDemo />,
};
