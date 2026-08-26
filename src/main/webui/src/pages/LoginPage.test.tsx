import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginPage } from './LoginPage';
import { login, register } from './login/authApi';
import { getCaptchaConfig } from '../api/config';

vi.mock('./login/authApi', () => ({
  login: vi.fn(),
  register: vi.fn(),
}));

vi.mock('../api/config', () => ({
  getCaptchaConfig: vi.fn(),
}));

// Captcha is disabled by default in every test below (matches ENABLE_CAPTCHA=false
// local dev, see CLAUDE.md) so submit buttons aren't gated on a real Turnstile widget.
beforeEach(() => {
  vi.mocked(getCaptchaConfig).mockResolvedValue({ enabled: false, siteKey: null });
  vi.mocked(login).mockReset();
  vi.mocked(register).mockReset();
  // @ts-expect-error jsdom doesn't implement navigation
  delete window.location;
  // @ts-expect-error minimal stub, only `href` is used by LoginPage
  window.location = { href: '' };
});

describe('LoginPage', () => {
  it('redirects to /jol/ on successful login', async () => {
    vi.mocked(login).mockResolvedValue({ ok: true, status: 200 });
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.type(screen.getByLabelText('Username'), 'Player1');
    await user.type(screen.getByLabelText('Password'), 'password');
    await user.click(screen.getByRole('button', { name: 'Log In' }));

    await waitFor(() => expect(window.location.href).toBe('/jol/'));
    expect(login).toHaveBeenCalledWith('Player1', 'password', true);
  });

  it('shows an inline error and re-enables the button on a bad password', async () => {
    vi.mocked(login).mockResolvedValue({ ok: false, status: 401 });
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.type(screen.getByLabelText('Username'), 'Player1');
    await user.type(screen.getByLabelText('Password'), 'wrong');
    await user.click(screen.getByRole('button', { name: 'Log In' }));

    expect(await screen.findByText('Invalid username or password.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Log In' })).toBeEnabled();
    expect(window.location.href).toBe('');
  });

  it('shows a network error when the server is unreachable', async () => {
    vi.mocked(login).mockRejectedValue(new Error('network down'));
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.type(screen.getByLabelText('Username'), 'Player1');
    await user.type(screen.getByLabelText('Password'), 'password');
    await user.click(screen.getByRole('button', { name: 'Log In' }));

    expect(await screen.findByText('Unable to reach the server. Please try again.')).toBeInTheDocument();
  });

  it('toggles to the register panel and back', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.click(screen.getByRole('button', { name: 'Need an account?' }));
    expect(screen.getByRole('button', { name: 'Already have an account?' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Already have an account?' }));
    expect(screen.getByRole('button', { name: 'Need an account?' })).toBeInTheDocument();
  });

  it('registers a new account and redirects on success', async () => {
    vi.mocked(register).mockResolvedValue({ ok: true, status: 200 });
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.click(screen.getByRole('button', { name: 'Need an account?' }));
    await user.type(screen.getByLabelText('Username'), 'NewPlayer');
    await user.type(screen.getByLabelText('Password'), 'hunter2');
    await user.type(screen.getByLabelText('E-mail address'), 'new@example.org');
    await user.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() => expect(window.location.href).toBe('/jol/'));
    expect(register).toHaveBeenCalledWith('NewPlayer', 'hunter2', 'new@example.org', '');
  });

  it('shows a specific error when the username is already taken', async () => {
    vi.mocked(register).mockResolvedValue({ ok: false, status: 409 });
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.click(screen.getByRole('button', { name: 'Need an account?' }));
    await user.type(screen.getByLabelText('Username'), 'Player1');
    await user.type(screen.getByLabelText('Password'), 'hunter2');
    await user.type(screen.getByLabelText('E-mail address'), 'p1@example.org');
    await user.click(screen.getByRole('button', { name: 'Register' }));

    expect(await screen.findByText('That username is already taken.')).toBeInTheDocument();
  });
});
