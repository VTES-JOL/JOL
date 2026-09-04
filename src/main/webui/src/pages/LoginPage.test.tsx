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

const VALID_PW = 'sup3rsecret';

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
    vi.mocked(login).mockResolvedValue({ ok: true, status: 200, message: '' });
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.type(screen.getByLabelText('Username'), 'Player1');
    await user.type(screen.getByLabelText('Password'), 'password');
    await user.click(screen.getByRole('button', { name: 'Log In' }));

    await waitFor(() => expect(window.location.href).toBe('/jol/'));
    expect(login).toHaveBeenCalledWith('Player1', 'password', true);
  });

  it('shows an inline error and re-enables the button on a bad password', async () => {
    vi.mocked(login).mockResolvedValue({ ok: false, status: 401, message: '' });
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

  it('disables Log In until both fields are filled', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    expect(screen.getByRole('button', { name: 'Log In' })).toBeDisabled();
    await user.type(screen.getByLabelText('Username'), 'Player1');
    expect(screen.getByRole('button', { name: 'Log In' })).toBeDisabled();
    await user.type(screen.getByLabelText('Password'), 'password');
    expect(screen.getByRole('button', { name: 'Log In' })).toBeEnabled();
  });

  it('toggles to the register panel and back', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.click(screen.getByRole('button', { name: 'Need an account?' }));
    expect(screen.getByRole('button', { name: 'Already have an account?' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Already have an account?' }));
    expect(screen.getByRole('button', { name: 'Need an account?' })).toBeInTheDocument();
  });

  it('keeps Register disabled until the fields are valid', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.click(screen.getByRole('button', { name: 'Need an account?' }));
    expect(screen.getByRole('button', { name: 'Register' })).toBeDisabled();

    await user.type(screen.getByLabelText('Username'), 'NewPlayer');
    await user.type(screen.getByLabelText('Password'), 'short'); // < 8 chars
    await user.type(screen.getByLabelText('E-mail address'), 'new@example.org');
    expect(screen.getByRole('button', { name: 'Register' })).toBeDisabled();

    await user.clear(screen.getByLabelText('Password'));
    await user.type(screen.getByLabelText('Password'), VALID_PW);
    expect(screen.getByRole('button', { name: 'Register' })).toBeEnabled();
  });

  it('registers a new account and redirects on success', async () => {
    vi.mocked(register).mockResolvedValue({ ok: true, status: 200, message: '' });
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.click(screen.getByRole('button', { name: 'Need an account?' }));
    await user.type(screen.getByLabelText('Username'), 'NewPlayer');
    await user.type(screen.getByLabelText('Password'), VALID_PW);
    await user.type(screen.getByLabelText('E-mail address'), 'new@example.org');
    await user.click(screen.getByRole('button', { name: 'Register' }));

    await waitFor(() => expect(window.location.href).toBe('/jol/'));
    expect(register).toHaveBeenCalledWith('NewPlayer', VALID_PW, 'new@example.org', '');
  });

  it('surfaces the server message when registration fails', async () => {
    vi.mocked(register).mockResolvedValue({
      ok: false,
      status: 409,
      message: 'That username is already taken.',
    });
    const user = userEvent.setup();
    render(<LoginPage />);

    await user.click(screen.getByRole('button', { name: 'Need an account?' }));
    await user.type(screen.getByLabelText('Username'), 'Player1');
    await user.type(screen.getByLabelText('Password'), VALID_PW);
    await user.type(screen.getByLabelText('E-mail address'), 'p1@example.org');
    await user.click(screen.getByRole('button', { name: 'Register' }));

    expect(await screen.findByText('That username is already taken.')).toBeInTheDocument();
  });
});
