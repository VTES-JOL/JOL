import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { JudgeRequestModal } from './JudgeRequestModal';
import type { JudgeRequestSnapshot } from '../../api/types';

// The "Call Judge" form / open-request view behind the HUD button. `request`
// null → the create form; a request → the viewer-aware view (edit / retract /
// resolve gated by the can* flags). Submits go through the API, so buttons are
// inert in isolation.
const meta = {
  title: 'Game/Modals/JudgeRequestModal',
  component: JudgeRequestModal,
  parameters: { layout: 'fullscreen' },
  args: {
    gameId: 'g1',
    onUpdated: fn(),
    onClose: fn(),
    submitting: false,
    guard: (async (run: () => Promise<unknown>) => run()) as never,
  },
} satisfies Meta<typeof JudgeRequestModal>;

export default meta;
type Story = StoryObj<typeof meta>;

const openRequest: JudgeRequestSnapshot = {
  id: 7,
  requester: 'Player2',
  category: 'INCORRECT_PLAY',
  createdAt: new Date(Date.now() - 5 * 60_000).toISOString(),
  updatedAt: new Date(Date.now() - 5 * 60_000).toISOString(),
  details: 'Player1 bled with [card:200812:Lucita, Anarch Sympathizer] while locked.',
  rawDetails: 'Player1 bled with [Lucita, Anarch Sympathizer] while locked.',
  status: 'OPEN',
  canEdit: false,
  canRetract: false,
  canResolve: false,
};

export const CreateForm: Story = {
  args: { request: null },
};

export const OpenRequestBystander: Story = {
  args: { request: openRequest },
};

export const OpenRequestRequester: Story = {
  args: { request: { ...openRequest, canEdit: true, canRetract: true } },
};

export const OpenRequestJudge: Story = {
  args: { request: { ...openRequest, canResolve: true } },
};
