import type { Meta, StoryObj } from '@storybook/react-vite';
import type { CardDetail, ImportPreview } from '../../api/types';
import { DeckImportModal } from './DeckImportModal';

const card = (name: string, crypt: boolean, types: string[]): CardDetail => ({
  id: name,
  name,
  crypt,
  types,
  group: crypt ? '2' : null,
  banned: false,
  advanced: false,
  sets: [],
  clan: crypt ? 'Ventrue' : null,
  path: null,
  capacity: crypt ? 6 : null,
  disciplines: [],
  andDisciplines: [],
  orDisciplines: [],
  requirementClans: [],
  requirementPath: null,
  poolCost: null,
  bloodCost: null,
});

const preview: ImportPreview = {
  format: 'krcg',
  deckName: 'Imported Ventrue',
  deckDescription: 'A sample import.',
  resolved: [
    { count: 4, card: card('Anson', true, ['Vampire']) },
    { count: 10, card: card('Govern the Unaligned', false, ['Action']) },
    { count: 12, card: card('Deflection', false, ['Reaction']) },
  ],
  errors: [{ line: '3 Made Up Card', reason: 'Card not found' }],
};

const meta = {
  title: 'Deck/DeckImportModal',
  component: DeckImportModal,
  parameters: { layout: 'fullscreen' },
  args: {
    onImport: async () => {},
    onClose: () => {},
    onPreview: async () => {
      await new Promise((r) => setTimeout(r, 300));
      return preview;
    },
  },
} satisfies Meta<typeof DeckImportModal>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
