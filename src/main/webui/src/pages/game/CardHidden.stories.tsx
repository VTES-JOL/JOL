import type { Meta, StoryObj } from '@storybook/react-vite';
import { CardHidden } from './CardHidden';
import { hiddenCard, faceDownBack } from './__fixtures__/gameFixtures';
import { withCardList } from './__fixtures__/decorators';

// The two withheld-card placeholders: the `*********` asterisks for a pile you
// cannot see into, and the card-back for a face-down card someone deliberately
// turned over in a visible region.
const meta = {
  title: 'Game/Board/CardHidden',
  component: CardHidden,
  decorators: [withCardList],
  args: { region: 'LIBRARY', coordinate: '1' },
} satisfies Meta<typeof CardHidden>;

export default meta;
type Story = StoryObj<typeof meta>;

export const HiddenPileCard: Story = {
  args: { card: hiddenCard() },
};

export const FaceDownBack: Story = {
  args: { region: 'READY', card: faceDownBack() },
};

export const WithCounters: Story = {
  args: { region: 'UNCONTROLLED', card: faceDownBack({ counters: 3 }) },
};

export const RemovedFromGame: Story = {
  name: 'Removed from game (dimmed)',
  args: { region: 'REMOVED_FROM_GAME', card: hiddenCard() },
};
