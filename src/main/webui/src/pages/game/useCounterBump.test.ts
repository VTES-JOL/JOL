import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { createElement, type ReactNode } from 'react';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCounterBump } from './useCounterBump';
import { api } from '../../api/client';
import { showError } from '../../stores/toast';
import type { CardSnapshot, GameSnapshot, PlayerSnapshot, RegionSnapshot } from '../../api/types';
import type { TableCardContext } from './cardCommands';

vi.mock('../../api/client', () => ({ api: { post: vi.fn() } }));
vi.mock('../../stores/toast', () => ({ showError: vi.fn() }));

function card(id: string): CardSnapshot {
  return { id, visible: true, counters: 2, cardId: id, name: id, minion: true };
}
function region(type: string, cards: CardSnapshot[]): RegionSnapshot {
  return { type, commandKey: type.toLowerCase(), label: type, simple: false, openHand: false, hiddenHand: false, cards };
}
function player(name: string): PlayerSnapshot {
  return { name, pool: 10, victoryPoints: 0, active: true, edge: false, pinged: false, regions: [region('READY', [card('Vamp')])] };
}
function snapshot(): GameSnapshot {
  return {
    id: 'g1', name: 'G', players: [player('P1')], seating: ['P1'], chat: [], commandErrors: [],
    currentPlayer: 'P1', edgePlayer: 'P1', turn: '1', turnLabel: 'Turn 1', phase: 'Unlock',
    phases: ['Unlock'], turns: [], pingOptions: [], player: true, admin: false, judge: false,
    globalNotes: null, privateNotes: null, edgeColor: '#fff', edgeTextColor: 'black',
    status: null, stamp: 1, rejected: false, judgeRequest: null,
  };
}

const ctx: TableCardContext = {
  controller: 'P1', controllerPool: 10, regionType: 'READY', regionCommandKey: 'ready',
  coordinate: '1', card: card('Vamp'), isChild: false, controlledByViewer: true,
};

let qc: QueryClient;
function setup() {
  qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  qc.setQueryData(['game', 'g1'], snapshot());
  const wrapper = ({ children }: { children: ReactNode }) =>
    createElement(QueryClientProvider, { client: qc }, children);
  return renderHook(() => useCounterBump('g1'), { wrapper });
}
const counters = () =>
  qc.getQueryData<GameSnapshot>(['game', 'g1'])!.players[0].regions[0].cards[0].counters;
const pool = () => qc.getQueryData<GameSnapshot>(['game', 'g1'])!.players[0].pool;

beforeEach(() => {
  vi.useFakeTimers();
  vi.mocked(api.post).mockReset().mockImplementation(async () => snapshot());
  vi.mocked(showError).mockReset();
});
afterEach(() => vi.useRealTimers());

describe('useCounterBump', () => {
  it('patches the cached snapshot immediately, before any request', () => {
    const { result } = setup();
    result.current.bump(ctx, 'blood', 1);
    expect(counters()).toBe(3);
    expect(api.post).not.toHaveBeenCalled();
  });

  it('merges a burst into one request with the net delta', async () => {
    const { result } = setup();
    for (let i = 0; i < 5; i++) result.current.bump(ctx, 'blood', 1);
    expect(counters()).toBe(7); // 2 + 5, shown instantly
    expect(api.post).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(400);
    expect(api.post).toHaveBeenCalledExactlyOnceWith(
      '/game/g1/view/submit',
      { phase: null, command: 'blood P1 ready 1 +5', chat: null, ping: null },
      { 'X-Submit-Id': expect.any(String) },
    );
  });

  it('cancels out +/- taps and sends nothing when the net delta is zero', async () => {
    const { result } = setup();
    result.current.bump(ctx, 'blood', 1);
    result.current.bump(ctx, 'blood', -1);
    expect(counters()).toBe(2);
    await vi.advanceTimersByTimeAsync(400);
    expect(api.post).not.toHaveBeenCalled();
  });

  it('a transfer also moves the controller pool the opposite way', async () => {
    const { result } = setup();
    result.current.bump(ctx, 'transfer', 1);
    expect(counters()).toBe(3);
    expect(pool()).toBe(9);
    await vi.advanceTimersByTimeAsync(400);
    expect(api.post).toHaveBeenCalledExactlyOnceWith(
      '/game/g1/view/submit',
      { phase: null, command: 'transfer ready 1 +1', chat: null, ping: null },
      { 'X-Submit-Id': expect.any(String) },
    );
  });

  it('applyUpdate re-applies still-pending deltas on top of a fresh snapshot', () => {
    const { result } = setup();
    result.current.bump(ctx, 'blood', 2); // pending, not yet flushed
    result.current.applyUpdate(snapshot()); // e.g. an unrelated command response
    expect(counters()).toBe(4); // 2 (fresh) + 2 (pending) — not reverted
  });

  it('toasts on a failed flush', async () => {
    vi.mocked(api.post).mockRejectedValue(new Error('boom'));
    const { result } = setup();
    result.current.bump(ctx, 'blood', 1);
    await vi.advanceTimersByTimeAsync(400);
    expect(showError).toHaveBeenCalledWith('Failed to update counters.');
  });
});
