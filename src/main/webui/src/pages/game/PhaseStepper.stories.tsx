import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { PhaseStepper } from './PhaseStepper';

// The five-phase turn ribbon in the HUD. All phases always render (past dimmed,
// current lit, future quiet); `selectablePhases` gates which you can jump to.
// Muted whole when it isn't the viewer's turn.
const meta = {
  title: 'Game/HUD/PhaseStepper',
  component: PhaseStepper,
  parameters: { layout: 'centered' },
  args: { onSelect: fn() },
} satisfies Meta<typeof PhaseStepper>;

export default meta;
type Story = StoryObj<typeof meta>;

export const YourTurnMinion: Story = {
  args: { current: 'Minion', selectablePhases: ['Minion', 'Influence', 'Discard'], canSelect: true, active: true },
};

export const YourTurnUnlock: Story = {
  args: { current: 'Unlock', selectablePhases: ['Unlock', 'Master', 'Minion', 'Influence', 'Discard'], canSelect: true, active: true },
};

export const OpponentTurn: Story = {
  args: { current: 'Influence', selectablePhases: ['Influence'], canSelect: false, active: false },
};

export const LastPhase: Story = {
  args: { current: 'Discard', selectablePhases: ['Discard'], canSelect: true, active: true },
};
