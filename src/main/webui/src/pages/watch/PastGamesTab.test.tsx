import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { PastGamesTab } from './PastGamesTab';
import { api } from '../../api/client';
import type { GameHistory } from '../../api/types';

vi.mock('../../api/client', () => ({
  api: { get: vi.fn(), getText: vi.fn() },
}));

const HISTORY: GameHistory[] = [
  {
    name: 'Kindred Clash',
    started: '2026-08-01T18:00:00Z',
    ended: '2026-08-01T20:00:00Z',
    results: [
      { playerName: 'Alice', deckName: 'Malk Toolbox', victoryPoints: 3, gameWin: true },
      { playerName: 'Bob', deckName: 'Weenie Animalism', victoryPoints: 2, gameWin: false },
      { playerName: 'Carol', deckName: '-- no deck name --', victoryPoints: 0, gameWin: false },
    ],
  },
  {
    name: 'Autumn Cup: Round 2 - Table 3',
    started: '2026-09-10T12:00:00Z',
    ended: '2026-09-10T13:30:00Z',
    results: [
      { playerName: 'Dave', deckName: 'Ventrue Grinder', victoryPoints: 1, gameWin: false },
      { playerName: 'Erin', deckName: 'Tremere Wall', victoryPoints: 4, gameWin: true },
    ],
  },
];

function renderTab(history: GameHistory[] = HISTORY) {
  vi.mocked(api.get).mockResolvedValue(history);
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <PastGamesTab />
    </QueryClientProvider>,
  );
}

beforeEach(() => {
  vi.mocked(api.get).mockReset();
});

describe('PastGamesTab', () => {
  it('renders one card per game with a tournament / casual badge', async () => {
    renderTab();
    await waitFor(() => expect(screen.getByText('Kindred Clash')).toBeInTheDocument());
    expect(screen.getByText('Autumn Cup')).toBeInTheDocument();
    expect(screen.getByText(/Round 2 · Table 3/)).toBeInTheDocument();
    expect(screen.getByText('Tournament')).toBeInTheDocument();
    expect(screen.getByText('Casual')).toBeInTheDocument();
    expect(screen.getByText('2 games')).toBeInTheDocument();
  });

  it('marks the game winner and shows a GW pill', async () => {
    renderTab();
    const winnerRow = (await screen.findByText('Alice')).closest('div.grid') as HTMLElement;
    expect(within(winnerRow).getByText('GW')).toBeInTheDocument();
    expect(within(winnerRow).getByText('3 VP')).toBeInTheDocument();
  });

  it('falls back to "no deck recorded" for a blank deck name', async () => {
    renderTab();
    expect(await screen.findByText('no deck recorded')).toBeInTheDocument();
  });

  it('filters by player, deck or game name', async () => {
    const user = userEvent.setup();
    renderTab();
    await screen.findByText('Kindred Clash');

    await user.type(screen.getByLabelText('Filter past games'), 'tremere');

    await waitFor(() => expect(screen.queryByText('Kindred Clash')).not.toBeInTheDocument());
    expect(screen.getByText('Autumn Cup')).toBeInTheDocument();
    expect(screen.getByText('1 of 2 games')).toBeInTheDocument();
  });

  it('shows the no-match empty state', async () => {
    const user = userEvent.setup();
    renderTab();
    await screen.findByText('Kindred Clash');

    await user.type(screen.getByLabelText('Filter past games'), 'zzzzz');

    expect(await screen.findByText('No games match your filter.')).toBeInTheDocument();
  });

  it('shows the empty state when there is no history', async () => {
    renderTab([]);
    expect(await screen.findByText('No completed games yet.')).toBeInTheDocument();
  });

  it('reorders cards by the selected sort', async () => {
    const user = userEvent.setup();
    renderTab();
    await screen.findByText('Kindred Clash');

    const titleText = () =>
      screen.getAllByText(/Kindred Clash|Autumn Cup/).map((el) => el.textContent);

    // newest first → Autumn Cup (Sep) before Kindred Clash (Aug)
    expect(titleText()[0]).toContain('Autumn Cup');

    await user.selectOptions(screen.getByLabelText('Sort past games'), 'oldest');
    await waitFor(() => expect(titleText()[0]).toContain('Kindred Clash'));
  });
});
