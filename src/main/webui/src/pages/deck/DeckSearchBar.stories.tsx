import type { Meta, StoryObj } from '@storybook/react-vite';
import { useState } from 'react';
import type { CardDetail } from '../../api/types';
import { DeckSearchBar } from './DeckSearchBar';

// Rendered through a stateful harness (search + add feedback), so stories
// carry no args of their own.

const CARDS: CardDetail[] = [
  {
    id: '101228',
    name: 'Govern the Unaligned',
    crypt: false,
    types: ['Action'],
    group: null,
    banned: false,
    advanced: false,
    sets: ['DS'],
    clan: null,
    path: null,
    capacity: null,
    disciplines: [],
    andDisciplines: [],
    orDisciplines: ['dom'],
    requirementClans: [],
    requirementPath: null,
    poolCost: null,
    bloodCost: null,
  },
  {
    id: '200001',
    name: 'Aabbt Kindred',
    crypt: true,
    types: ['Vampire'],
    group: '2',
    banned: false,
    advanced: false,
    sets: ['FN'],
    clan: 'Follower of Set',
    path: null,
    capacity: 4,
    disciplines: ['for', 'pre', 'ser'],
    andDisciplines: [],
    orDisciplines: [],
    requirementClans: [],
    requirementPath: null,
    poolCost: null,
    bloodCost: null,
  },
  {
    id: '100730',
    name: 'Carrion Crows',
    crypt: false,
    types: ['Action', 'Combat'],
    group: null,
    banned: false,
    advanced: false,
    sets: ['DS'],
    clan: null,
    path: null,
    capacity: null,
    disciplines: [],
    andDisciplines: ['ani'],
    orDisciplines: [],
    requirementClans: [],
    requirementPath: null,
    poolCost: null,
    bloodCost: null,
  },
];

function Harness() {
  const [added, setAdded] = useState<string[]>([]);
  return (
    <div className="jt:w-[360px] jt:bg-surface jt:border jt:border-line/60 jt:rounded-lg">
      <DeckSearchBar
        onSearch={async (q) => {
          await new Promise((r) => setTimeout(r, 250));
          return CARDS.filter((c) => c.name.toLowerCase().includes(q.toLowerCase()));
        }}
        onAddCard={(c) => setAdded((a) => [...a, c.name])}
      />
      <div className="jt:px-3 jt:py-2 jt:text-xs jt:text-ink-muted">
        Added: {added.length ? added.join(', ') : '—'}
      </div>
    </div>
  );
}

const meta: Meta = {
  title: 'Deck/DeckSearchBar',
  parameters: { layout: 'centered' },
  render: () => <Harness />,
};

export default meta;
type Story = StoryObj;

export const Default: Story = {};
