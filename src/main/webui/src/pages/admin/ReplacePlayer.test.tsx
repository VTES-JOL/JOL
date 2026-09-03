import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReplacePlayer } from './ReplacePlayer';
import { api } from '../../api/client';
import { showError } from '../../stores/toast';
import { confirmDialog } from '../../stores/dialog';

vi.mock('../../api/client', () => ({
  api: { get: vi.fn(), put: vi.fn() },
}));

vi.mock('../../stores/toast', () => ({
  showError: vi.fn(),
}));

vi.mock('../../stores/dialog', () => ({
  confirmDialog: vi.fn(),
}));

beforeEach(() => {
  vi.mocked(api.get).mockReset();
  vi.mocked(api.put).mockReset();
  vi.mocked(showError).mockReset();
  // Default: the admin confirms the "Replace this player?" dialog.
  vi.mocked(confirmDialog).mockReset().mockResolvedValue(true);
});

function renderWithClient(
  games: Record<string, string>,
  substitutes: string[],
  gamePlayers: Record<string, string[]> = {},
) {
  vi.mocked(api.get).mockImplementation((path: string) => {
    if (path === '/admin-page/games') return Promise.resolve(games);
    if (path === '/admin-page/substitutes') return Promise.resolve(substitutes);
    const match = /^\/game\/([^/]+)\/players$/.exec(path);
    if (match) return Promise.resolve(gamePlayers[match[1]] ?? []);
    return Promise.resolve([]);
  });
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <ReplacePlayer />
    </QueryClientProvider>,
  );
}

describe('ReplacePlayer', () => {
  it('loads the first game\'s players on mount and defaults the substitute to the first entry', async () => {
    renderWithClient(
      { g1: 'Game One', g2: 'Game Two' },
      ['Sub1', 'Sub2'],
      { g1: ['Player1', 'Player2'] },
    );

    await waitFor(() => expect(api.get).toHaveBeenCalledWith('/game/g1/players'));
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));
    expect(screen.getByLabelText('Substitute')).toHaveValue('Sub1');
  });

  it('reloads players when a different game is selected', async () => {
    const user = userEvent.setup();
    renderWithClient(
      { g1: 'Game One', g2: 'Game Two' },
      ['Sub1'],
      { g1: ['Player1'], g2: ['Player3', 'Player4'] },
    );
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));

    await user.selectOptions(screen.getByLabelText('Games'), 'g2');

    expect(api.get).toHaveBeenCalledWith('/game/g2/players');
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player3'));
  });

  it('submits the replacement', async () => {
    vi.mocked(api.put).mockResolvedValue(undefined);
    const user = userEvent.setup();
    renderWithClient({ g1: 'Game One' }, ['Sub1'], { g1: ['Player1', 'Player2'] });
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));

    await user.click(screen.getByRole('button', { name: 'Replace player' }));

    await waitFor(() =>
      expect(api.put).toHaveBeenCalledWith('/admin-page/games/g1/replace-player', {
        existingPlayer: 'Player1',
        newPlayer: 'Sub1',
      }),
    );
    expect(confirmDialog).toHaveBeenCalled();
  });

  it('does not submit when the confirm dialog is dismissed', async () => {
    vi.mocked(confirmDialog).mockResolvedValue(false);
    vi.mocked(api.put).mockResolvedValue(undefined);
    const user = userEvent.setup();
    renderWithClient({ g1: 'Game One' }, ['Sub1'], { g1: ['Player1', 'Player2'] });
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));

    await user.click(screen.getByRole('button', { name: 'Replace player' }));

    await waitFor(() => expect(confirmDialog).toHaveBeenCalled());
    expect(api.put).not.toHaveBeenCalled();
  });

  it('shows a toast when the replace call fails', async () => {
    vi.mocked(api.put).mockRejectedValue(new Error('boom'));
    const user = userEvent.setup();
    renderWithClient({ g1: 'Game One' }, ['Sub1'], { g1: ['Player1'] });
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));

    await user.click(screen.getByRole('button', { name: 'Replace player' }));

    await waitFor(() => expect(showError).toHaveBeenCalledWith('Failed to replace player.'));
  });

  it('does not submit when there are no games at all', async () => {
    renderWithClient({}, []);
    await waitFor(() => expect(api.get).toHaveBeenCalledWith('/admin-page/games'));

    await userEvent.setup().click(screen.getByRole('button', { name: 'Replace player' }));
    expect(api.put).not.toHaveBeenCalled();
  });
});
