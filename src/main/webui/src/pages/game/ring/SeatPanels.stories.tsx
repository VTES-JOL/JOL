import type { Meta, StoryObj } from '@storybook/react-vite';
import { SeatPanels } from './SeatPanels';
import { buildRingModel } from './ringModel';
import { cardView } from './cardView';
import { minion } from '../__fixtures__/gameFixtures';
import { TURN9_VIEWER, turn9Table } from './__fixtures__/ringFixtures';

// Layout D at natural size (no viewport). See TableView for the zoom / pan wrapper.
const table = (n: 2 | 3 | 4 | 5, viewer: string | null = TURN9_VIEWER) => {
  const { players, seating } = turn9Table(n);
  return buildRingModel(players, seating, { viewerName: viewer });
};

const meta = {
  title: 'Game/Ring/SeatPanels',
  component: SeatPanels,
  parameters: { layout: 'centered' },
  args: { model: table(5), viewerName: TURN9_VIEWER, hub: ['Turn 9', 'Minion Phase', 'Predator right · Prey left'] },
} satisfies Meta<typeof SeatPanels>;

export default meta;
type Story = StoryObj<typeof meta>;

export const FivePlayers: Story = {};
export const FourPlayers: Story = { args: { model: table(4, 'Sable Voss'), viewerName: 'Sable Voss' } };
export const ThreePlayers: Story = { args: { model: table(3, 'Marcus Kane'), viewerName: 'Marcus Kane' } };
export const TwoPlayers: Story = { args: { model: table(2, 'Marcus Kane'), viewerName: 'Marcus Kane' } };
export const JudgeOrSpectator: Story = { args: { model: table(5, null), viewerName: null } };
export const Pips: Story = { args: { meter: 'pips' } };

/** A seat with 30 ready cards: its panel grows taller / wider, nothing shrinks or hides. */
export const BusySeat: Story = {
  args: (() => {
    const base = table(5);
    const seats = base.seats.map((s, i) =>
      i === 0
        ? { ...s, ready: Array.from({ length: 30 }, (_, k) => cardView(minion(`Vampire ${k + 1}`, { id: `b${k}`, counters: k % 6, capacity: 6, locked: k % 5 === 0 }))) }
        : s,
    );
    return { model: { ...base, seats } };
  })(),
};
