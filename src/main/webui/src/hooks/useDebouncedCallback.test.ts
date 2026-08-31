import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useDebouncedCallback } from './useDebouncedCallback';

beforeEach(() => vi.useFakeTimers());
afterEach(() => vi.useRealTimers());

describe('useDebouncedCallback', () => {
  it('invokes once, with the last args, after the delay', () => {
    const fn = vi.fn();
    const { result } = renderHook(() => useDebouncedCallback(fn, 500));

    result.current.call('a');
    result.current.call('b');
    expect(fn).not.toHaveBeenCalled();

    vi.advanceTimersByTime(500);
    expect(fn).toHaveBeenCalledExactlyOnceWith('b');
  });

  it('flush() runs the pending call immediately and only once', () => {
    const fn = vi.fn();
    const { result } = renderHook(() => useDebouncedCallback(fn, 500));

    result.current.call('x');
    result.current.flush();
    expect(fn).toHaveBeenCalledExactlyOnceWith('x');

    vi.advanceTimersByTime(500);
    expect(fn).toHaveBeenCalledOnce();
  });

  it('flush() with nothing pending is a no-op', () => {
    const fn = vi.fn();
    const { result } = renderHook(() => useDebouncedCallback(fn, 500));
    result.current.flush();
    expect(fn).not.toHaveBeenCalled();
  });

  it('cancel() drops the pending call', () => {
    const fn = vi.fn();
    const { result } = renderHook(() => useDebouncedCallback(fn, 500));
    result.current.call('x');
    result.current.cancel();
    vi.advanceTimersByTime(500);
    result.current.flush();
    expect(fn).not.toHaveBeenCalled();
  });

  it('flushes a pending call on unmount', () => {
    const fn = vi.fn();
    const { result, unmount } = renderHook(() => useDebouncedCallback(fn, 500));
    result.current.call('bye');
    unmount();
    expect(fn).toHaveBeenCalledExactlyOnceWith('bye');
  });

  it('always calls the latest closure', () => {
    let seen = '';
    const { result, rerender } = renderHook(({ tag }) => useDebouncedCallback(() => (seen = tag), 500), {
      initialProps: { tag: 'first' },
    });
    result.current.call();
    rerender({ tag: 'second' });
    vi.advanceTimersByTime(500);
    expect(seen).toBe('second');
  });
});
