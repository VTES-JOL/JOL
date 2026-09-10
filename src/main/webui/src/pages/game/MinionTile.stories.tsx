import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { MinionTile } from './MinionTile';
import { minion, gun, retainer, bloodStack, faceDownBack } from './__fixtures__/gameFixtures';
import { withTileGrid } from './__fixtures__/decorators';

// The ready / torpor minion tile. Every knob the board exposes — capacity ring,
// disciplines, clan / sect / path glyphs, lock chrome, the inline blood
// stepper, state chips (contested ▸ face-down ▸ locked) and the attached-card
// branch — lives here, so it is the tile to tune board density against.
const meta = {
  title: 'Game/Board/MinionTile',
  component: MinionTile,
  decorators: [withTileGrid(2)],
  args: {
    region: 'READY',
    coordinate: '1',
    onAction: fn(),
    onQuick: fn(),
    onCounter: fn(),
  },
} satisfies Meta<typeof MinionTile>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Ready: Story = {
  args: { card: minion('Muaziz, Archon of Ulugh Beg', { counters: 4 }) },
};

export const Locked: Story = {
  args: { card: minion('Lucita, Anarch Sympathizer', { counters: 5, locked: true }) },
};

export const Contested: Story = {
  args: { card: minion('Anson', { contested: true, counters: 2 }) },
};

export const FaceDownOwn: Story = {
  name: 'Face-down (controller view)',
  args: { card: minion('Nakhthorheb', { faceDown: true, counters: 1 }) },
};

export const WithVotesAndInfernal: Story = {
  args: {
    card: minion('Antón de Concepción', { votes: '2', infernal: true, counters: 3, sect: 'Sabbat', clan: 'Lasombra' }),
  },
};

export const WithAttachments: Story = {
  args: {
    card: minion('Cristo', {
      counters: 2,
      cards: [gun(), retainer(), bloodStack(4)],
    }),
  },
};

export const BigAttachmentStack: Story = {
  args: {
    card: minion('Wall Vampire', {
      counters: 6,
      cards: [gun(), gun({ name: 'Ivory Bow' }), retainer(), retainer({ name: 'Vagabond Mystic' }), bloodStack(2), faceDownBack()],
    }),
  },
};

export const Torpor: Story = {
  args: { region: 'TORPOR', card: minion('Nakhthorheb', { counters: 0, sect: 'Independent', clan: 'Follower of Set' }) },
};

export const Compact: Story = {
  name: 'Compact (uncontrolled)',
  args: { compact: true, region: 'UNCONTROLLED', card: minion('Anvil', { counters: 1 }) },
  decorators: [withTileGrid(3)],
};

export const StaticNoHandlers: Story = {
  name: 'Read-only (spectator)',
  args: { card: minion('Muaziz, Archon of Ulugh Beg', { counters: 4 }), onAction: undefined, onQuick: undefined, onCounter: undefined },
};

export const NarrowColumn: Story = {
  args: { card: minion('Antara', { counters: 3, disciplines: ['AUS', 'CEL', 'OBF', 'dom', 'for', 'pot', 'pre'] }) },
  decorators: [withTileGrid(1)],
};
