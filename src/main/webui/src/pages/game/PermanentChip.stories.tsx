import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { PermanentChip } from './PermanentChip';
import { card } from './__fixtures__/gameFixtures';
import { withTileGrid } from './__fixtures__/decorators';

// A non-minion card in READY — master, location or powerbase (Powerbase:
// Montreal, Information Highway, Veil of Darkness). A compact one-line chip, not
// a MinionTile: no capacity ring, no discipline row.
const meta = {
  title: 'Game/Board/PermanentChip',
  component: PermanentChip,
  decorators: [withTileGrid(2)],
  args: { region: 'READY', coordinate: '3', onAction: fn(), onQuick: fn() },
} satisfies Meta<typeof PermanentChip>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Location: Story = {
  args: { card: card('Information Highway', { typeClass: 'master', minion: false }) },
};

export const Powerbase: Story = {
  args: { card: card('Powerbase: Montreal', { typeClass: 'master', minion: false, counters: 0 }) },
};

export const Locked: Story = {
  args: { card: card('Dreams of the Sphinx', { typeClass: 'master', minion: false, locked: true, counters: 2 }) },
};

export const Contested: Story = {
  args: { card: card('Anarch Free Press, The', { typeClass: 'master', minion: false, contested: true }) },
};

export const WithLabelAndCounters: Story = {
  args: { card: card('The Rack', { typeClass: 'master', minion: false, counters: 4, label: 'shared' }) },
};
