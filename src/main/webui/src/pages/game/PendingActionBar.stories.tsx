import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { PendingActionBar } from './PendingActionBar';
import { pendingAction } from './__fixtures__/gameFixtures';

// The response-window strip under the HUD (rules R1). Three faces by viewer:
// a seat that owes a response (urgent, Respond / Pass), the actor (accent,
// Resolve / Take Edge), everyone else (quiet informational line).
const meta = {
  title: 'Game/HUD/PendingActionBar',
  component: PendingActionBar,
  parameters: { layout: 'fullscreen' },
  decorators: [(Story) => <div className="border border-line-accent"><Story /></div>],
  args: { onCommand: fn(), onRespond: fn() },
} satisfies Meta<typeof PendingActionBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const YouOweAResponse: Story = {
  args: { pending: pendingAction({ awaiting: ['Player2'] }), viewerName: 'Player2' },
};

export const BeingBledDirectly: Story = {
  args: { pending: pendingAction({ type: 'BLEED', targetPlayer: 'Player2', amount: 3, awaiting: ['Player2'] }), viewerName: 'Player2' },
};

export const ActorWaiting: Story = {
  args: { pending: pendingAction({ awaiting: ['Player2', 'Player3'] }), viewerName: 'Player1' },
};

export const ActorAllPassed: Story = {
  name: 'Actor — all passed (Take Edge)',
  args: { pending: pendingAction({ type: 'BLEED', awaiting: [], passed: ['Player2', 'Player3'] }), viewerName: 'Player1' },
};

export const Bystander: Story = {
  args: { pending: pendingAction({ awaiting: ['Player2'], passed: ['Player3'] }), viewerName: 'Player3' },
};

export const PoliticalReferendum: Story = {
  args: {
    pending: pendingAction({ type: 'POLITICAL', label: 'referendum', targetPlayer: null, amount: 0, awaiting: ['Player2', 'Player3'] }),
    viewerName: 'Player2',
  },
};
