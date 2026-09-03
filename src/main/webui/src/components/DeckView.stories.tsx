import type { Meta, StoryObj } from '@storybook/react-vite';
import type { CardDetail, Deck } from '../api/types';
import { DeckView } from './DeckView';

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

// A partial detail map — enough to show icons render when present and rows
// degrade gracefully when a card has no detail.
const details: Record<string, CardDetail> = {
  '1': {
    id: '1', name: 'Anarch Convert', crypt: true, types: ['Vampire'], group: 'ANY', banned: false,
    advanced: false, sets: [], clan: 'Caitiff', path: null, capacity: 1, disciplines: [],
    andDisciplines: [], orDisciplines: [], requirementClans: [], requirementPath: null,
    poolCost: null, bloodCost: null,
  },
  '4': {
    id: '4', name: 'Deep Song', crypt: false, types: ['Action'], group: null, banned: false,
    advanced: false, sets: [], clan: null, path: null, capacity: null, disciplines: [],
    andDisciplines: [], orDisciplines: ['ANI'], requirementClans: [], requirementPath: null,
    poolCost: null, bloodCost: null,
  },
};

const meta = {
  title: 'Components/DeckView',
  component: DeckView,
  parameters: { layout: 'padded' },
} satisfies Meta<typeof DeckView>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { deck: sampleDeck, details },
};

export const NoDetails: Story = {
  args: { deck: sampleDeck },
};

export const EmptyDeck: Story = {
  args: {
    deck: { ...sampleDeck, crypt: { count: 0, cards: [] }, library: { count: 0, cards: [] } },
  },
};
