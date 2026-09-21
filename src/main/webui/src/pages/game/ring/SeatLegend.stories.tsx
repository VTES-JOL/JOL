import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, fn, userEvent, within } from 'storybook/test';
import { SeatLegend } from './SeatLegend';
import { buildRingModel } from './ringModel';
import { TURN9_VIEWER, turn9Players, turn9Table } from './__fixtures__/ringFixtures';
import { region } from '../__fixtures__/gameFixtures';
import { card, hiddenCard } from '../__fixtures__/gameFixtures';

const { players, seating } = turn9Table(5);
const withHands = players.map((p) => ({
  ...p,
  regions: [
    ...p.regions,
    region('HAND', p.name === TURN9_VIEWER || p.name === 'Marcus Kane'
      ? [card('Deep Song'), card('Govern the Unaligned'), card('Blood Doll')]
      : [hiddenCard(), hiddenCard(), hiddenCard(), hiddenCard()]),
  ],
}));
const seatsOf = (ps = withHands, s = seating) => buildRingModel(ps, s, { viewerName: TURN9_VIEWER }).seats;

const meta = {
  title: 'Game/Ring/SeatLegend',
  component: SeatLegend,
  args: { seats: seatsOf(), viewerName: TURN9_VIEWER },
} satisfies Meta<typeof SeatLegend>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Player: Story = {};
export const JudgeWithHands: Story = { args: { showHands: true, viewerName: null } };
export const TwoPlayers: Story = { args: { seats: buildRingModel(turn9Players().slice(0, 2), seating.slice(0, 2)).seats, viewerName: null } };
export const Ousted: Story = {
  args: {
    seats: seatsOf(withHands.map((p) => (p.name === 'The Baron' ? { ...p, pool: 0, exitKind: 'OUST' as const } : p))),
  },
};
export const Clickable: Story = {
  args: { onSeatClick: fn() },
  play: async ({ canvasElement, args }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: /Ilya Rostova/ }));
    await expect(args.onSeatClick).toHaveBeenCalledWith(expect.objectContaining({ name: 'Ilya Rostova' }));
  },
};
