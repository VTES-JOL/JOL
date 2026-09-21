import { describe, expect, it } from 'vitest';
import { bloodFraction, cardLabel, cardView, kindOf, shortNameOf, titleTagFor } from './cardView';
import { card, gun, hiddenCard, minion, retainer } from '../__fixtures__/gameFixtures';
import { ally, location } from './__fixtures__/ringFixtures';

describe('cardView', () => {
  it('classifies kinds', () => {
    expect(kindOf(minion('Anson'))).toBe('vampire');
    expect(kindOf(ally('War Ghoul', 3))).toBe('ally');
    expect(kindOf(location('Powerbase: Montreal'))).toBe('location');
    expect(kindOf(card('Govern the Unaligned'))).toBe('other');
  });

  it('shortens names at the first comma', () => {
    expect(shortNameOf('Muaziz, Archon of Ulugh Beg')).toBe('Muaziz');
    expect(shortNameOf('Zane')).toBe('Zane');
  });

  it('maps titles from the label, ignoring free text', () => {
    expect(titleTagFor('Prince')).toBe('P');
    expect(titleTagFor(' primogen ')).toBe('Pg');
    expect(titleTagFor('bounce Player3')).toBeNull();
    expect(titleTagFor(undefined)).toBeNull();
  });

  it('carries state flags and attachments', () => {
    const v = cardView(minion('Cristo', { locked: true, contested: true, counters: 2, capacity: 5, cards: [gun(), retainer(), card('X')] }), {
      torpor: true,
    });
    expect(v).toMatchObject({ locked: true, contested: true, torpor: true, counters: 2, capacity: 5 });
    expect(v.attachments).toEqual(['equipment', 'retainer', 'other']);
  });

  it('never leaks the identity of a hidden card', () => {
    const v = cardView(hiddenCard({ name: 'Secret', counters: 3 }));
    expect(v.hidden).toBe(true);
    expect(v.name).toBe('');
    expect(cardLabel(v)).toBe('Hidden card');
  });

  it('describes a card in words', () => {
    const v = cardView(minion('Zane', { counters: 2, capacity: 5, locked: true, contested: true, label: 'Primogen' }));
    expect(cardLabel(v)).toBe('Zane, blood 2 of 5, title Pg, locked, contested');
  });

  it('clamps the blood fraction', () => {
    expect(bloodFraction({ counters: 3, capacity: 6 })).toBe(0.5);
    expect(bloodFraction({ counters: 9, capacity: 6 })).toBe(1);
    expect(bloodFraction({ counters: 3, capacity: 0 })).toBe(0);
  });
});
