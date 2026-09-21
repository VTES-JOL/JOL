import { describe, expect, it } from 'vitest';
import { buildRingModel } from './ringModel';
import { availableLevels, coerceLevel, defaultFocus, neighbours, resolveFocus } from './seatFocus';
import { boardViewReducer, initialBoardView, normalizeBoardView } from './boardViewState';
import { TURN9_SEATING, TURN9_VIEWER, turn9Players, turn9Table } from './__fixtures__/ringFixtures';

const five = () => buildRingModel(turn9Players(), TURN9_SEATING, { viewerName: TURN9_VIEWER }).seats;
const withOusted = (name: string) =>
  buildRingModel(turn9Players().map((p) => (p.name === name ? { ...p, pool: 0 } : p)), TURN9_SEATING).seats;

describe('neighbours', () => {
  it('derives prey (next) and predator (previous) from table order, wrapping', () => {
    const s = five();
    expect(neighbours(s, 'Lysette Marchetti')).toMatchObject({ prey: { name: 'Ilya Rostova' }, predator: { name: 'Sable Voss' } });
    expect(neighbours(s, 'Marcus Kane').predator?.name).toBe('The Baron');
    expect(neighbours(s, 'The Baron').prey?.name).toBe('Marcus Kane');
  });

  it('skips ousted seats', () => {
    const s = withOusted('Ilya Rostova');
    expect(neighbours(s, 'Lysette Marchetti').prey?.name).toBe('The Baron');
  });

  it('prefers the server-supplied prey / predator', () => {
    const players = turn9Players().map((p) => (p.name === 'Lysette Marchetti' ? { ...p, prey: 'Marcus Kane', predator: 'Ilya Rostova' } : p));
    const s = buildRingModel(players, TURN9_SEATING).seats;
    expect(neighbours(s, 'Lysette Marchetti')).toMatchObject({ prey: { name: 'Marcus Kane' }, predator: { name: 'Ilya Rostova' } });
  });
});

describe('levels', () => {
  it('offers the triad only with three or more live seats', () => {
    expect(availableLevels(five())).toEqual(['table', 'triad', 'seat']);
    const two = buildRingModel(turn9Table(2).players, turn9Table(2).seating).seats;
    expect(availableLevels(two)).toEqual(['table', 'seat']);
    expect(coerceLevel('triad', two)).toBe('seat');
    expect(availableLevels(withOusted('The Baron').map((s, i) => (i > 1 ? { ...s, ousted: true } : s)))).toEqual(['table', 'seat']);
  });
});

describe('focus', () => {
  it('defaults to the viewer, then the edge holder, then the first live seat', () => {
    expect(defaultFocus(five(), TURN9_VIEWER)).toBe('Lysette Marchetti');
    expect(defaultFocus(five(), null)).toBe('Marcus Kane'); // edge holder in the fixture
    expect(defaultFocus(five().map((s) => ({ ...s, edge: false, ousted: s.name === 'Marcus Kane' })), null)).toBe('Sable Voss');
  });
  it('discards a stale focus', () => {
    expect(resolveFocus('Nobody', five(), TURN9_VIEWER)).toBe('Lysette Marchetti');
  });
});

describe('boardViewReducer', () => {
  const ctx = { seats: five(), viewerName: TURN9_VIEWER };
  const run = (...actions: Parameters<typeof boardViewReducer>[1][]) =>
    actions.reduce((s, a) => boardViewReducer(s, a, ctx), initialBoardView());

  it('starts at the table, focused on the viewer', () => {
    expect(normalizeBoardView(initialBoardView(), ctx)).toMatchObject({ level: 'table', focus: 'Lysette Marchetti', layout: null });
  });
  it('drills table → triad → seat and stops', () => {
    expect(run({ type: 'drill' }).level).toBe('triad');
    expect(run({ type: 'drill' }, { type: 'drill' }).level).toBe('seat');
    expect(run({ type: 'drill' }, { type: 'drill' }, { type: 'drill' }).level).toBe('seat');
  });
  it('opens a seat in one step: focus + one level deeper', () => {
    expect(run({ type: 'open', name: 'The Baron' })).toMatchObject({ level: 'triad', focus: 'The Baron' });
    expect(run({ type: 'level', level: 'triad' }, { type: 'open', name: 'The Baron' }).level).toBe('seat');
  });
  it('drills straight to seat in a two-player game', () => {
    const two = buildRingModel(turn9Table(2).players, turn9Table(2).seating).seats;
    const s = boardViewReducer(initialBoardView(), { type: 'drill' }, { seats: two });
    expect(s.level).toBe('seat');
  });
  it('re-centres on a neighbour and steps prey / predator', () => {
    const s = run({ type: 'level', level: 'triad' }, { type: 'step', dir: 'prey' });
    expect(s).toMatchObject({ level: 'triad', focus: 'Ilya Rostova' });
    expect(run({ type: 'step', dir: 'predator' }).focus).toBe('Sable Voss');
  });
  it('keeps level and focus independent of the layout toggle', () => {
    const s = run({ type: 'level', level: 'triad' }, { type: 'focus', name: 'The Baron' }, { type: 'layout', layout: 'panels' });
    expect(s).toEqual({ level: 'triad', focus: 'The Baron', layout: 'panels' });
    expect(boardViewReducer(s, { type: 'layout', layout: null }, ctx).layout).toBeNull();
  });
  it('coerces a restored triad when seats drop below three', () => {
    const two = buildRingModel(turn9Table(2).players, turn9Table(2).seating).seats;
    expect(normalizeBoardView({ level: 'triad', focus: 'Nobody', layout: null }, { seats: two }).level).toBe('seat');
  });
});
