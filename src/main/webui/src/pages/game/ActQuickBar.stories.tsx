import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { ActQuickBar } from './ActQuickBar';
import { selfPlayer, player, region, minion, pendingAction } from './__fixtures__/gameFixtures';

// The context-aware quick-command strip above the command input. Base row
// always (unlock all · edge · burn edge · draw); extra rows for your Influence
// turn, an open RUSH/RESCUE combat, or a POLITICAL referendum (with a vote
// tally summed from crypt `votes` annotations).
const meMinion = minion('Lucita, Anarch Sympathizer', { id: 'acting-1', counters: 5 });
const me = selfPlayer('Player1', {
  regions: [
    region('READY', [meMinion, minion('Cristo', { counters: 2 })]),
    region('TORPOR', []),
    region('UNCONTROLLED', []),
    region('RESEARCH', []),
    region('HAND', []),
  ],
});
const opponents = [
  player('Player2', { regions: [region('READY', [minion('Antara', { votes: '2' })]), region('TORPOR', [])] }),
  player('Player3', { regions: [region('READY', [minion('Anson', { votes: '1' }), minion('Muaziz, Archon of Ulugh Beg', { votes: 'P' })]), region('TORPOR', [])] }),
];

const meta = {
  title: 'Game/HUD/ActQuickBar',
  component: ActQuickBar,
  parameters: { layout: 'padded' },
  decorators: [(Story) => <div className="w-[420px] max-w-full"><Story /></div>],
  args: { onCommand: fn(), me, players: [me, ...opponents], hasEdge: true },
} satisfies Meta<typeof ActQuickBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const BaseRow: Story = {
  args: { phase: 'Master', isMyTurn: true, pending: null },
};

export const InfluenceTurn: Story = {
  args: { phase: 'Influence', isMyTurn: true, pending: null },
};

export const Combat: Story = {
  args: { phase: 'Minion', isMyTurn: true, pending: pendingAction({ type: 'RUSH', label: 'rush', actingCardId: 'acting-1', targetPlayer: 'Player2' }) },
};

export const Referendum: Story = {
  args: { phase: 'Minion', isMyTurn: true, pending: pendingAction({ type: 'POLITICAL', label: 'referendum', targetPlayer: null }) },
};
