import type { Meta, StoryObj } from '@storybook/react-vite';
import type { CardDetail } from '../../api/types';
import type { DeckEntry } from '../../components/ui/deckKit';
import { DeckAnalyticsPanel } from './DeckAnalyticsPanel';

const entries: DeckEntry[] = [
  { cardId: 'v1', name: 'Anson', count: 4, isCrypt: true, types: ['Vampire'], group: '2', banned: false },
  { cardId: 'v2', name: 'Ingrid Russo', count: 4, isCrypt: true, types: ['Vampire'], group: '2', banned: false },
  { cardId: 'v3', name: 'Lolita Houston', count: 3, isCrypt: true, types: ['Vampire'], group: '2', banned: false },
  { cardId: 'v4', name: 'Nadia', count: 1, isCrypt: true, types: ['Vampire'], group: '2', banned: false },
  { cardId: 'l1', name: 'Govern the Unaligned', count: 10, isCrypt: false, types: ['Action'], banned: false },
  { cardId: 'l2', name: 'Deflection', count: 12, isCrypt: false, types: ['Reaction'], banned: false },
  { cardId: 'l3', name: 'Aire of Elation', count: 6, isCrypt: false, types: ['Action Modifier'], banned: false },
  { cardId: 'l4', name: 'Villein', count: 8, isCrypt: false, types: ['Master'], banned: false },
  { cardId: 'l5', name: 'Carrion Crows', count: 4, isCrypt: false, types: ['Action', 'Combat'], banned: false },
];

const d = (o: Partial<CardDetail>): CardDetail => ({
  id: '', name: '', crypt: false, types: [], group: null, banned: false, advanced: false, sets: [],
  clan: null, path: null, capacity: null, disciplines: [],
  andDisciplines: [], orDisciplines: [], requirementClans: [], requirementPath: null, poolCost: null, bloodCost: null,
  ...o,
});

const detailMap = new Map<string, CardDetail>([
  ['v1', d({ id: 'v1', crypt: true, clan: 'Ventrue', capacity: 8, disciplines: ['DOM', 'for', 'PRE'] })],
  ['v2', d({ id: 'v2', crypt: true, clan: 'Ventrue', capacity: 7, disciplines: ['dom', 'FOR', 'pre'] })],
  ['v3', d({ id: 'v3', crypt: true, clan: 'Malkavian', capacity: 4, disciplines: ['AUS', 'dom', 'obf'] })],
  ['v4', d({ id: 'v4', crypt: true, clan: 'Ventrue', capacity: 3, disciplines: ['dom', 'pre'] })],
  ['l1', d({ id: 'l1', types: ['Action'], orDisciplines: ['dom'], poolCost: null })],
  ['l2', d({ id: 'l2', types: ['Reaction'], orDisciplines: ['dom'] })],
  ['l3', d({ id: 'l3', types: ['Action Modifier'], orDisciplines: ['pre'] })],
  ['l4', d({ id: 'l4', types: ['Master'], poolCost: 1 })],
  ['l5', d({ id: 'l5', types: ['Action', 'Combat'], andDisciplines: ['ani'], bloodCost: 1 })],
]);

const meta = {
  title: 'Deck/DeckAnalyticsPanel',
  component: DeckAnalyticsPanel,
  parameters: { layout: 'centered' },
  decorators: [
    (Story) => (
      <div className="jt:w-[340px] jt:h-[620px] jt:flex jt:flex-col">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof DeckAnalyticsPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

export const WithDeck: Story = { args: { entries, detailMap } };
export const Empty: Story = { args: { entries: [], detailMap: new Map() } };
