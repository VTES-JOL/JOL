import type { ComponentProps } from 'react';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CommandForm } from './CommandForm';
import { useSubmitGuard } from '../../hooks/useSubmitGuard';
import { useCommandStatus } from './useCommandStatus';
import { api } from '../../api/client';
import { confirmDialog } from '../../stores/dialog';
import { showError } from '../../stores/toast';
import type { GameSnapshot } from '../../api/types';

// Wires CommandForm to the real useSubmitGuard + useCommandStatus hooks, same
// as GamePage does — keeps these tests exercising the actual submitting/guard
// behavior instead of stubbing it out.
function TestHarness(props: Omit<ComponentProps<typeof CommandForm>, 'submitting' | 'guard' | 'captureStatus'>) {
  const { submitting, guard } = useSubmitGuard();
  const { status, captureStatus } = useCommandStatus();
  return (
    <>
      <CommandForm {...props} submitting={submitting} guard={guard} captureStatus={captureStatus} />
      {status && <div>{status}</div>}
    </>
  );
}

vi.mock('../../api/client', () => ({
  api: { post: vi.fn(), put: vi.fn() },
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
    seating: [],
    chat: [],
    commandErrors: [],
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
    stamp: 1,
    judgeRequest: null,
    ...overrides,
  };
}

beforeEach(() => {
  vi.mocked(api.post).mockReset();
  vi.mocked(confirmDialog).mockReset();
  vi.mocked(showError).mockReset();
});

describe('CommandForm', () => {
  it('submits the command (+ ping) and clears the field', async () => {
    const updated = makeGame({ phase: 'Master' });
    vi.mocked(api.post).mockResolvedValue(updated);
    const onUpdated = vi.fn();
    const user = userEvent.setup();
    render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={onUpdated} />);

    const input = screen.getByPlaceholderText('Enter game commands');
    await user.type(input, 'burn library 1');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    // Phase moved to the HUD stepper (C4); chat to ChatCompose (D24).
    expect(api.post).toHaveBeenCalledWith(
      '/game/g1/view/submit',
      { phase: null, command: 'burn library 1', chat: null, ping: null },
      { 'X-Submit-Id': expect.any(String) },
    );
    expect(onUpdated).toHaveBeenCalledWith(updated);
    expect(input).toHaveValue('');
  });

  it('shows a toast and stops submitting on API failure', async () => {
    vi.mocked(api.post).mockRejectedValue(new Error('boom'));
    const user = userEvent.setup();
    render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={vi.fn()} />);

    await user.type(screen.getByPlaceholderText('Enter game commands'), 'burn library 1');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    expect(await screen.findByRole('button', { name: 'Submit' })).toBeEnabled();
    expect(showError).toHaveBeenCalledWith('Failed to submit.');
  });

  it('ends the turn only after the confirm dialog resolves true', async () => {
    vi.mocked(confirmDialog).mockResolvedValue(false);
    const user = userEvent.setup();
    render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={vi.fn()} />);

    await user.click(screen.getByRole('button', { name: 'End Turn' }));
    expect(api.post).not.toHaveBeenCalled();

    vi.mocked(confirmDialog).mockResolvedValue(true);
    vi.mocked(api.post).mockResolvedValue(makeGame());
    await user.click(screen.getByRole('button', { name: 'End Turn' }));

    expect(api.post).toHaveBeenCalledWith('/game/g1/view/end-turn');
  });

  it("disables End Turn when it is not the viewer's turn", () => {
    render(<TestHarness gameId="g1" game={makeGame({ currentPlayer: 'Player2' })} viewerName="Player1" onUpdated={vi.fn()} />);
    expect(screen.getByRole('button', { name: 'End Turn' })).toBeDisabled();
  });

  it('renders nothing for a viewer who cannot play', () => {
    const { container } = render(
      <TestHarness gameId="g1" game={makeGame({ player: false, judge: true })} viewerName="Judge1" onUpdated={vi.fn()} />,
    );
    expect(container.querySelector('#commandForm')).toBeNull();
  });

  it("keeps a rejected command's status visible across a stale game-prop refresh", async () => {
    // Regression: GET /view always returns status: null, so the message must be
    // held in local state, not read off `game.status`.
    vi.mocked(api.post).mockResolvedValue(makeGame({ status: 'No amount given use +/-' }));
    const user = userEvent.setup();
    const { rerender } = render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={vi.fn()} />);

    await user.type(screen.getByPlaceholderText('Enter game commands'), 'vp');
    await user.click(screen.getByRole('button', { name: 'Submit' }));

    expect(await screen.findByText('No amount given use +/-')).toBeInTheDocument();

    rerender(<TestHarness gameId="g1" game={makeGame({ status: null })} viewerName="Player1" onUpdated={vi.fn()} />);
    expect(screen.getByText('No amount given use +/-')).toBeInTheDocument();
  });
});
