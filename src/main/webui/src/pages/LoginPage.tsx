import { useEffect, useState, type FormEvent } from 'react';
import { getCaptchaConfig } from '../api/config';
import { login, register } from './login/authApi';
import { TurnstileWidget } from './login/TurnstileWidget';
import { Card, CardHeader, CardBody } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { PasswordInput } from '../components/ui/PasswordInput';
import { Switch } from '../components/ui/Switch';
import { Button } from '../components/ui/Button';
import { FieldHint, InlineAlert } from '../components/ui/FormFeedback';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD = 8;

// The one page reachable while logged out (see app/AppRoutes.tsx), so it
// renders its own product chrome — a header strip matching the app's TopBar —
// rather than the authenticated shell. Login and Register are one toggle.
export function LoginPage() {
  const [showRegister, setShowRegister] = useState(false);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(true);
  const [loginError, setLoginError] = useState('');
  const [loggingIn, setLoggingIn] = useState(false);

  const [newPlayer, setNewPlayer] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [captchaToken, setCaptchaToken] = useState('');
  const [registerError, setRegisterError] = useState('');
  const [registering, setRegistering] = useState(false);

  const [captcha, setCaptcha] = useState<{ enabled: boolean; siteKey: string | null } | null>(null);
  useEffect(() => {
    getCaptchaConfig()
      .then(setCaptcha)
      .catch(() => setCaptcha({ enabled: false, siteKey: null }));
  }, []);

  const submitLogin = (e: FormEvent) => {
    e.preventDefault();
    setLoggingIn(true);
    setLoginError('');
    login(username, password, remember)
      .then((res) => {
        if (res.ok) {
          window.location.href = '/jol/';
        } else {
          setLoginError('Invalid username or password.');
          setLoggingIn(false);
        }
      })
      .catch(() => {
        setLoginError('Unable to reach the server. Please try again.');
        setLoggingIn(false);
      });
  };

  const submitRegister = (e: FormEvent) => {
    e.preventDefault();
    setRegistering(true);
    setRegisterError('');
    register(newPlayer, newPassword, newEmail, captchaToken)
      .then((res) => {
        if (res.ok) {
          window.location.href = '/jol/';
        } else {
          setRegisterError(res.message || 'Registration failed. Please try again.');
          setRegistering(false);
        }
      })
      .catch(() => {
        setRegisterError('Unable to reach the server. Please try again.');
        setRegistering(false);
      });
  };

  // A misconfigured deployment (captcha on, no site key) can't render the
  // widget, so registration genuinely can't proceed — say so rather than
  // leaving a silently-disabled button.
  const captchaMisconfigured = !!captcha?.enabled && !captcha.siteKey;
  const captchaSatisfied = !captcha?.enabled || !!captchaToken;

  const loginValid = !loggingIn && username.trim().length > 0 && password.length > 0;
  const emailValid = EMAIL_RE.test(newEmail);
  const registerValid =
    !registering &&
    newPlayer.trim().length > 0 &&
    newPassword.length >= MIN_PASSWORD &&
    emailValid &&
    captchaSatisfied &&
    !captchaMisconfigured;

  return (
    <div className="flex flex-col flex-1 min-h-0 bg-base text-ink">
      <header className="flex items-center bg-[#1c1e21] px-4 shrink-0">
        <span className="px-2 py-3 font-serif text-lg text-white">V:TES Online</span>
      </header>

      <div className="flex-1 min-h-0 overflow-y-auto flex items-start justify-center px-4 py-10 sm:items-center">
        <div className="w-full max-w-md">
          {!showRegister ? (
            <Card>
              <CardHeader className="flex items-center justify-between">
                <span className="text-lg">Welcome back</span>
                <button
                  type="button"
                  className="text-sm text-accent hover:underline"
                  onClick={() => setShowRegister(true)}
                >
                  Need an account?
                </button>
              </CardHeader>
              <CardBody className="flex flex-col gap-3">
                <p className="text-sm text-ink-secondary">
                  The unofficial home to play Vampire: The Eternal Struggle online.{' '}
                  <a
                    className="text-accent underline"
                    href="https://www.vekn.net/what-is-v-tes"
                    target="_blank"
                    rel="noreferrer"
                  >
                    What is V:TES?
                  </a>
                </p>
                <form onSubmit={submitLogin} className="flex flex-col gap-3">
                  <Input
                    id="dsuserin"
                    label="Username"
                    autoComplete="username"
                    autoFocus
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                  />
                  <PasswordInput
                    id="dspassin"
                    label="Password"
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <Switch
                    id="remember"
                    label="Remember me"
                    checked={remember}
                    onChange={(e) => setRemember(e.target.checked)}
                  />
                  {loginError && <InlineAlert kind="danger">{loginError}</InlineAlert>}
                  <Button type="submit" variant="primary" size="lg" className="w-full" disabled={!loginValid}>
                    {loggingIn ? 'Signing in…' : 'Log In'}
                  </Button>
                </form>
              </CardBody>
            </Card>
          ) : (
            <Card>
              <CardHeader className="flex items-center justify-between">
                <span className="text-lg">Create your account</span>
                <button
                  type="button"
                  className="text-sm text-accent hover:underline"
                  onClick={() => setShowRegister(false)}
                >
                  Already have an account?
                </button>
              </CardHeader>
              <CardBody className="flex flex-col gap-3">
                <p className="text-sm text-ink-secondary">
                  Import decks from your favourite builder, play multiple games at once, and test a deck before a
                  tournament.
                </p>
                <form onSubmit={submitRegister} className="flex flex-col gap-3">
                  <Input
                    id="newplayer"
                    label="Username"
                    autoComplete="username"
                    value={newPlayer}
                    onChange={(e) => setNewPlayer(e.target.value)}
                  />
                  <div>
                    <PasswordInput
                      id="newpassword"
                      label="Password"
                      autoComplete="new-password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      error={
                        newPassword.length > 0 && newPassword.length < MIN_PASSWORD
                          ? `At least ${MIN_PASSWORD} characters.`
                          : undefined
                      }
                    />
                    {(newPassword.length === 0 || newPassword.length >= MIN_PASSWORD) && (
                      <FieldHint>At least {MIN_PASSWORD} characters. There is no e-mail reset yet, so keep it safe.</FieldHint>
                    )}
                  </div>
                  <Input
                    id="newemail"
                    type="email"
                    label="E-mail address"
                    autoComplete="email"
                    placeholder="user@example.org"
                    value={newEmail}
                    onChange={(e) => setNewEmail(e.target.value)}
                    error={newEmail.length > 0 && !emailValid ? 'Enter a valid e-mail address.' : undefined}
                  />
                  {captcha?.enabled && captcha.siteKey && (
                    <TurnstileWidget siteKey={captcha.siteKey} onToken={setCaptchaToken} />
                  )}
                  {captchaMisconfigured && (
                    <InlineAlert kind="danger">Registration is temporarily unavailable. Please try again later.</InlineAlert>
                  )}
                  {registerError && <InlineAlert kind="danger">{registerError}</InlineAlert>}
                  <Button type="submit" variant="primary" size="lg" className="w-full" disabled={!registerValid}>
                    {registering ? 'Creating account…' : 'Register'}
                  </Button>
                </form>
              </CardBody>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
