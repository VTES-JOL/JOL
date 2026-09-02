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
    judgeRequest: null,
    ...overrides,
  };
}

beforeEach(() => {
  vi.mocked(api.post).mockReset();
  vi.mocked(api.put).mockReset();
  vi.mocked(confirmDialog).mockReset();
  vi.mocked(showError).mockReset();
});

const openRequest: NonNullable<GameSnapshot['judgeRequest']> = {
  id: 7,
  requester: 'Player1',
  category: 'CARD_RULING',
  createdAt: '2026-09-02T10:00:00Z',
  updatedAt: '2026-09-02T10:00:00Z',
  details: 'question about [card:100:Fame]',
  rawDetails: 'question about [Fame]',
  status: 'OPEN',
  canEdit: false,
  canRetract: false,
  canResolve: false,
};

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

  describe('call a judge', () => {
    it('lets a seated player raise a request from the Call Judge button', async () => {
      const updated = makeGame({ judgeRequest: { ...openRequest } });
      vi.mocked(api.post).mockResolvedValue(updated);
      const onUpdated = vi.fn();
      const user = userEvent.setup();
      render(<TestHarness gameId="g1" game={makeGame()} viewerName="Player1" onUpdated={onUpdated} />);

      await user.click(screen.getByRole('button', { name: /Call Judge/ }));
      await user.selectOptions(screen.getByLabelText('Type of request'), 'CARD_RULING');
      await user.type(screen.getByLabelText(/What do you need a ruling on/), 'Does Fame trigger?');
      await user.click(screen.getByRole('button', { name: 'Call judge' }));

      expect(api.post).toHaveBeenCalledWith('/game/g1/judge-request', {
        category: 'CARD_RULING',
        details: 'Does Fame trigger?',
      });
      expect(onUpdated).toHaveBeenCalledWith(updated);
    });

    it('shows "Judge Called" and a read-only request for a non-requester', async () => {
      const user = userEvent.setup();
      render(
        <TestHarness
          gameId="g1"
          game={makeGame({ judgeRequest: { ...openRequest, requester: 'Player2' } })}
          viewerName="Player1"
          onUpdated={vi.fn()}
        />,
      );

      await user.click(screen.getByRole('button', { name: /Judge Called/ }));
      expect(screen.getByText(/called by/)).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Save changes' })).not.toBeInTheDocument();
      expect(screen.queryByLabelText('Resolution notes')).not.toBeInTheDocument();
    });

    it('lets the requester edit their open request', async () => {
      const updated = makeGame({ judgeRequest: { ...openRequest } });
      vi.mocked(api.put).mockResolvedValue(updated);
      const user = userEvent.setup();
      render(
        <TestHarness
          gameId="g1"
          game={makeGame({ judgeRequest: { ...openRequest, canEdit: true, canRetract: true } })}
          viewerName="Player1"
          onUpdated={vi.fn()}
        />,
      );

      await user.click(screen.getByRole('button', { name: /Judge Called/ }));
      const details = screen.getByLabelText(/What do you need a ruling on/);
      expect(details).toHaveValue('question about [Fame]');
      await user.clear(details);
      await user.type(details, 'clearer question');
      await user.click(screen.getByRole('button', { name: 'Save changes' }));

      expect(api.put).toHaveBeenCalledWith('/game/g1/judge-request', {
        category: 'CARD_RULING',
        details: 'clearer question',
      });
    });

    it('offers a resolution box to a non-seated judge', async () => {
      const updated = makeGame();
      vi.mocked(api.post).mockResolvedValue(updated);
      const user = userEvent.setup();
      render(
        <TestHarness
          gameId="g1"
          game={makeGame({ player: false, judge: true, judgeRequest: { ...openRequest, canResolve: true } })}
          viewerName="Judge1"
          onUpdated={vi.fn()}
        />,
      );

      await user.click(screen.getByRole('button', { name: /Judge Called/ }));
      await user.type(screen.getByLabelText('Resolution notes'), 'Ruling: yes, it triggers.');
      await user.click(screen.getByRole('button', { name: 'Resolve request' }));

      expect(api.post).toHaveBeenCalledWith('/game/g1/judge-request/resolve', {
        notes: 'Ruling: yes, it triggers.',
      });
    });
  });
});
