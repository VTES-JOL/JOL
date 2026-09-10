import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { NotesToggleButton } from './NotesToggleButton';

// The HUD "Notes" control that opens the notes & deck drawer. A grey dot means
// notes have content; a pulsing accent dot means the global notes just changed.
const meta = {
  title: 'Game/HUD/NotesToggleButton',
  component: NotesToggleButton,
  parameters: { layout: 'centered' },
  args: { onClick: fn() },
} satisfies Meta<typeof NotesToggleButton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Plain: Story = { args: { indicator: 'none' } };
export const HasContent: Story = { args: { indicator: 'content' } };
export const JustChanged: Story = { args: { indicator: 'update' } };
export const CompactChanged: Story = { args: { indicator: 'update', compact: true } };
