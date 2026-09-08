import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MobileTabBar } from './MobileTabBar';

const base = {
  active: 'table' as const,
  onSelect: vi.fn(),
  showHand: true,
  showAct: true,
  handCount: 7,
  logUnread: false,
};

describe('MobileTabBar', () => {
  it('shows all four tabs for a seated player, with the hand count', () => {
    render(<MobileTabBar {...base} onSelect={vi.fn()} />);
    expect(screen.getByRole('button', { name: /Table/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Hand/ })).toHaveTextContent('7');
    expect(screen.getByRole('button', { name: /Log/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Act/ })).toBeInTheDocument();
  });

  it('hides Hand and Act for a bare spectator', () => {
    render(<MobileTabBar {...base} onSelect={vi.fn()} showHand={false} showAct={false} />);
    expect(screen.getByRole('button', { name: /Table/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Log/ })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Hand/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Act/ })).not.toBeInTheDocument();
  });

  it('marks the active tab pressed and reports selections', async () => {
    const onSelect = vi.fn();
    const user = userEvent.setup();
    render(<MobileTabBar {...base} active="log" onSelect={onSelect} />);

    expect(screen.getByRole('button', { name: /Log/ })).toHaveAttribute('aria-pressed', 'true');
    await user.click(screen.getByRole('button', { name: /Act/ }));
    expect(onSelect).toHaveBeenCalledWith('act');
  });
});
