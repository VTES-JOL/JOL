import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ReplacePlayer } from './ReplacePlayer';
import { api } from '../../api/client';
import { showError } from '../../components/toast';

vi.mock('../../api/client', () => ({
  api: { get: vi.fn(), put: vi.fn() },
}));

vi.mock('../../components/toast', () => ({
  showError: vi.fn(),
}));

beforeEach(() => {
  vi.mocked(api.get).mockReset();
  vi.mocked(api.put).mockReset();
  vi.mocked(showError).mockReset();
});

describe('ReplacePlayer', () => {
  it('loads the first game\'s players on mount and defaults the substitute to the first entry', async () => {
    vi.mocked(api.get).mockResolvedValue(['Player1', 'Player2']);
    render(<ReplacePlayer games={{ g1: 'Game One', g2: 'Game Two' }} substitutes={['Sub1', 'Sub2']} onSaved={vi.fn()} />);

    expect(api.get).toHaveBeenCalledWith('/game/g1/players');
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));
    expect(screen.getByLabelText('Substitute')).toHaveValue('Sub1');
  });

  it('reloads players when a different game is selected', async () => {
    vi.mocked(api.get).mockResolvedValueOnce(['Player1']).mockResolvedValueOnce(['Player3', 'Player4']);
    const user = userEvent.setup();
    render(<ReplacePlayer games={{ g1: 'Game One', g2: 'Game Two' }} substitutes={['Sub1']} onSaved={vi.fn()} />);
    await waitFor(() => expect(api.get).toHaveBeenCalledTimes(1));

    await user.selectOptions(screen.getByLabelText('Games'), 'g2');

    expect(api.get).toHaveBeenCalledWith('/game/g2/players');
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player3'));
  });

  it('submits the replacement and calls onSaved', async () => {
    vi.mocked(api.get).mockResolvedValue(['Player1', 'Player2']);
    vi.mocked(api.put).mockResolvedValue(undefined);
    const onSaved = vi.fn();
    const user = userEvent.setup();
    render(<ReplacePlayer games={{ g1: 'Game One' }} substitutes={['Sub1']} onSaved={onSaved} />);
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));

    await user.click(screen.getByRole('button', { name: 'Replace player' }));

    expect(api.put).toHaveBeenCalledWith('/admin-page/games/g1/replace-player', {
      existingPlayer: 'Player1',
      newPlayer: 'Sub1',
    });
    await waitFor(() => expect(onSaved).toHaveBeenCalled());
  });

  it('shows a toast and does not call onSaved when the replace call fails', async () => {
    vi.mocked(api.get).mockResolvedValue(['Player1']);
    vi.mocked(api.put).mockRejectedValue(new Error('boom'));
    const onSaved = vi.fn();
    const user = userEvent.setup();
    render(<ReplacePlayer games={{ g1: 'Game One' }} substitutes={['Sub1']} onSaved={onSaved} />);
    await waitFor(() => expect(screen.getByLabelText('Player to replace:')).toHaveValue('Player1'));

    await user.click(screen.getByRole('button', { name: 'Replace player' }));

    await waitFor(() => expect(showError).toHaveBeenCalledWith('Failed to replace player.'));
    expect(onSaved).not.toHaveBeenCalled();
  });

  it('does not submit when there are no games at all', async () => {
    render(<ReplacePlayer games={{}} substitutes={[]} onSaved={vi.fn()} />);

    expect(api.get).not.toHaveBeenCalled();
    await userEvent.setup().click(screen.getByRole('button', { name: 'Replace player' }));
    expect(api.put).not.toHaveBeenCalled();
  });
});
