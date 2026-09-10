import type { Meta, StoryObj } from '@storybook/react-vite';
import { GameChatLog } from './GameChatLog';
import { sampleChat, chatLine } from './__fixtures__/gameFixtures';
import type { CommandError } from '../../api/types';

// The shared chat renderer (live panel + history). Per-actor accent runs,
// SYSTEM lines quieted, date / "New" separators, and — judge only — the raw
// command header per submission plus interleaved failed attempts.
const meta = {
  title: 'Game/Chat/GameChatLog',
  component: GameChatLog,
  parameters: { layout: 'padded' },
  decorators: [
    (Story) => (
      <div className="flex h-[420px] w-[460px] max-w-full flex-col overflow-hidden rounded border border-line-accent">
        <Story />
      </div>
    ),
  ],
  args: { viewerName: 'Player1', seating: ['Player1', 'Player2', 'Player3'] },
} satisfies Meta<typeof GameChatLog>;

export default meta;
type Story = StoryObj<typeof meta>;

export const LiveTurn: Story = {
  args: { lines: sampleChat },
};

export const WithNewDivider: Story = {
  args: {
    lines: sampleChat,
    newSince: sampleChat[3].postedAt,
  },
};

const errors: CommandError[] = [
  {
    timestamp: sampleChat[2].timestamp,
    occurredAt: sampleChat[2].postedAt,
    player: 'Player1',
    command: 'bleed 2',
    error: 'Unknown verb: bleed',
  },
];

export const JudgeCommands: Story = {
  args: {
    showCommands: true,
    errors,
    lines: sampleChat.map((l, i) =>
      i === 5 ? { ...l, invocation: 'lock ready 1; declare bleed', invocationBy: 'Player1', invocationSeq: 1 } : l,
    ),
  },
};

export const LongScrollback: Story = {
  args: {
    lines: [
      ...sampleChat,
      ...Array.from({ length: 40 }, (_, i) =>
        chatLine(i % 2 ? 'Player2' : 'Player3', `line ${i + 1} — chatter about the referendum`),
      ),
    ],
  },
};
