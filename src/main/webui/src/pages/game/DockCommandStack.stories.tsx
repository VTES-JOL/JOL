import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { DockCommandStack } from './DockCommandStack';
import { gameSnapshot, pendingAction } from './__fixtures__/gameFixtures';

// The command-controls assembly shared by the wide dock and the mobile Act
// sheet: PendingActionBar + ActQuickBar + CommandForm (+ Call Judge on the
// sheet), in two arrangements. Submissions hit the API — inert in isolation.
const base = gameSnapshot({ currentPlayer: 'Player1' });
const meta = {
  title: 'Game/DockCommandStack',
  component: DockCommandStack,
  parameters: { layout: 'padded' },
  decorators: [(Story) => <div className="w-[520px] max-w-full rounded border border-line-accent bg-panel/30 p-2"><Story /></div>],
  args: {
    game: base,
    gameId: 'g1',
    viewerName: 'Player1',
    me: base.players[0],
    isMyTurn: true,
    pendingActionable: false,
    onCommand: fn(),
    onUpdated: fn(),
    captureStatus: ((u: unknown) => u) as never,
    submitting: false,
    guard: (async (run: () => Promise<unknown>) => run()) as never,
    onRespondFocus: fn(),
  },
} satisfies Meta<typeof DockCommandStack>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Dock: Story = {
  args: { variant: 'dock' },
};

export const DockWithPendingResponse: Story = {
  args: {
    variant: 'dock',
    pendingActionable: true,
    game: gameSnapshot({ currentPlayer: 'Player1', pendingAction: pendingAction({ actor: 'Player2', awaiting: ['Player1'] }) }),
  },
};

export const Sheet: Story = {
  args: { variant: 'sheet' },
};

export const SheetJudge: Story = {
  args: { variant: 'sheet', game: gameSnapshot({ judge: true, player: false }), viewerName: 'JudgeAmy' },
};
