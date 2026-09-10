import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { Card } from './Card';
import { minion, card as libCard, gun, retainer, bloodStack, faceDownBack } from './__fixtures__/gameFixtures';
import { withCardList } from './__fixtures__/decorators';

// The "full" card row (card.jsp) — used for CRYPT and expanded piles, and the
// recursive host for attached-card ladders. MinionTile is the tile form of the
// same data for the live board.
const meta = {
  title: 'Game/Board/Card',
  component: Card,
  decorators: [withCardList],
  args: { region: 'CRYPT', coordinate: '1', onAction: fn(), onQuick: fn() },
} satisfies Meta<typeof Card>;

export default meta;
type Story = StoryObj<typeof meta>;

export const CryptMinion: Story = {
  args: { card: minion('Muaziz, Archon of Ulugh Beg', { counters: 0 }) },
};

export const ReadyWithAttachments: Story = {
  args: {
    region: 'READY',
    card: minion('Cristo', { counters: 2, cards: [gun(), retainer(), bloodStack(3)] }),
  },
};

export const Locked: Story = {
  args: { region: 'READY', card: minion('Lucita, Anarch Sympathizer', { counters: 5, locked: true }) },
};

export const Contested: Story = {
  args: { region: 'READY', card: minion('Anson', { contested: true, counters: 1 }) },
};

export const FaceDownWithVisibleChild: Story = {
  args: {
    region: 'READY',
    card: { ...faceDownBack(), cards: [libCard('Political Stranglehold', { typeClass: 'political' })] },
  },
};

export const LibraryCardRow: Story = {
  args: { region: 'ASH_HEAP', card: libCard('Govern the Unaligned', { label: 'saved for finals' }) },
};
