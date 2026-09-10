import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { CommandForm } from './CommandForm';
import { gameSnapshot } from './__fixtures__/gameFixtures';

// The command band — quick-command · input · Submit · End Turn. Seated players
// only (renders nothing otherwise). End Turn is enabled only on your turn.
// Submissions hit the API, so they are inert in isolation.
const meta = {
  title: 'Game/CommandForm',
  component: CommandForm,
  parameters: { layout: 'padded' },
  decorators: [(Story) => <div className="w-[560px] max-w-full rounded border border-line-accent bg-panel/40 p-2"><Story /></div>],
  args: {
    gameId: 'g1',
    viewerName: 'Player1',
    onUpdated: fn(),
    captureStatus: ((u: unknown) => u) as never,
    submitting: false,
    guard: (async (run: () => Promise<unknown>) => run()) as never,
  },
} satisfies Meta<typeof CommandForm>;

export default meta;
type Story = StoryObj<typeof meta>;

export const YourTurn: Story = {
  args: { game: gameSnapshot({ currentPlayer: 'Player1' }) },
};

export const OpponentTurn: Story = {
  args: { game: gameSnapshot({ currentPlayer: 'Player2' }) },
};

export const Submitting: Story = {
  args: { game: gameSnapshot({ currentPlayer: 'Player1' }), submitting: true },
};

export const SpectatorRendersNothing: Story = {
  args: { game: gameSnapshot({ player: false }), viewerName: 'Watcher' },
};
