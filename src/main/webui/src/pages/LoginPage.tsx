import { useEffect, useState, type FormEvent } from 'react';
import { getCaptchaConfig } from '../api/config';
import { login, register } from './login/authApi';
import { TurnstileWidget } from './login/TurnstileWidget';
import { Card, CardHeader, CardBody } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Switch } from '../components/ui/Switch';
import { Button } from '../components/ui/Button';
import { InlineAlert } from '../components/ui/FormFeedback';

// Mirrors login.jsp — the one page reachable while logged out, so it's a
// standalone route outside the authenticated shell (see App.tsx). Login and
// Register are the same toggle-panel UX legacy used.
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
        } else if (res.status === 409) {
          setRegisterError('That username is already taken.');
          setRegistering(false);
        } else if (res.status === 400) {
          setRegisterError('Captcha verification failed. Please try again.');
          setRegistering(false);
        } else {
          setRegisterError('Registration failed. Please try again.');
          setRegistering(false);
        }
      })
      .catch(() => {
        setRegisterError('Unable to reach the server. Please try again.');
        setRegistering(false);
      });
  };

  const captchaSatisfied = !captcha?.enabled || !!captchaToken;

  return (
    <div id="content" className="flex flex-col flex-1 min-h-0 overflow-y-auto bg-base text-ink">
      <div className="w-full max-w-xl mx-auto mt-8 px-3">
        <h1 className="font-serif text-3xl mb-3">V:TES Online</h1>
        {!showRegister ? (
          <Card>
            <CardHeader className="flex justify-between items-center">
              <span className="text-lg">Welcome back</span>
              <button type="button" className="text-sm text-accent hover:underline" onClick={() => setShowRegister(true)}>
                Need an account?
              </button>
            </CardHeader>
            <CardBody className="flex flex-col gap-3">
              <p className="text-sm text-ink-secondary">
                Welcome to V:TES Online, the unofficial home to play Vampire: The Eternal Struggle online.{' '}
                <a className="text-accent underline" href="https://www.vekn.net/what-is-v-tes" target="_blank" rel="noreferrer">
                  What is Vampire: The Eternal Struggle?
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
                <Input
                  id="dspassin"
                  type="password"
                  label="Password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
                <Switch id="remember" label="Remember me" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
                {loginError && <InlineAlert kind="danger">{loginError}</InlineAlert>}
                <Button type="submit" variant="secondary" size="lg" className="self-start" disabled={loggingIn}>
                  Log In
                </Button>
              </form>
            </CardBody>
          </Card>
        ) : (
          <Card>
            <CardHeader className="flex justify-between items-center">
              <span className="text-lg">Join us</span>
              <button type="button" className="text-sm text-accent hover:underline" onClick={() => setShowRegister(false)}>
                Already have an account?
              </button>
            </CardHeader>
            <CardBody className="flex flex-col gap-3">
              <p className="text-sm text-ink-secondary">
                Import decks from your favorite deck building program. Play multiple games simultaneously. Test a deck
                before a tournament.
              </p>
              <form onSubmit={submitRegister} className="flex flex-col gap-3">
                <Input id="newplayer" label="Username" value={newPlayer} onChange={(e) => setNewPlayer(e.target.value)} />
                <Input
                  id="newpassword"
                  type="password"
                  label="Password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
                <Input
                  id="newemail"
                  type="email"
                  label="E-mail address"
                  placeholder="user@example.org"
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                />
                {captcha?.enabled && captcha.siteKey && (
                  <TurnstileWidget siteKey={captcha.siteKey} onToken={setCaptchaToken} />
                )}
                {registerError && <InlineAlert kind="danger">{registerError}</InlineAlert>}
                <Button
                  type="submit"
                  variant="secondary"
                  size="lg"
                  className="self-start"
                  disabled={registering || !captchaSatisfied}
                >
                  Register
                </Button>
              </form>
            </CardBody>
          </Card>
        )}
      </div>
    </div>
  );
}
