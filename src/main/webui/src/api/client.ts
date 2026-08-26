import { CLIENT_ID } from '../ws/socket';
import { reportFailure, reportSuccess } from './connectivity';
import { API_BASE } from './apiBase';

class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method,
      credentials: 'include',
      headers: {
        'X-Client-Id': CLIENT_ID,
        ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
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
    throw new ApiError(res.status, await res.text());
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
    throw new ApiError(res.status, await res.text());
  }
  return res.text();
}

export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body ?? {}),
  put: <T>(path: string, body?: unknown) => request<T>('PUT', path, body ?? {}),
  del: <T>(path: string, body?: unknown) => request<T>('DELETE', path, body),
  // For text/plain responses (e.g. CSV export) — request<T>() always parses JSON.
  getText: (path: string) => requestText(path),
};

export { ApiError };
