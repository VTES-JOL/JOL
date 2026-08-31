import type { Meta, StoryObj } from '@storybook/react-vite';
import type { DeckValidity } from '../../api/types';
import type { DeckEntry } from './deckKit';
import { DeckStatusBar } from './DeckStatusBar';

const entries: DeckEntry[] = [
  { cardId: 'c1', name: 'Anson', count: 12, isCrypt: true, types: ['Vampire'], group: '2', banned: false },
  { cardId: 'l1', name: 'Govern the Unaligned', count: 40, isCrypt: false, types: ['Action'], banned: false },
  { cardId: 'l2', name: 'Deflection', count: 40, isCrypt: false, types: ['Reaction'], banned: false },
];

const validity = (over: Partial<Record<'STANDARD' | 'DUEL' | 'V5', DeckValidity>>): Record<string, DeckValidity> => {
  const base = (fmt: string, valid: boolean, errors: string[] = []): DeckValidity => ({
    format: fmt,
    valid,
    errors,
    computedAt: new Date().toISOString(),
  });
  return {
    STANDARD: over.STANDARD ?? base('Standard', true),
    DUEL: over.DUEL ?? base('Duel', false, ['Crypt must be exactly 3 cards for Duel.', 'Deck exceeds the Duel card limit.']),
    V5: over.V5 ?? base('V5', false, ['Contains cards outside the V5 card pool.']),
  };
};

const meta = {
  title: 'Deck/DeckStatusBar',
  component: DeckStatusBar,
  parameters: { layout: 'padded' },
  decorators: [
    (Story) => (
      <div className="w-[560px] bg-surface border border-line/60 rounded-lg">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof DeckStatusBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Mixed: Story = { args: { entries, formatValidity: validity({}) } };

export const AllValid: Story = {
  args: {
    entries,
    formatValidity: validity({
      DUEL: { format: 'Duel', valid: true, errors: [], computedAt: new Date().toISOString() },
      V5: { format: 'V5', valid: true, errors: [], computedAt: new Date().toISOString() },
    }),
  },
};

export const WithBanned: Story = {
  args: {
    entries: [...entries, { cardId: 'b1', name: 'Banned Card', count: 2, isCrypt: false, types: ['Master'], banned: true }],
    formatValidity: validity({}),
  },
};

export const Empty: Story = { args: { entries: [], formatValidity: {} } };
