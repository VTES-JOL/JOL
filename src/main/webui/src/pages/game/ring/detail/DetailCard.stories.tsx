import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, fn, userEvent, within } from 'storybook/test';
import { DetailCard } from './DetailCard';
import { cardView } from '../cardView';
import { card, gun, hiddenCard, minion, retainer } from '../../__fixtures__/gameFixtures';
import { ally, location } from '../__fixtures__/ringFixtures';
import { SEAT_COLORS } from '../ringModel';

// The Triad / Seat card — text-first, room to read. (The overview glyph is MiniCard.)
const v = (c: Parameters<typeof cardView>[0], torpor = false) => cardView(c, { torpor });

const meta = {
  title: 'Game/Ring/DetailCard',
  component: DetailCard,
  decorators: [(Story) => (<div style={{ width: 240, padding: 12 }}><Story /></div>)],
  args: { seatColor: SEAT_COLORS[2], size: 'seat', view: v(minion('Muaziz, Archon of Ulugh Beg', { counters: 4, capacity: 7, label: 'Prince' })) },
} satisfies Meta<typeof DetailCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Ready: Story = {};
export const Triad: Story = { args: { size: 'triad' }, decorators: [(Story) => (<div style={{ width: 170, padding: 12 }}><Story /></div>)] };
export const Locked: Story = { args: { view: v(minion('Ayelech', { counters: 6, capacity: 7, locked: true })) } };
export const Contested: Story = { args: { view: v(minion('Emily Carson', { counters: 4, capacity: 5, contested: true, label: 'Primogen' })) } };
export const Torpor: Story = { args: { view: v(minion('Hamid Mansour', { counters: 0, capacity: 4 }), true) } };
export const FaceDown: Story = { args: { view: v(minion('Secret Vampire', { counters: 3, capacity: 6, faceDown: true })) } };
export const WithAttachments: Story = {
  args: { view: v(minion('Theo Bell', { counters: 8, capacity: 8, cards: [gun(), gun(), retainer(), card('Flak Jacket')] })) },
};
export const Ally: Story = { args: { view: v(ally('War Ghoul', 5)) } };
export const Location: Story = { args: { view: v(location('Powerbase: Montreal')) } };
export const Hidden: Story = { args: { view: v(hiddenCard()) } };

export const WithStepper: Story = {
  args: { onCounter: fn(), onClick: fn() },
  play: async ({ canvasElement, args }) => {
    const c = within(canvasElement);
    await userEvent.click(c.getByRole('button', { name: /Add blood/ }));
    await expect(args.onCounter).toHaveBeenCalledWith(expect.anything(), 1);
    await expect(args.onClick).not.toHaveBeenCalled(); // stepper never triggers the card click
    await userEvent.click(c.getByRole('button', { name: /^Muaziz/ }));
    await expect(args.onClick).toHaveBeenCalledTimes(1);
  },
};
