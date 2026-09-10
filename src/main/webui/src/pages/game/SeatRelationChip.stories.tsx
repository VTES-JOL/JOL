import type { Meta, StoryObj } from '@storybook/react-vite';
import { SeatRelationChip } from './SeatRelationChip';

// The "▼ Your prey" / "▲ Your predator" / "Across the table" tag above an
// opponent seat. Prey green (downstream), predator gold (acts on you).
const meta = {
  title: 'Game/HUD/SeatRelationChip',
  component: SeatRelationChip,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof SeatRelationChip>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Prey: Story = { args: { relation: 'prey' } };
export const Predator: Story = { args: { relation: 'predator' } };
export const AcrossTable: Story = { args: { relation: 'table' } };
export const None: Story = { args: { relation: null } };
