import { useEffect, useState, type FormEvent } from 'react';
import { getCaptchaConfig } from '../api/config';
import { login, register } from './login/authApi';
import { TurnstileWidget } from './login/TurnstileWidget';

// Mirrors login.jsp — the one page reachable while logged out, so it's a
// standalone route outside NavProvider/TopBar (see App.tsx): NavProvider's
// /nav fetch requires auth and would otherwise 401-redirect right back here
// in a loop. Login and Register are the same toggle-panel UX legacy used,
// not two separate routes (register.jsp never existed as a standalone page).
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
    <div id="wrapper" className="container-fluid">
      <div className="w-md-50 m-auto mt-4" id="content">
        <h1>V:TES Online</h1>
        {!showRegister ? (
          <div className="card" id="loginPanel">
            <div className="card-header d-flex justify-content-between align-items-center">
              <span className="fs-5">Welcome back</span>
              <button type="button" className="btn btn-link" onClick={() => setShowRegister(true)}>
                Need an account?
              </button>
            </div>
            <div className="card-body">
              <p>Welcome to V:TES Online, the unofficial home to play Vampire: The Eternal Struggle online.</p>
              <p>
                <a href="https://www.vekn.net/what-is-v-tes" target="_blank" rel="noreferrer">
                  What is Vampire: The Eternal Struggle?
                </a>
              </p>
              <form onSubmit={submitLogin}>
                <div className="form-floating mb-3">
                  <input
                    type="text"
                    className="form-control"
                    id="dsuserin"
                    autoComplete="username"
                    placeholder="Username"
                    autoFocus
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                  />
                  <label htmlFor="dsuserin">Username</label>
                </div>
                <div className="form-floating mb-3">
                  <input
                    type="password"
                    className="form-control"
                    id="dspassin"
                    autoComplete="current-password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <label htmlFor="dspassin">Password</label>
                </div>
                <div className="form-check mb-3">
                  <input
                    type="checkbox"
                    className="form-check-input"
                    id="remember"
                    checked={remember}
                    onChange={(e) => setRemember(e.target.checked)}
                  />
                  <label className="form-check-label" htmlFor="remember">
                    Remember me
                  </label>
                </div>
                {loginError && <div className="alert alert-danger py-2">{loginError}</div>}
                <button type="submit" id="loginBtn" className="btn btn-outline-secondary btn-lg mt-2" disabled={loggingIn}>
                  Log In
                </button>
              </form>
            </div>
          </div>
        ) : (
          <div className="card" id="registerPanel">
            <div className="card-header d-flex justify-content-between align-items-center">
              <span className="fs-5">Join us</span>
              <button type="button" className="btn btn-link" onClick={() => setShowRegister(false)}>
                Already have an account?
              </button>
            </div>
            <div className="card-body">
              <div className="mb-2">
                <div>
                  Import decks from your favorite deck building program. Play multiple games simultaneously. Test a deck
                  before a tournament.
                </div>
              </div>
              <form onSubmit={submitRegister}>
                <div className="form-floating mb-3">
                  <input
                    type="text"
                    className="form-control"
                    id="newplayer"
                    placeholder="Username"
                    value={newPlayer}
                    onChange={(e) => setNewPlayer(e.target.value)}
                  />
                  <label htmlFor="newplayer">Username</label>
                </div>
                <div className="form-floating mb-3">
                  <input
                    type="password"
                    className="form-control"
                    id="newpassword"
                    placeholder="Password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                  />
                  <label htmlFor="newpassword">Password</label>
                </div>
                <div className="form-floating mb-3">
                  <input
                    type="email"
                    className="form-control"
                    id="newemail"
                    placeholder="user@example.org"
                    value={newEmail}
                    onChange={(e) => setNewEmail(e.target.value)}
                  />
                  <label htmlFor="newemail">E-mail address</label>
                </div>
                {captcha?.enabled && captcha.siteKey && (
                  <div className="mb-3">
                    <TurnstileWidget siteKey={captcha.siteKey} onToken={setCaptchaToken} />
                  </div>
                )}
                {registerError && <div className="alert alert-danger py-2">{registerError}</div>}
                <button type="submit" className="btn btn-outline-secondary btn-lg mt-1" disabled={registering || !captchaSatisfied}>
                  Register
                </button>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
