import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { TableHud } from './TableHud';
import { gameSnapshot, pendingAction } from './__fixtures__/gameFixtures';

// The persistent turn/phase ribbon: game · turn · active seat · phase stepper ·
// waiting · edge, plus the Call Judge / Notes / History cluster. Also hosts the
// PendingActionBar and the rejected-command message.
//
// Note: the WS store is disconnected in isolation, so the "Reconnecting…" chip
// shows in every story here — it is not part of what each story demonstrates.
const meta = {
  title: 'Game/HUD/TableHud',
  component: TableHud,
  parameters: { layout: 'fullscreen' },
  args: {
    gameId: 'g1',
    viewerName: 'Player1',
    onSubmitPhase: fn(),
    canSubmitPhase: true,
    commandStatus: '',
    onClearStatus: fn(),
    onPendingCommand: fn(),
    onPendingRespond: fn(),
    onUpdated: fn(),
    submitting: false,
    guard: (async (run: () => Promise<unknown>) => run()) as never,
    onOpenNotes: fn(),
    notesIndicator: 'none',
    showHistory: false,
    onToggleHistory: fn(),
  },
} satisfies Meta<typeof TableHud>;

export default meta;
type Story = StoryObj<typeof meta>;

export const YourTurn: Story = {
  args: { game: gameSnapshot({ currentPlayer: 'Player1', phase: 'Master' }) },
};

export const OpponentTurnWaiting: Story = {
  args: {
    viewerName: 'Player1',
    game: gameSnapshot({
      currentPlayer: 'Player2',
      turnLabel: 'Player2 3',
      phase: 'Minion',
      players: gameSnapshot().players.map((p) =>
        p.name === 'Player2' ? { ...p, active: true, lastActionAt: new Date(Date.now() - 3 * 3600_000).toISOString() } : { ...p, active: false },
      ),
    }),
    canSubmitPhase: false,
  },
};

export const EdgeHeld: Story = {
  args: { game: gameSnapshot({ edgePlayer: 'Player1' }) },
};

export const PendingResponse: Story = {
  args: { game: gameSnapshot({ pendingAction: pendingAction({ awaiting: ['Player2'] }) }) },
};

export const RejectedCommand: Story = {
  args: { game: gameSnapshot(), commandStatus: 'Cannot bleed: acting minion is locked.' },
};

export const JudgeView: Story = {
  args: { game: gameSnapshot({ judge: true, player: false }), viewerName: 'JudgeAmy' },
};
