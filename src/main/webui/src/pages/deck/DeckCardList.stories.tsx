import type { Meta, StoryObj } from '@storybook/react-vite';
import type { CardDetail } from '../../api/types';
import type { DeckEntry } from '../../components/ui/deckKit';
import { DeckCardList } from './DeckCardList';

const entries: DeckEntry[] = [
  { cardId: '200001', name: 'Aabbt Kindred', count: 3, isCrypt: true, types: ['Vampire'], group: '2', banned: false },
  { cardId: '201733', name: 'Aaradhya, The Callous Tyrant', count: 2, isCrypt: true, types: ['Vampire'], group: '5', banned: false, advanced: false },
  { cardId: '101228', name: 'Govern the Unaligned', count: 10, isCrypt: false, types: ['Action'], banned: false },
  { cardId: '100901', name: 'Deflection', count: 12, isCrypt: false, types: ['Reaction'], banned: false },
  { cardId: '100730', name: 'Carrion Crows', count: 6, isCrypt: false, types: ['Action', 'Combat'], banned: false },
];

const detail = (over: Partial<CardDetail>): CardDetail => ({
  id: '', name: '', crypt: false, types: [], group: null, banned: false, advanced: false, sets: [],
  clan: null, path: null, capacity: null, disciplines: [],
  andDisciplines: [], orDisciplines: [], requirementClans: [], requirementPath: null, poolCost: null, bloodCost: null,
  ...over,
});

const detailMap = new Map<string, CardDetail>([
  ['200001', detail({ id: '200001', crypt: true, clan: 'Follower of Set', capacity: 4, disciplines: ['for', 'pre', 'ser'] })],
  ['201733', detail({ id: '201733', crypt: true, clan: 'Ventrue', capacity: 6, path: 'Power and the Inner Voice', disciplines: ['ANI', 'DOM', 'for', 'POT', 'PRE'] })],
  ['101228', detail({ id: '101228', types: ['Action'], orDisciplines: ['dom'], poolCost: null })],
  ['100901', detail({ id: '100901', types: ['Reaction'], orDisciplines: ['dom'] })],
  ['100730', detail({ id: '100730', types: ['Action', 'Combat'], andDisciplines: ['ani'], bloodCost: 1 })],
]);

const meta = {
  title: 'Deck/DeckCardList',
  component: DeckCardList,
  parameters: { layout: 'centered' },
  decorators: [
    (Story) => (
      <div className="jt:w-[380px] jt:h-[420px] jt:flex jt:flex-col jt:bg-surface jt:border jt:border-line/60 jt:rounded-lg jt:overflow-hidden">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof DeckCardList>;

export default meta;
type Story = StoryObj<typeof meta>;

export const ReadOnly: Story = {
  args: { entries, detailMap },
};

export const Editable: Story = {
  args: { entries, detailMap, onIncrement: () => {}, onDecrement: () => {} },
};

export const Empty: Story = {
  args: { entries: [], detailMap: new Map() },
};
