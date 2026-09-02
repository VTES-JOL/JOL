import type { Meta, StoryObj } from '@storybook/react-vite';
import { LegacyDeckEditor } from './LegacyDeckEditor';

const contents = `2x Alan Sovereign (ADV)
4x Nkechi
2x Jost Werner

// still tuning the combat package
10x Govern the Unaligned
6x Villein
2x Deflection
4x .44 Magnum
2x wwef
removed 2x lilith
`;

const meta = {
  title: 'Deck/LegacyDeckEditor',
  component: LegacyDeckEditor,
  parameters: { layout: 'centered' },
  args: {
    deckName: 'Old Ventrue Grind',
    initialContents: contents,
    errors: ['2x wwef', 'removed 2x lilith'],
    onSave: async () => {},
    onDelete: () => {},
  },
  decorators: [
    (Story) => (
      <div className="w-[440px] h-[560px] flex flex-col">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof LegacyDeckEditor>;

export default meta;
type Story = StoryObj<typeof meta>;

export const WithUnresolvedLines: Story = {};

export const ParsesCleanly: Story = {
  args: { errors: [] },
};
