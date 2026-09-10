import type { Meta, StoryObj } from '@storybook/react-vite';
import type { PlayerSnapshot } from '../../api/types';
import { SeatGrid } from './SeatGrid';
import { player } from './__fixtures__/gameFixtures';

// The opponent-seat layout extracted from GamePage. `renderSeat` here draws a
// labelled placeholder so the column-count and 2×2 ordering logic is the focus
// (real seats are covered by SeatColumn / PlayerBoard stories).
const seats = (n: number): PlayerSnapshot[] =>
  Array.from({ length: n }, (_, i) => player(`Player${i + 2}`, { pool: 12 - i, victoryPoints: i === 0 ? 1 : 0 }));

const placeholder = (seat: PlayerSnapshot) => (
  <div className="rounded-lg border border-line-accent bg-hover/50 p-4 text-center text-sm">
    <div className="font-semibold text-ink">{seat.name}</div>
    <div className="text-ink-muted">pool {seat.pool}</div>
  </div>
);

const meta = {
  title: 'Game/Board/SeatGrid',
  component: SeatGrid,
  parameters: { layout: 'padded' },
  args: { renderSeat: placeholder },
} satisfies Meta<typeof SeatGrid>;

export default meta;
type Story = StoryObj<typeof meta>;

export const TwoOpponents: Story = { args: { seats: seats(2), variant: 'counted' } };
export const ThreeOpponents: Story = { args: { seats: seats(3), variant: 'counted' } };
export const FourOpponents: Story = { args: { seats: seats(4), variant: 'counted' } };

export const FourMidWide2x2: Story = {
  name: 'Four opponents, mid-wide (2×2, prey/predator flank)',
  args: { seats: seats(4), variant: 'counted', midWide: true },
};

export const FiveSpectator: Story = {
  name: 'Five seats (spectator, 3-up)',
  args: { seats: seats(5), variant: 'counted' },
};

export const AutoFill: Story = {
  name: 'Auto-fill (768–1023 band)',
  args: { seats: seats(4), variant: 'autofill' },
};
