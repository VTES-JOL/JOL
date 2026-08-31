import type { ComponentProps } from 'react';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CommandForm } from './CommandForm';
import { useSubmitGuard } from '../../hooks/useSubmitGuard';
import { api } from '../../api/client';
import { confirmDialog } from '../../stores/dialog';
import { showError } from '../../stores/toast';
import type { GameSnapshot } from '../../api/types';

// Wires CommandForm to the real useSubmitGuard hook, same as GamePage does —
// keeps these tests exercising the actual submitting/guard behavior instead
// of stubbing it out.
function TestHarness(props: Omit<ComponentProps<typeof CommandForm>, 'submitting' | 'guard'>) {
  const { submitting, guard } = useSubmitGuard();
  return <CommandForm {...props} submitting={submitting} guard={guard} />;
}

vi.mock('../../api/client', () => ({
  api: { post: vi.fn() },
}));

vi.mock('../../stores/dialog', () => ({
  confirmDialog: vi.fn(),
}));

vi.mock('../../stores/toast', () => ({
  showError: vi.fn(),
}));

function makeGame(overrides: Partial<GameSnapshot> = {}): GameSnapshot {
  return {
    id: 'g1',
    name: 'Test Game',
    players: [],
    currentPlayer: 'Player1',
    edgePlayer: 'Player1',
    turn: '1',
    turnLabel: 'Turn 1',
    phase: 'Untap',
    phases: ['Untap', 'Master', 'Minion', 'Influence'],
    turns: [],
    pingOptions: ['Player2'],
    player: true,
    admin: false,
    judge: false,
    globalNotes: null,
    privateNotes: null,
    edgeColor: '#fff',
    edgeTextColor: 'black',
    status: null,
    stamp: '1',
    ...overrides,
  };
}

beforeEach(() => {
  vi.mocked(api.post).mockReset();
  vi.mocked(confirmDialog).mockReset();
  vi.mocked(showError).mockReset();
});

describe('CommandForm', () => {
  it('submits phase/command/chat/ping together and clears the free-text fields', async () => {
    const updated = makeGame({ phase: 'Master' });
    vi.mocked(api.post).mockResolvedValue(updated);
    const onUpdated = vi.fn();
    const user = userEvent.setup();
    render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={onUpdated} />);

    await user.type(screen.getByLabelText('Command'), 'burn library 1');
    await user.type(screen.getByLabelText('Chat'), 'hello');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    expect(api.post).toHaveBeenCalledWith('/game/g1/view/submit', {
      phase: 'Untap',
      command: 'burn library 1',
      chat: 'hello',
      ping: null,
    });
    expect(onUpdated).toHaveBeenCalledWith(updated);
    expect(screen.getByLabelText('Command')).toHaveValue('');
    expect(screen.getByLabelText('Chat')).toHaveValue('');
  });

  it('shows a toast and stops submitting on API failure', async () => {
    vi.mocked(api.post).mockRejectedValue(new Error('boom'));
    const user = userEvent.setup();
    render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={vi.fn()} />);

    await user.type(screen.getByLabelText('Chat'), 'hello');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    expect(await screen.findByRole('button', { name: 'Submit' })).toBeEnabled();
    expect(showError).toHaveBeenCalledWith('Failed to submit.');
  });

  it('ends the turn only after the confirm dialog resolves true', async () => {
    vi.mocked(confirmDialog).mockResolvedValue(false);
    const onUpdated = vi.fn();
    const user = userEvent.setup();
    render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={onUpdated} />);

    await user.click(screen.getByRole('button', { name: 'End Turn' }));
    expect(api.post).not.toHaveBeenCalled();

    vi.mocked(confirmDialog).mockResolvedValue(true);
    vi.mocked(api.post).mockResolvedValue(makeGame());
    await user.click(screen.getByRole('button', { name: 'End Turn' }));

    expect(api.post).toHaveBeenCalledWith('/game/g1/view/end-turn');
  });

  it('disables End Turn and the Phase select when it is not the viewer\'s turn', () => {
    render(<TestHarness gameId="g1" game={makeGame({ currentPlayer: 'Player2' })} viewerName="Player1" onUpdated={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'End Turn' })).toBeDisabled();
    expect(screen.getByLabelText('Phase')).toBeDisabled();
  });

  it('hides player-only controls (Phase/Command/Ping/End Turn) for a judge who cannot play', () => {
    render(
      <TestHarness gameId="g1" game={makeGame({ player: false, judge: true })} viewerName="Judge1" onUpdated={vi.fn()} />,
    );

    expect(screen.queryByLabelText('Phase')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Command')).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'End Turn' })).not.toBeInTheDocument();
    expect(screen.getByLabelText('Chat')).toBeEnabled();
  });

  it('disables chat entirely for a spectator (neither player nor judge)', () => {
    render(<TestHarness gameId="g1" game={makeGame({ player: false })} viewerName="Spectator" onUpdated={vi.fn()} />);

    expect(screen.getByLabelText('Chat')).toBeDisabled();
  });

  it('shows a rejected command\'s status message and keeps it visible across a stale game-prop refresh', async () => {
    // Regression test for the race this was fixed for: GameStateResource's
    // GET /view (what any WebSocket-triggered refresh re-fetches, including
    // the one this very submit's own state-save self-triggers) always
    // returns status: null. If the message were read from `game.status`
    // instead of local state, a parent re-render with a fresh-but-stale
    // snapshot — exactly what happens here — would erase it before anyone
    // could read it.
    vi.mocked(api.post).mockResolvedValue(makeGame({ status: 'No amount given use +/-' }));
    const onUpdated = vi.fn();
    const user = userEvent.setup();
    const { rerender } = render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={onUpdated} />);

    await user.type(screen.getByLabelText('Command'), 'vp');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    expect(await screen.findByText('No amount given use +/-')).toBeInTheDocument();

    rerender(<TestHarness gameId="g1" game={makeGame({ status: null })} viewerName="Player1" onUpdated={onUpdated} />);

    expect(screen.getByText('No amount given use +/-')).toBeInTheDocument();
  });
});
