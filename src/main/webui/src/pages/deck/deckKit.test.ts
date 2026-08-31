import { describe, expect, it } from 'vitest';
import {
  type DeckEntry,
  computeSummary,
  formatSummaryCompact,
  getBannedEntries,
  groupEntries,
  parseSummary,
} from './deckKit';

const crypt = (name: string, count: number, group?: string, extra: Partial<DeckEntry> = {}): DeckEntry => ({
  cardId: name,
  name,
  count,
  isCrypt: true,
  types: ['Vampire'],
  group,
  banned: false,
  ...extra,
});

const lib = (name: string, count: number, types: string[], extra: Partial<DeckEntry> = {}): DeckEntry => ({
  cardId: name,
  name,
  count,
  isCrypt: false,
  types,
  banned: false,
  ...extra,
});

describe('groupEntries', () => {
  it('puts Crypt first, then library type groups alphabetically, cards sorted by name', () => {
    const groups = groupEntries([
      lib('Govern the Unaligned', 6, ['Action']),
      crypt('Zoe', 2, '2'),
      lib('Deflection', 4, ['Reaction']),
      crypt('Anson', 1, '1'),
      lib('Aire of Elation', 2, ['Action Modifier']),
    ]);

    expect(groups.map((g) => g.key)).toEqual(['Crypt', 'Action', 'Action Modifier', 'Reaction']);
    expect(groups[0].entries.map((e) => e.name)).toEqual(['Anson', 'Zoe']);
    expect(groups[0].total).toBe(3);
  });

  it('combines multi-type cards into their own group key', () => {
    const groups = groupEntries([lib('Carrion Crows', 3, ['Action', 'Combat'])]);
    expect(groups[0].key).toBe('Action/Combat');
  });
});

describe('computeSummary', () => {
  it('returns null for an empty deck', () => {
    expect(computeSummary([])).toBeNull();
  });

  it('sums counts and lists distinct non-ANY groups sorted numerically', () => {
    const summary = computeSummary([
      crypt('Anson', 4, '1'),
      crypt('Zoe', 8, '2'),
      crypt('Anarch Convert', 1, 'ANY'),
      lib('Govern the Unaligned', 10, ['Action']),
      lib('Deflection', 12, ['Reaction']),
    ]);
    expect(summary).toEqual({ crypt: 13, library: 22, groups: '1/2' });
  });

  it('groups is null when every crypt card is ANY-group', () => {
    expect(computeSummary([crypt('Anarch Convert', 12, 'ANY')])?.groups).toBeNull();
  });
});

describe('parseSummary / formatSummaryCompact round-trip', () => {
  it('parses the compact form', () => {
    expect(parseSummary('12,80,4/5')).toEqual({ crypt: 12, library: 80, groups: '4/5' });
    expect(parseSummary('12,80,')).toEqual({ crypt: 12, library: 80, groups: null });
    expect(parseSummary(null)).toBeNull();
  });

  it('round-trips', () => {
    const s = { crypt: 12, library: 80, groups: '4/5' as string | null };
    expect(parseSummary(formatSummaryCompact(s))).toEqual(s);
    const noGroups = { crypt: 12, library: 80, groups: null };
    expect(parseSummary(formatSummaryCompact(noGroups))).toEqual(noGroups);
  });
});

describe('getBannedEntries', () => {
  it('filters to banned cards only', () => {
    const entries = [crypt('Anson', 1, '1'), lib('Banned Card', 1, ['Master'], { banned: true })];
    expect(getBannedEntries(entries).map((e) => e.name)).toEqual(['Banned Card']);
  });
});
