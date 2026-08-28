import type { Meta, StoryObj } from '@storybook/react-vite';
import type { DeckInfoBean } from '../../api/types';
import { DeckListPane } from './DeckListPane';

const decks: DeckInfoBean[] = [
  { name: 'Weenie Animalism', deckId: 'd1', deckFormat: 'TAGGED', gameFormats: ['STANDARD'], comments: 'Bleed + wall hybrid.' },
  { name: 'V5 Hecata', deckId: 'd2', deckFormat: 'MODERN', gameFormats: ['STANDARD', 'V5'], comments: '' },
  { name: 'Old Legacy Import', deckId: 'd3', deckFormat: 'LEGACY', gameFormats: [], comments: 'Resave to upgrade.' },
  { name: 'Lasombra Duel', deckId: 'd4', deckFormat: 'MODERN', gameFormats: ['DUEL'], comments: 'Fast oust.' },
];

const meta = {
  title: 'Deck/DeckListPane',
  component: DeckListPane,
  parameters: { layout: 'centered' },
  args: {
    decks,
    tags: ['STANDARD', 'V5', 'DUEL'],
    selectedId: 'd2',
    formatFilter: '',
    onFormatFilterChange: () => {},
    onSelect: () => {},
    onNew: () => {},
    onImport: () => {},
  },
  decorators: [
    (Story) => (
      <div className="jt:w-[320px] jt:h-[440px] jt:flex jt:flex-col">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof DeckListPane>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
export const Empty: Story = { args: { decks: [] } };
