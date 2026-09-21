import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { UncontrolledMarker } from './UncontrolledMarker';
import type { MarkerView } from './ringModel';
import { withGlyphFrame } from './__fixtures__/glyphFrames';
import { SEAT_COLORS } from './ringModel';

// A crypt vampire still in the uncontrolled region, drawn on the ring's rim.
const m = (o: Partial<MarkerView> = {}): MarkerView => ({
  id: 'u1',
  name: 'Hector Trelane',
  hidden: false,
  counters: 3,
  capacity: 5,
  ...o,
});

const meta = {
  title: 'Game/Ring/UncontrolledMarker',
  component: UncontrolledMarker,
  decorators: [withGlyphFrame(4)],
  args: { seatColor: SEAT_COLORS[2], scale: 1, marker: m() },
} satisfies Meta<typeof UncontrolledMarker>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Partial: Story = {};
export const Empty: Story = { args: { marker: m({ counters: 0 }) } };
export const Full: Story = { args: { marker: m({ counters: 5, capacity: 5 }) } };
export const HiddenOpponent: Story = {
  name: 'Hidden (opponent — count only)',
  args: { marker: m({ hidden: true, name: '', capacity: 0, counters: 2 }) },
};
export const Clickable: Story = { args: { onClick: fn() } };

export const Row: Story = {
  decorators: [withGlyphFrame(2)],
  render: ({ seatColor }) => (
    <div className="flex items-center gap-3">
      {[0, 1, 2, 3, 4, 5].map((n) => (
        <UncontrolledMarker key={n} marker={m({ counters: n, capacity: 5 })} seatColor={seatColor} />
      ))}
      <UncontrolledMarker marker={m({ hidden: true, name: '', capacity: 0, counters: 3 })} seatColor={seatColor} />
    </div>
  ),
};
