import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, fn, userEvent, within } from 'storybook/test';
import { RingBoard } from './RingBoard';
import { buildRingModel } from './ringModel';
import { turn9Table, TURN9_VIEWER } from './__fixtures__/ringFixtures';
import { cardView } from './cardView';
import { minion } from '../__fixtures__/gameFixtures';
import type { PlayerSnapshot } from '../../../api/types';

// Layout C — the wedge ring. Same turn-9 board at 2–5 players, as a player, as a
// judge / spectator (no own seat), across the size range, and stress-packed.
const HUB = ['Turn 9', 'Minion Phase', 'Predator right · Prey left'];

const table = (n: 2 | 3 | 4 | 5, viewerName: string | null = null, focusName: string | null = null) => {
  const { players, seating } = turn9Table(n);
  return buildRingModel(players, seating, { viewerName, focusName });
};

const meta = {
  title: 'Game/Ring/RingBoard',
  component: RingBoard,
  parameters: { layout: 'centered' },
  args: { hub: HUB, showNames: true, size: 820, meter: 'gauge', model: table(5, TURN9_VIEWER), viewerName: TURN9_VIEWER },
} satisfies Meta<typeof RingBoard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const FivePlayers: Story = {};
export const FourPlayers: Story = { args: { model: table(4, 'Sable Voss'), viewerName: 'Sable Voss' } };
export const ThreePlayers: Story = { args: { model: table(3, 'Marcus Kane'), viewerName: 'Marcus Kane' } };
export const TwoPlayers: Story = { args: { model: table(2, 'Marcus Kane'), viewerName: 'Marcus Kane' } };

export const JudgeOrSpectator: Story = {
  name: 'Judge / spectator (no own seat)',
  args: { model: table(5), viewerName: null },
};
export const FocusOnASeat: Story = {
  name: 'Spectator focused on Ilya Rostova',
  args: { model: table(5, null, 'Ilya Rostova'), viewerName: null },
};

export const Pips: Story = { args: { meter: 'pips' } };
export const Small: Story = { args: { size: 560 } };
export const Large: Story = { args: { size: 1000 } };

export const OustedSeat: Story = {
  args: (() => {
    const { players, seating } = turn9Table(5);
    const out: PlayerSnapshot[] = players.map((p) => (p.name === 'The Baron' ? { ...p, pool: 0 } : p));
    return { model: buildRingModel(out, seating, { viewerName: TURN9_VIEWER }) };
  })(),
};

/** Every seat holds 12 ready cards: packs smaller, and reports what does not fit. */
export const StressBusyBoard: Story = {
  args: (() => {
    const base = table(5, TURN9_VIEWER);
    const seats = base.seats.map((s) => ({
      ...s,
      ready: Array.from({ length: 12 }, (_, i) =>
        cardView(minion(`Vampire ${i + 1}`, { id: `${s.name}-${i}`, counters: (i % 5) + 1, capacity: 6, locked: i % 4 === 0 })),
      ),
    }));
    return { model: { ...base, seats } };
  })(),
};

export const Overflow: Story = {
  args: (() => {
    const base = table(5, TURN9_VIEWER);
    const seats = base.seats.map((s, i) =>
      i === 0
        ? { ...s, ready: Array.from({ length: 40 }, (_, k) => cardView(minion(`V${k}`, { id: `o-${k}`, counters: 2, capacity: 5 }))) }
        : s,
    );
    return { model: { ...base, seats } };
  })(),
};

export const Interactive: Story = {
  args: { onSeatClick: fn(), onCardClick: fn(), onMarkerClick: fn() },
  play: async ({ canvasElement, args }) => {
    const c = within(canvasElement);
    await userEvent.click(c.getByRole('button', { name: /^Sable Voss: / }));
    await expect(args.onSeatClick).toHaveBeenCalledTimes(1);
    await expect(args.onSeatClick).toHaveBeenCalledWith(expect.objectContaining({ name: 'Sable Voss' }));

    // A card click reaches the card, not the wedge behind it.
    await userEvent.click(c.getByRole('button', { name: /^Ayelech, blood 6 of 7/ }));
    await expect(args.onCardClick).toHaveBeenCalledTimes(1);
    await expect(args.onSeatClick).toHaveBeenCalledTimes(1);

    await userEvent.click(c.getAllByRole('button', { name: /Hector Trelane, uncontrolled/ })[0]);
    await expect(args.onMarkerClick).toHaveBeenCalledTimes(1);
  },
};
