import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { CardContextMenu } from './CardContextMenu';
import { minion, card, tableCtx } from './__fixtures__/gameFixtures';

// The desktop click / right-click card menu (portals to body, point-anchored).
// Leads with the phase-aware "Likely now" set; the full grouped list is behind
// "More actions…". Counter stepper, blood transfer, label and the clan/path/
// sect editor fold in as conditional rows. Below md it becomes CardActionSheet.
const meta = {
  title: 'Game/Menus/CardContextMenu',
  component: CardContextMenu,
  parameters: { layout: 'fullscreen' },
  decorators: [(Story) => <div className="h-[520px]"><Story /></div>],
  args: {
    anchor: { x: 80, y: 60 },
    viewerName: 'Player1',
    onSubmit: fn(),
    onCounterBump: fn(),
    onRequestTarget: fn(),
    onClose: fn(),
  },
} satisfies Meta<typeof CardContextMenu>;

export default meta;
type Story = StoryObj<typeof meta>;

export const OwnReadyMinionMinionPhase: Story = {
  args: { ctx: tableCtx(minion('Lucita, Anarch Sympathizer', { counters: 5 })), phase: 'Minion' },
};

export const OwnReadyMinionInfluencePhase: Story = {
  args: { ctx: tableCtx(minion('Cristo', { counters: 2 })), phase: 'Influence' },
};

export const OpponentMinion: Story = {
  args: {
    ctx: tableCtx(minion('Anson', { counters: 3, owner: 'Player2' }), {
      controller: 'Player2',
      controlledByViewer: false,
    }),
    phase: 'Minion',
  },
};

export const LockedContested: Story = {
  args: { ctx: tableCtx(minion('Muaziz, Archon of Ulugh Beg', { counters: 4, locked: true, contested: true })), phase: 'Minion' },
};

export const AshHeapCard: Story = {
  args: {
    ctx: tableCtx(card('Govern the Unaligned'), { regionType: 'ASH_HEAP', regionCommandKey: 'ashheap', card: card('Govern the Unaligned') }),
    phase: 'Master',
  },
};
