import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { GameChatLog } from './GameChatLog';
import type { ChatData } from '../../api/types';

function line(over: Partial<ChatData>): ChatData {
  return { timestamp: '3-Feb 14:05', message: 'did a thing.', source: 'Player1', ...over };
}

describe('GameChatLog', () => {
  const lines: ChatData[] = [
    line({ message: 'START OF UNLOCK PHASE.', source: 'Player1' }),
    line({ message: 'transfers 1 blood.', invocation: 'transfer inactive 3 +1', invocationBy: 'Judge1' }),
    line({ message: 'transfers 1 blood.', invocation: 'transfer inactive 3 +1', invocationBy: 'Judge1' }),
    line({ message: 'influences out a card.', invocation: 'influence 3', invocationBy: 'Judge1' }),
  ];

  it('never shows command context when showCommands is off', () => {
    render(<GameChatLog lines={lines} viewerName="Player1" showCommands={false} />);
    expect(screen.queryByText('transfer inactive 3 +1')).toBeNull();
    expect(screen.queryByText('influence 3')).toBeNull();
  });

  it('shows one command header per consecutive run when showCommands is on', () => {
    render(<GameChatLog lines={lines} viewerName="Player1" showCommands />);
    // Two lines share the same invocation -> one header, not two.
    expect(screen.getAllByText('transfer inactive 3 +1')).toHaveLength(1);
    expect(screen.getAllByText('influence 3')).toHaveLength(1);
    expect(screen.getAllByText('Judge1')).toHaveLength(2);
  });

  it('shows a header per submission when invocationSeq distinguishes identical text', () => {
    // Five separate `transfer ready 1 -1` submissions: same text, distinct seq.
    const repeated: ChatData[] = [1, 2, 3, 4, 5].map((seq) =>
      line({
        message: 'transferred 1 blood.',
        invocation: 'transfer ready 1 -1',
        invocationBy: 'Player1',
        invocationSeq: seq,
      }),
    );
    render(<GameChatLog lines={repeated} viewerName="Player1" showCommands />);
    expect(screen.getAllByText('transfer ready 1 -1')).toHaveLength(5);
  });

  it('shows one header for a multi-line single submission (shared invocationSeq)', () => {
    const oneCommand: ChatData[] = [
      line({ message: 'plays a card.', invocation: 'play hand 1 draw', invocationBy: 'Player1', invocationSeq: 9 }),
      line({ message: 'draws from their library.', invocation: 'play hand 1 draw', invocationBy: 'Player1', invocationSeq: 9 }),
    ];
    render(<GameChatLog lines={oneCommand} viewerName="Player1" showCommands />);
    expect(screen.getAllByText('play hand 1 draw')).toHaveLength(1);
  });

  it('leaves lines without an invocation unprefixed', () => {
    render(<GameChatLog lines={[line({ message: 'plain chat' })]} viewerName={null} showCommands />);
    expect(screen.getByText('plain chat')).toBeInTheDocument();
    expect(document.querySelector('.chat-command')).toBeNull();
  });

  const errors = [
    { timestamp: '3-Feb 14:06', player: 'ShanDow', command: 'blodo ShanDow ready 2 -1', error: 'Unknown command' },
  ];

  it('shows mistyped attempts only when showCommands is on', () => {
    const { rerender } = render(
      <GameChatLog lines={lines} viewerName={null} showCommands={false} errors={errors} />,
    );
    expect(screen.queryByText(/blodo ShanDow/)).toBeNull();

    rerender(<GameChatLog lines={lines} viewerName={null} showCommands errors={errors} />);
    expect(screen.getByText('blodo ShanDow ready 2 -1')).toBeInTheDocument();
    expect(screen.getByText(/Unknown command/)).toBeInTheDocument();
  });

  it('interleaves an attempt by timestamp', () => {
    render(
      <GameChatLog
        lines={[line({ message: 'first', timestamp: '3-Feb 14:05' }), line({ message: 'last', timestamp: '3-Feb 14:07' })]}
        viewerName={null}
        showCommands
        errors={[{ timestamp: '3-Feb 14:06', player: 'ShanDow', command: 'oops' }]}
      />,
    );
    const text = document.body.textContent ?? '';
    expect(text.indexOf('first')).toBeLessThan(text.indexOf('oops'));
    expect(text.indexOf('oops')).toBeLessThan(text.indexOf('last'));
  });
});
