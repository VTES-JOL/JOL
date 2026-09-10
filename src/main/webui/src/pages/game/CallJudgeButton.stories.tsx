import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { CallJudgeButton } from './CallJudgeButton';
import { gameSnapshot } from './__fixtures__/gameFixtures';
import type { JudgeRequestSnapshot } from '../../api/types';

// The HUD pill + its request modal. "Call Judge" normally; "Judge Called"
// (pulsing red) when game.judgeRequest is set. Hidden for a plain spectator
// with no open request.
const meta = {
  title: 'Game/HUD/CallJudgeButton',
  component: CallJudgeButton,
  parameters: { layout: 'centered' },
  args: {
    gameId: 'g1',
    onUpdated: fn(),
    submitting: false,
    guard: (async (run: () => Promise<unknown>) => run()) as never,
  },
} satisfies Meta<typeof CallJudgeButton>;

export default meta;
type Story = StoryObj<typeof meta>;

const openRequest: JudgeRequestSnapshot = {
  id: 3,
  requester: 'Player2',
  category: 'CARD_RULING',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  details: 'Does [card:100730:Carrion Crows] stay in play after the acting minion leaves combat?',
  rawDetails: null,
  status: 'OPEN',
  canEdit: false,
  canRetract: false,
  canResolve: false,
};

export const NoRequest: Story = {
  args: { game: gameSnapshot() },
};

export const JudgeCalled: Story = {
  args: { game: gameSnapshot({ judgeRequest: openRequest }) },
};

export const SpectatorHidden: Story = {
  name: 'Spectator, no request (renders nothing)',
  args: { game: gameSnapshot({ player: false, judge: false }) },
};
