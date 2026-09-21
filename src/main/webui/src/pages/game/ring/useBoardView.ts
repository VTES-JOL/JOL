import { useCallback, useEffect, useMemo, useState } from 'react';
import { resolveLayout, type RingLayoutKind } from './boardPrefs';
import { boardViewReducer, initialBoardView, normalizeBoardView, type BoardViewState } from './boardViewState';
import type { RingSeatModel } from './ringModel';
import { availableLevels, neighbours, type BoardLevel } from './seatFocus';

// Board-view state for one game: level (table / triad / seat), focused seat, and
// the per-game layout toggle. Persisted to sessionStorage per game (a temporary,
// this-visit choice — the durable preference is the profile setting, read by the
// caller and passed as `profileLayout`).

export interface UseBoardViewOptions {
  gameId?: string | null;
  seats: RingSeatModel[];
  viewerName?: string | null;
  /** Level to start at when nothing is stored (Seat on mobile). */
  defaultLevel?: BoardLevel;
  /** Layout from the viewer's profile, if they set one. */
  profileLayout?: RingLayoutKind | null;
  /** Container width in px, for the width-based layout default. */
  width: number;
}

const key = (gameId?: string | null) => `jol-board-view:${gameId ?? 'none'}`;

function load(gameId: string | null | undefined, fallback: BoardViewState): BoardViewState {
  try {
    const raw = sessionStorage.getItem(key(gameId));
    if (!raw) return fallback;
    const v = JSON.parse(raw) as Partial<BoardViewState>;
    return {
      level: v.level === 'triad' || v.level === 'seat' || v.level === 'table' ? v.level : fallback.level,
      focus: typeof v.focus === 'string' ? v.focus : null,
      layout: v.layout === 'wedge' || v.layout === 'panels' ? v.layout : null,
    };
  } catch {
    return fallback; // private window / blocked storage: just don't persist
  }
}

export function useBoardView({ gameId, seats, viewerName, defaultLevel = 'table', profileLayout, width }: UseBoardViewOptions) {
  const ctx = useMemo(() => ({ seats, viewerName }), [seats, viewerName]);
  const [raw, setRaw] = useState<BoardViewState>(() => load(gameId, initialBoardView(defaultLevel)));
  const state = useMemo(() => normalizeBoardView(raw, ctx), [raw, ctx]);

  useEffect(() => {
    try {
      sessionStorage.setItem(key(gameId), JSON.stringify(state));
    } catch {
      /* storage unavailable — the state still works in memory */
    }
  }, [gameId, state]);

  const dispatch = useCallback(
    (a: Parameters<typeof boardViewReducer>[1]) => setRaw((s) => boardViewReducer(s, a, ctx)),
    [ctx],
  );

  const focusSeat = seats.find((s) => s.name === state.focus) ?? null;
  return {
    level: state.level,
    focusName: state.focus,
    focusSeat,
    neighbours: neighbours(seats, state.focus ?? ''),
    levels: availableLevels(seats),
    /** The layout in effect: this game's toggle > profile preference > width default. */
    layout: resolveLayout(state.layout, profileLayout, width),
    setLevel: (level: BoardLevel) => dispatch({ type: 'level', level }),
    setLayout: (layout: RingLayoutKind) => dispatch({ type: 'layout', layout }),
    focus: (name: string) => dispatch({ type: 'focus', name }),
    step: (dir: 'prey' | 'predator') => dispatch({ type: 'step', dir }),
    drill: () => dispatch({ type: 'drill' }),
    open: (name: string) => dispatch({ type: 'open', name }),
  };
}

export type BoardViewApi = ReturnType<typeof useBoardView>;
