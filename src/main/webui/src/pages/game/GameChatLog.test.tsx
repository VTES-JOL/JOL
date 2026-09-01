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

  it('leaves lines without an invocation unprefixed', () => {
    render(<GameChatLog lines={[line({ message: 'plain chat' })]} viewerName={null} showCommands />);
    expect(screen.getByText('plain chat')).toBeInTheDocument();
    expect(document.querySelector('.chat-command')).toBeNull();
  });
});
