import { API_BASE } from '../../api/apiBase';
import { CLIENT_ID } from '../../ws/socket';

// Deliberately bypasses api/client.ts's request() — that helper hard-redirects
// to /jol/login on any 401, which is the right behavior for an already-logged-in
// page whose session expired, but wrong here: a bad password is a normal,
// expected 401 this page needs to show inline, not treat as "session expired."
async function post(path: string, body: unknown): Promise<{ ok: boolean; status: number }> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', 'X-Client-Id': CLIENT_ID },
    body: JSON.stringify(body),
  });
  return { ok: res.ok, status: res.status };
}

export function login(username: string, password: string, remember: boolean) {
  return post('/auth/login', { username, password, remember });
}

export function register(username: string, password: string, email: string, captchaResponse: string) {
  return post('/auth/register', { username, password, email, captchaResponse });
}

export function logout() {
  return post('/auth/logout', {});
}
