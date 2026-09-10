import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { PlayerBoard } from './PlayerBoard';
import { player, selfPlayer, region, minion, card, hiddenCard } from './__fixtures__/gameFixtures';
import { withBoardDensity, withSeatFrame } from './__fixtures__/decorators';

// One whole seat: header (relation chip · acting flag · edge · VP · pool tone)
// over the board regions and the piles footer.
const meta = {
  title: 'Game/Board/PlayerBoard',
  component: PlayerBoard,
  decorators: [withSeatFrame, withBoardDensity('tiles', 2)],
  args: {
    gameId: 'sb-board',
    edgeColor: '#8b1a1a',
    edgeTextColor: 'white',
    isSeatedPlayer: true,
    viewerName: 'Player1',
    onTableCardClick: fn(),
    onQuickCommand: fn(),
    onCounterBump: fn(),
    onPlayCardClick: fn(),
  },
} satisfies Meta<typeof PlayerBoard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Opponent: Story = {
  args: { player: player('Player2', { pool: 11 }), relation: 'prey' },
};

export const ActingWithEdge: Story = {
  args: {
    player: player('Player3', { pool: 7, active: true, edge: true, victoryPoints: 1 }),
    relation: 'predator',
  },
};

export const OwnSeat: Story = {
  args: { player: selfPlayer('Player1', { pool: 24 }), viewerName: 'Player1' },
};

export const LowPoolPinged: Story = {
  args: { player: player('Player4', { pool: 3, pinged: true }), relation: 'table', pingable: true },
};

export const Ousted: Story = {
  args: {
    player: player('Player5', {
      pool: 0,
      victoryPoints: 0,
      exitKind: 'OUST',
      exitVpRecipient: 'Player1',
      regions: [
        region('READY', []),
        region('TORPOR', []),
        region('UNCONTROLLED', []),
        region('RESEARCH', [card('Edge Explosion', { typeClass: 'action' })]),
        region('ASH_HEAP', Array.from({ length: 20 }, () => hiddenCard())),
      ],
    }),
  },
};

export const InfluencePriority: Story = {
  name: 'Influence phase (uncontrolled floated up)',
  args: {
    player: selfPlayer('Player1', {
      regions: [
        region('READY', [minion('Lucita, Anarch Sympathizer', { counters: 5 })]),
        region('TORPOR', []),
        region('UNCONTROLLED', [minion('Anvil', { counters: 2 }), minion('Tarbaby Jack', { counters: 1 })]),
        region('RESEARCH', []),
        region('HAND', [card('Govern the Unaligned')], { openHand: true }),
      ],
    }),
    influencePriority: true,
  },
};
