import { useCallback, useEffect, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { api } from '../../api/client';
import { showError } from '../../stores/toast';
import type { GameSnapshot } from '../../api/types';
import { findCardByCoordinate } from './coordinates';
import type { TableCardContext } from './cardCommands';
import { submitHeaders } from './submitId';

// Blood counters on the card vs. a pool<->card transfer — different commands
// and (for transfers) a matching pool delta, but the same +/- stepper UX.
export type CounterKind = 'blood' | 'transfer';

// How long to wait for more taps before sending the merged command. Long
// enough that a burst of clicks collapses into one request, short enough that
// a single tap still feels immediate to commit.
const FLUSH_DELAY_MS = 400;

interface PendingBump {
  ctx: TableCardContext;
  kind: CounterKind;
  delta: number;
}

function firstName(controller: string): string {
  return controller.split(' ')[0];
}

// Mirrors cardActions.addCounter/transferToCard exactly, but with an arbitrary
// magnitude: `blood <player> <region> <coord> +N` / `transfer <region> <coord> +N`.
function buildCommand({ ctx, kind, delta }: PendingBump): string {
  const amount = `${delta > 0 ? '+' : '-'}${Math.abs(delta)}`;
  return kind === 'transfer'
    ? `transfer ${ctx.regionCommandKey} ${ctx.coordinate} ${amount}`
    : `blood ${firstName(ctx.controller)} ${ctx.regionCommandKey} ${ctx.coordinate} ${amount}`;
}

// Apply one delta to a (cloned) snapshot: the card's counter, plus — for a
// transfer — the controller's pool moving the opposite way.
function patchSnapshot(game: GameSnapshot, ctx: TableCardContext, kind: CounterKind, delta: number): void {
  const card = findCardByCoordinate(game, ctx.controller, ctx.regionType, ctx.coordinate);
  if (!card) return;
  card.counters = Math.max(0, card.counters + delta);
  if (kind === 'transfer') {
    const player = game.players.find((p) => p.name === firstName(ctx.controller));
    if (player) player.pool = Math.max(0, player.pool - delta);
  }
}

/**
 * Instant (optimistic) + coalesced counter stepping.
 *
 * Each +/- tap patches the cached game snapshot synchronously — the stepper
 * number, the on-table counter badge and (for transfers) the pool all move
 * immediately, with no wait for the ~250ms server round trip — while the
 * actual `blood`/`transfer` command is deferred and merged: five fast taps on
 * a card send one `blood … +5`, not five serialized requests. The authoritative
 * snapshot from that one request then reconciles the cache.
 *
 * `applyUpdate` replaces GamePage's plain `setQueryData`: every snapshot written
 * through it (command responses, end-turn, card modals) has any *still-pending*
 * counter deltas re-applied on top, so a normal command landing mid-burst
 * doesn't visually revert an in-progress bump. (A WS-driven refetch bypasses
 * this and can flicker until the next flush ~400ms later — rare, since the
 * actor's own echo is excluded server-side.)
 */
export function useCounterBump(gameId: string | undefined) {
  const queryClient = useQueryClient();
  const pending = useRef(new Map<string, PendingBump>());
  const timer = useRef<number | null>(null);
  const flushing = useRef(false);

  const applyPendingTo = useCallback((base: GameSnapshot): GameSnapshot => {
    if (pending.current.size === 0) return base;
    const next = structuredClone(base);
    for (const p of pending.current.values()) patchSnapshot(next, p.ctx, p.kind, p.delta);
    return next;
  }, []);

  const applyUpdate = useCallback(
    (snap: GameSnapshot) => {
      if (gameId) queryClient.setQueryData(['game', gameId], applyPendingTo(snap));
    },
    [queryClient, gameId, applyPendingTo],
  );

  const flush = useCallback(async () => {
    if (flushing.current || !gameId) return;
    flushing.current = true;
    try {
      while (pending.current.size > 0) {
        const entry = pending.current.entries().next().value as [string, PendingBump];
        pending.current.delete(entry[0]);
        const bump = entry[1];
        if (bump.delta === 0) continue;
        try {
          const snap = await api.post<GameSnapshot>(
            `/game/${gameId}/view/submit`,
            {
              phase: null,
              command: buildCommand(bump),
              chat: null,
              ping: null,
            },
            submitHeaders(),
          );
          applyUpdate(snap); // re-applies any deltas queued while this was in flight
        } catch (err) {
          console.error('Failed to update counters', err);
          showError('Failed to update counters.');
          queryClient.invalidateQueries({ queryKey: ['game', gameId] });
        }
      }
    } finally {
      flushing.current = false;
    }
  }, [gameId, applyUpdate, queryClient]);

  const bump = useCallback(
    (ctx: TableCardContext, kind: CounterKind, step: number) => {
      if (!gameId) return;
      const key = `${kind}|${ctx.controller}|${ctx.regionType}|${ctx.coordinate}`;
      const existing = pending.current.get(key);
      pending.current.set(key, { ctx, kind, delta: (existing?.delta ?? 0) + step });

      const current = queryClient.getQueryData<GameSnapshot>(['game', gameId]);
      if (current) {
        const next = structuredClone(current);
        patchSnapshot(next, ctx, kind, step);
        queryClient.setQueryData(['game', gameId], next);
      }

      if (timer.current) window.clearTimeout(timer.current);
      timer.current = window.setTimeout(() => {
        timer.current = null;
        void flush();
      }, FLUSH_DELAY_MS);
    },
    [gameId, queryClient, flush],
  );

  // Flush anything still pending if the page unmounts mid-burst.
  useEffect(
    () => () => {
      if (timer.current) window.clearTimeout(timer.current);
      if (pending.current.size > 0) void flush();
    },
    [flush],
  );

  return { bump, applyUpdate };
}
