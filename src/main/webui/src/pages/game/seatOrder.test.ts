import { describe, expect, it } from 'vitest';
import type { PlayerSnapshot } from '../../api/types';
import { relationOf, seatOrder } from './seatOrder';

function seat(name: string, pool = 10): PlayerSnapshot {
  return { name, pool, victoryPoints: 0, active: false, edge: false, pinged: false, regions: [] };
}

// Table: A(pred of B) → B → C(viewer) → D(prey of C) → E, wrapping E → A.
const seating = ['A', 'B', 'C', 'D', 'E'];
const players = seating.map((n) => seat(n));

describe('seatOrder', () => {
  it('rotates opponents to play order starting at the seat after the viewer (prey first)', () => {
    const { me, others } = seatOrder(players, seating, 'C');
    expect(me?.name).toBe('C');
    expect(others.map((p) => p.name)).toEqual(['D', 'E', 'A', 'B']);
  });

  it('wraps for the last seat', () => {
    const { me, others } = seatOrder(players, seating, 'E');
    expect(me?.name).toBe('E');
    expect(others.map((p) => p.name)).toEqual(['A', 'B', 'C', 'D']);
  });

  it('keeps ousted seats in the row (they render collapsed, not removed)', () => {
    const withOust = [seat('A'), seat('B', 0), seat('C'), seat('D'), seat('E')];
    const { others } = seatOrder(withOust, seating, 'C');
    expect(others.map((p) => p.name)).toEqual(['D', 'E', 'A', 'B']);
  });

  it('spectator (not seated) gets every seat in table order', () => {
    const { me, others } = seatOrder(players, seating, 'Zorro');
    expect(me).toBeNull();
    expect(others.map((p) => p.name)).toEqual(seating);
  });

  it('null viewer behaves as a spectator', () => {
    const { me, others } = seatOrder(players, seating, null);
    expect(me).toBeNull();
    expect(others.map((p) => p.name)).toEqual(seating);
  });

  it('tolerates a seating entry with no matching player', () => {
    const { others } = seatOrder([seat('C'), seat('D')], seating, 'C');
    expect(others.map((p) => p.name)).toEqual(['D']);
  });
});

describe('relationOf (fallback)', () => {
  const notOusted = () => false;

  it('names the seat after the viewer as prey and the one before as predator', () => {
    expect(relationOf('D', seating, 'C', notOusted)).toBe('prey');
    expect(relationOf('B', seating, 'C', notOusted)).toBe('predator');
    expect(relationOf('E', seating, 'C', notOusted)).toBeNull();
  });

  it('skips an ousted prey to the next live seat', () => {
    const ousted = (n: string) => n === 'D';
    expect(relationOf('D', seating, 'C', ousted)).toBeNull();
    expect(relationOf('E', seating, 'C', ousted)).toBe('prey');
  });

  it('skips an ousted predator to the previous live seat, wrapping the table', () => {
    const ousted = (n: string) => n === 'B';
    expect(relationOf('B', seating, 'C', ousted)).toBeNull();
    expect(relationOf('A', seating, 'C', ousted)).toBe('predator');
  });

  it('returns null for a spectator or a one-seat table', () => {
    expect(relationOf('A', seating, 'Zorro', notOusted)).toBeNull();
    expect(relationOf('A', ['A'], 'A', notOusted)).toBeNull();
  });

  it('in a two-player game the opponent is both — prey wins (checked first)', () => {
    expect(relationOf('B', ['A', 'B'], 'A', notOusted)).toBe('prey');
  });
});
