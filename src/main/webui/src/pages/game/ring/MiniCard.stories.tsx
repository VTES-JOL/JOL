import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { MiniCard } from './MiniCard';
import { cardView, type CardView } from './cardView';
import { card, gun, hiddenCard, minion, retainer } from '../__fixtures__/gameFixtures';
import { ally, location } from './__fixtures__/ringFixtures';
import { withGlyphFrame } from './__fixtures__/glyphFrames';
import { SEAT_COLORS } from './ringModel';

// The overview card glyph (Wedge / Panels). 46×64 at scale 1, turned 90° when
// locked. Stories are magnified with CSS zoom; the "Sizes" story shows the
// packer's scale range at true size so legibility can be judged where it counts.
const v = (c: Parameters<typeof cardView>[0], torpor = false): CardView => cardView(c, { torpor });

const meta = {
  title: 'Game/Ring/MiniCard',
  component: MiniCard,
  decorators: [withGlyphFrame(3)],
  args: { seatColor: SEAT_COLORS[0], meter: 'gauge', scale: 1 },
} satisfies Meta<typeof MiniCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Ready: Story = { args: { view: v(minion('Muaziz, Archon of Ulugh Beg', { counters: 4, capacity: 7 })) } };
export const Full: Story = { args: { view: v(minion('Trevon Parker', { counters: 6, capacity: 6 })) } };
export const Empty: Story = { args: { view: v(minion('Tarautas', { counters: 0, capacity: 4 })) } };
export const Locked: Story = { args: { view: v(minion('Ayelech', { counters: 6, capacity: 7, locked: true })) } };
export const Contested: Story = { args: { view: v(minion('Emily Carson', { counters: 4, capacity: 5, contested: true, label: 'Primogen' })) } };
export const Torpor: Story = { args: { view: v(minion('Hamid Mansour', { counters: 0, capacity: 4 }), true) } };
export const LockedAndContested: Story = {
  args: { view: v(minion('Mkhokheli', { counters: 2, capacity: 6, locked: true, contested: true, label: 'Prince' })) },
};
export const Titled: Story = { args: { view: v(minion('Mustafa, The Heir', { counters: 5, capacity: 6, label: 'Prince' })) } };
export const WithAttachments: Story = {
  args: { view: v(minion('Theo Bell', { counters: 8, capacity: 8, cards: [gun(), gun(), retainer(), card('Flak Jacket')] })) },
};
export const LongName: Story = { args: { view: v(minion('Lord Ephraim Wainwright', { counters: 3, capacity: 6 })) } };
export const FaceDown: Story = { args: { view: v(minion('Secret Vampire', { counters: 3, capacity: 6, faceDown: true })) } };
export const HiddenBack: Story = { args: { view: v(hiddenCard()) } };
export const Ally: Story = { args: { view: v(ally('War Ghoul', 3)) } };
export const AllyManyCounters: Story = { args: { view: v(ally('War Ghoul', 14)) } };
export const AllyEmpty: Story = { args: { view: v(ally('War Ghoul', 0)) } };
export const PartialGauge: Story = { args: { view: v(minion('Naomi Stewart', { counters: 2, capacity: 6 })) } };
export const EmptyWithCapacity: Story = { args: { view: v(minion('Tarautas', { counters: 0, capacity: 5 })) } };
export const Location: Story = { args: { view: v(location('Powerbase: Montreal')) } };
export const Pips: Story = {
  args: { meter: 'pips', view: v(minion('Kateline Nadasdy', { counters: 5, capacity: 7 })) },
};
export const Clickable: Story = {
  args: { onClick: fn(), view: v(minion('Vasily', { counters: 4, capacity: 6 })) },
};

/** Every state side by side, both meters — the review sheet. */
export const Gallery: Story = {
  args: { view: v(minion('x')) },
  decorators: [withGlyphFrame(2)],
  render: ({ seatColor }) => {
    const states: [string, CardView][] = [
      ['Ready', v(minion('Muaziz, Archon', { counters: 4, capacity: 7 }))],
      ['Locked', v(minion('Ayelech', { counters: 6, capacity: 7, locked: true }))],
      ['Contested', v(minion('Emily Carson', { counters: 4, capacity: 5, contested: true, label: 'Primogen' }))],
      ['Torpor', v(minion('Hamid Mansour', { counters: 0, capacity: 4 }), true)],
      ['Attached', v(minion('Theo Bell', { counters: 8, capacity: 8, cards: [gun(), retainer()] }))],
      ['Ally 3', v(ally('War Ghoul', 3))],
      ['Ally 12', v(ally('War Ghoul', 12))],
      ['Partial', v(minion('Naomi Stewart', { counters: 2, capacity: 6 }))],
      ['Empty', v(minion('Tarautas', { counters: 0, capacity: 5 }))],
      ['Location', v(location('Powerbase: Montreal'))],
      ['Hidden', v(hiddenCard())],
    ];
    return (
      <div className="flex flex-col gap-5">
        {(['gauge', 'pips'] as const).map((meter) => (
          <div key={meter} className="flex flex-wrap items-end gap-6">
            {states.map(([label, view]) => (
              <div key={label} className="flex w-[70px] flex-col items-center gap-2">
                <div className="flex h-[70px] w-[70px] items-center justify-center">
                  <MiniCard view={view} meter={meter} seatColor={seatColor} />
                </div>
                <span className="text-center text-[9px] text-ink-muted">{label}</span>
              </div>
            ))}
          </div>
        ))}
      </div>
    );
  },
};

/** True on-screen size across the packer's scale range (no zoom): can you read it? */
export const Sizes: Story = {
  args: { view: v(minion('Trevon Parker', { counters: 4, capacity: 6, label: 'Prince', cards: [gun()] })) },
  decorators: [withGlyphFrame(1)],
  render: ({ view, seatColor, meter }) => (
    <div className="flex items-end gap-4">
      {[1, 0.88, 0.76, 0.64, 0.52].map((s) => (
        <div key={s} className="flex flex-col items-center gap-2">
          <div className="flex items-center justify-center" style={{ width: 64 * s, height: 64 * s }}>
            <MiniCard view={view} seatColor={seatColor} meter={meter} scale={s} />
          </div>
          <span className="text-[10px] text-ink-muted">{Math.round(s * 100)}%</span>
        </div>
      ))}
    </div>
  ),
};

export const SeatColours: Story = {
  args: { view: v(minion('Anson', { counters: 3, capacity: 6 })) },
  decorators: [withGlyphFrame(2)],
  render: ({ view }) => (
    <div className="flex gap-3">
      {SEAT_COLORS.map((c) => (
        <MiniCard key={c} view={view} seatColor={c} />
      ))}
    </div>
  ),
};
