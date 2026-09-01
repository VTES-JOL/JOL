import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { reloadForUpdate } from './updateCheck';

describe('reloadForUpdate', () => {
  const reload = vi.fn();

  beforeEach(() => {
    reload.mockClear();
    // jsdom's location.reload throws "Not implemented" — replace it.
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: { ...window.location, reload },
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('primes a fresh app-shell fetch before reloading', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('<!doctype html>'));
    vi.stubGlobal('fetch', fetchMock);

    await reloadForUpdate();

    expect(fetchMock).toHaveBeenCalledWith(import.meta.env.BASE_URL, { cache: 'reload' });
    expect(reload).toHaveBeenCalledTimes(1);
  });

  it('still reloads when the prefetch fails (offline)', async () => {
    const fetchMock = vi.fn().mockRejectedValue(new Error('offline'));
    vi.stubGlobal('fetch', fetchMock);

    await reloadForUpdate();

    expect(reload).toHaveBeenCalledTimes(1);
  });
});
