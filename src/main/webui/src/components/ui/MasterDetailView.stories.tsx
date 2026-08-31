import type { Meta, StoryObj } from '@storybook/react-vite';
import { MasterDetailView } from './MasterDetailView';

const meta = {
  title: 'Components/MasterDetailView',
  component: MasterDetailView,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof MasterDetailView>;

export default meta;
type Story = StoryObj<typeof meta>;

const panel = (label: string, body: string) => (
  <div className="flex-1 min-h-0 overflow-y-auto p-4 bg-surface border border-line rounded-lg">
    <h3 className="text-ink font-semibold mb-2">{label}</h3>
    <p className="text-ink-muted text-sm">{body}</p>
  </div>
);

export const TwoColumn: Story = {
  args: {
    columns: '260px 1fr',
    panels: [
      { key: 'list', label: 'Decks', content: panel('Decks', 'The master list. Collapses to a dropdown below the md breakpoint.') },
      { key: 'detail', label: 'Editor', content: panel('Editor', 'The detail pane. Fills the remaining width on desktop.') },
    ],
  },
};

export const ThreeColumn: Story = {
  args: {
    columns: '240px 1fr 260px',
    breakpoint: 'lg',
    panels: [
      { key: 'list', label: 'Rounds', content: panel('Rounds', 'Left rail.') },
      { key: 'table', label: 'Table', content: panel('Table', 'Center.') },
      { key: 'notes', label: 'Notes', content: panel('Notes', 'Right rail.') },
    ],
  },
};
