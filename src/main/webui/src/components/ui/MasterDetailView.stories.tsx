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
  <div className="jt:flex-1 jt:min-h-0 jt:overflow-y-auto jt:p-4 jt:bg-surface jt:border jt:border-line jt:rounded-lg">
    <h3 className="jt:text-ink jt:font-semibold jt:mb-2">{label}</h3>
    <p className="jt:text-ink-muted jt:text-sm">{body}</p>
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
