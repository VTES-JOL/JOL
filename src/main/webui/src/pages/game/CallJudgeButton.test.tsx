import type { ComponentProps } from 'react';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CallJudgeButton } from './CallJudgeButton';
import { useSubmitGuard } from '../../hooks/useSubmitGuard';
import { api } from '../../api/client';
import type { GameSnapshot } from '../../api/types';

function TestHarness(props: Omit<ComponentProps<typeof CallJudgeButton>, 'submitting' | 'guard'>) {
  const { submitting, guard } = useSubmitGuard();
  return <CallJudgeButton {...props} submitting={submitting} guard={guard} />;
}

vi.mock('../../api/client', () => ({ api: { post: vi.fn(), put: vi.fn() } }));

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
    phases: [],
    turns: [],
    pingOptions: [],
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

beforeEach(() => {
  vi.mocked(api.post).mockReset();
  vi.mocked(api.put).mockReset();
});

describe('CallJudgeButton', () => {
  it('renders nothing for a bare spectator with no open request', () => {
    const { container } = render(<TestHarness gameId="g1" game={makeGame({ player: false })} onUpdated={vi.fn()} />);
    expect(container).toBeEmptyDOMElement();
  });

  it('lets a seated player raise a request', async () => {
    const updated = makeGame({ judgeRequest: { ...openRequest } });
    vi.mocked(api.post).mockResolvedValue(updated);
    const onUpdated = vi.fn();
    const user = userEvent.setup();
    render(<TestHarness gameId="g1" game={makeGame()} onUpdated={onUpdated} />);

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
        onUpdated={vi.fn()}
      />,
    );

    await user.click(screen.getByRole('button', { name: /Judge Called/ }));
    expect(screen.getByText(/called by/)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Save changes' })).not.toBeInTheDocument();
  });

  it('offers a resolution box to a non-seated judge', async () => {
    vi.mocked(api.post).mockResolvedValue(makeGame());
    const user = userEvent.setup();
    render(
      <TestHarness
        gameId="g1"
        game={makeGame({ player: false, judge: true, judgeRequest: { ...openRequest, canResolve: true } })}
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
