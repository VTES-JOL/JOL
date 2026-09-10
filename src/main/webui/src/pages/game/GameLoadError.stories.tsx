import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { GameLoadError } from './GameLoadError';

// GamePage's failure state — the game id is missing, or GET /game/{id}/view
// errored with nothing cached to fall back on.
const meta = {
  title: 'Game/GameLoadError',
  component: GameLoadError,
  parameters: { layout: 'fullscreen' },
  args: { onRetry: fn(), onBack: fn() },
} satisfies Meta<typeof GameLoadError>;

export default meta;
type Story = StoryObj<typeof meta>;

export const LoadFailed: Story = {
  args: { canRetry: true },
};

export const NoGameId: Story = {
  name: 'No game id (no Try again)',
  args: { canRetry: false },
};
