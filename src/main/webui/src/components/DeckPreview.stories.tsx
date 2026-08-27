import type { Meta, StoryObj } from '@storybook/react-vite';
import type { Deck } from '../api/types';
import { DeckPreview } from './DeckPreview';

const sampleDeck: Deck = {
  id: 'd1',
  name: 'Sample Toolbox',
  player: 'Player1',
  author: 'Player1',
  comments: '',
  crypt: {
    count: 4,
    cards: [
      { id: 1, name: 'Anarch Convert', count: 2, comments: '' },
      { id: 2, name: 'Beast, The Fury of Caine', count: 1, comments: '' },
      { id: 3, name: 'Carolina Vález', count: 1, comments: 'playtest' },
    ],
  },
  library: {
    count: 6,
    cards: [
      {
        type: 'Action',
        count: 3,
        cards: [
          { id: 4, name: 'Deep Song', count: 2, comments: '' },
          { id: 5, name: 'Blood Hunt', count: 1, comments: '' },
        ],
      },
      {
        type: 'Combat',
        count: 3,
        cards: [{ id: 6, name: 'Immortal Grapple', count: 3, comments: '' }],
      },
    ],
  },
};

const meta = {
  title: 'Components/DeckPreview',
  component: DeckPreview,
  parameters: { layout: 'padded' },
} satisfies Meta<typeof DeckPreview>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { deck: sampleDeck },
};

export const EmptyDeck: Story = {
  args: {
    deck: { ...sampleDeck, crypt: { count: 0, cards: [] }, library: { count: 0, cards: [] } },
  },
};
