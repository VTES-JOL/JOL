import type { RingLayoutKind } from './boardPrefs';
import { availableLevels, coerceLevel, neighbours, resolveFocus, type BoardLevel } from './seatFocus';
import type { RingSeatModel } from './ringModel';

// The board-view state machine: which level, which seat is focused, and the
// per-game layout override. A pure reducer so every transition is unit-tested;
// useBoardView only adds persistence.

export interface BoardViewState {
  level: BoardLevel;
  /** Focused seat name; null until first needed. */
  focus: string | null;
  /** Session-only layout toggle for this game; null = follow profile / width. */
  layout: RingLayoutKind | null;
}

export type BoardViewAction =
  | { type: 'level'; level: BoardLevel }
  | { type: 'focus'; name: string }
  | { type: 'layout'; layout: RingLayoutKind | null }
  | { type: 'step'; dir: 'prey' | 'predator' }
  /** One level deeper: table → triad (or seat when triad is unavailable) → seat. */
  | { type: 'drill' }
  /** Open a seat from the table: focus it and go one level deeper in a single step. */
  | { type: 'open'; name: string };

export interface BoardViewContext {
  seats: RingSeatModel[];
  viewerName?: string | null;
}

export const initialBoardView = (level: BoardLevel = 'table'): BoardViewState => ({ level, focus: null, layout: null });

/** Make a (possibly stale, e.g. restored) state valid for the current seats. */
export function normalizeBoardView(s: BoardViewState, ctx: BoardViewContext): BoardViewState {
  return { ...s, level: coerceLevel(s.level, ctx.seats), focus: resolveFocus(s.focus, ctx.seats, ctx.viewerName) };
}

export function boardViewReducer(state: BoardViewState, action: BoardViewAction, ctx: BoardViewContext): BoardViewState {
  const s = normalizeBoardView(state, ctx);
  switch (action.type) {
    case 'level':
      return { ...s, level: coerceLevel(action.level, ctx.seats) };
    case 'focus':
      return { ...s, focus: resolveFocus(action.name, ctx.seats, ctx.viewerName) };
    case 'layout':
      return { ...s, layout: action.layout };
    case 'step': {
      const n = neighbours(ctx.seats, s.focus ?? '');
      const to = n[action.dir]?.name;
      return to ? { ...s, focus: to } : s;
    }
    case 'open':
      return boardViewReducer({ ...s, focus: resolveFocus(action.name, ctx.seats, ctx.viewerName) }, { type: 'drill' }, ctx);
    case 'drill': {
      const levels = availableLevels(ctx.seats);
      const next = levels[Math.min(levels.indexOf(s.level) + 1, levels.length - 1)];
      return { ...s, level: next };
    }
  }
}
