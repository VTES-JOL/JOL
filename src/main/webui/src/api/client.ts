import { CLIENT_ID } from '../stores/socket';
import { reportFailure, reportSuccess } from '../stores/connectivity';
import { API_BASE } from './apiBase';

class ApiError extends Error {
  status: number;
  /**
   * Stable error token from the server's `{code,message}` body (see
   * net.deckserver.rest.ApiExceptionMappers). Present on mapped 4xx / 5xx;
   * undefined for network errors, 401, and unmapped 500s. Prefer branching on
   * this over matching `message`.
   */
  code?: string;

  constructor(status: number, message: string, code?: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

// Parse the server's uniform `{code,message}` error body; fall back to the raw
// text as the message when it isn't that shape.
function parseErrorBody(text: string): { message: string; code?: string } {
  try {
    const json = JSON.parse(text);
    if (json && typeof json === 'object' && typeof json.code === 'string') {
      return { message: typeof json.message === 'string' ? json.message : text, code: json.code };
    }
  } catch {
    /* not JSON */
  }
  return { message: text };
}

async function request<T>(method: string, path: string, body?: unknown, extraHeaders?: Record<string, string>): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method,
      credentials: 'include',
      headers: {
        'X-Client-Id': CLIENT_ID,
        ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...extraHeaders,
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    // fetch() itself rejected — the server (or network) is unreachable,
    // not just erroring. Distinct from any HTTP status response below,
    // which means the server responded at all, however badly.
    reportFailure();
    throw new ApiError(0, 'Network error');
  }
  reportSuccess();
  if (res.status === 401) {
    window.location.href = '/jol/login';
    throw new ApiError(401, 'Unauthenticated');
  }
  if (!res.ok) {
    const parsed = parseErrorBody(await res.text());
    throw new ApiError(res.status, parsed.message, parsed.code);
  }
  if (res.status === 204) {
    return undefined as T;
  }
  return (await res.json()) as T;
}

async function requestText(path: string): Promise<string> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, { credentials: 'include', headers: { 'X-Client-Id': CLIENT_ID } });
  } catch {
    reportFailure();
    throw new ApiError(0, 'Network error');
  }
  reportSuccess();
  if (res.status === 401) {
    window.location.href = '/jol/login';
    throw new ApiError(401, 'Unauthenticated');
  }
  if (!res.ok) {
    const parsed = parseErrorBody(await res.text());
    throw new ApiError(res.status, parsed.message, parsed.code);
  }
  return res.text();
}

async function postText<T>(path: string, body: string): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'X-Client-Id': CLIENT_ID, 'Content-Type': 'text/plain' },
      body,
    });
  } catch {
    reportFailure();
    throw new ApiError(0, 'Network error');
  }
  reportSuccess();
  if (res.status === 401) {
    window.location.href = '/jol/login';
    throw new ApiError(401, 'Unauthenticated');
  }
  if (!res.ok) {
    const parsed = parseErrorBody(await res.text());
    throw new ApiError(res.status, parsed.message, parsed.code);
  }
  return (await res.json()) as T;
}

export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown, headers?: Record<string, string>) =>
    request<T>('POST', path, body ?? {}, headers),
  put: <T>(path: string, body?: unknown, headers?: Record<string, string>) =>
    request<T>('PUT', path, body ?? {}, headers),
  del: <T>(path: string, body?: unknown) => request<T>('DELETE', path, body),
  // For text/plain responses (e.g. CSV export) — request<T>() always parses JSON.
  getText: (path: string) => requestText(path),
  // POST a raw text/plain body, parse a JSON response (deck-import preview).
  postText: <T>(path: string, body: string) => postText<T>(path, body),
};

export { ApiError };
