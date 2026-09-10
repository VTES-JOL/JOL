import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { CardSimple } from './CardSimple';
import { card, minion, faceDownBack } from './__fixtures__/gameFixtures';
import { withCardList } from './__fixtures__/decorators';

// card-simple.jsp — the one-line row for ASH_HEAP / HAND / RFG / LIBRARY /
// RESEARCH. Type icon on the right, no recursion.
const meta = {
  title: 'Game/Board/CardSimple',
  component: CardSimple,
  decorators: [withCardList],
  args: { region: 'ASH_HEAP', coordinate: '1', onClick: fn() },
} satisfies Meta<typeof CardSimple>;

export default meta;
type Story = StoryObj<typeof meta>;

export const AshHeap: Story = {
  args: { card: card('Govern the Unaligned', { typeClass: 'action' }) },
};

export const WithLabel: Story = {
  args: { card: card('Deflection', { typeClass: 'reaction', label: 'bounce Player3' }) },
};

export const CryptCardInPile: Story = {
  args: { region: 'RESEARCH', card: minion('Antara', { hasBlood: true, clanClasses: ['gangrel'] }) },
};

export const FaceDown: Story = {
  args: { region: 'HAND', card: faceDownBack() },
};

export const Advanced: Story = {
  args: { card: minion('Kemintiri', { advanced: true, typeClass: 'vampire' }) },
};
