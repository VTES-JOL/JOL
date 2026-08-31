import { describe, expect, it } from 'vitest';
import type { CardDetail, ExtendedDeck } from '../../api/types';
import type { DeckEntry } from './deckKit';
import { enrichEntries, entriesFromExtendedDeck, entriesToContents, entryIds } from './deckEntries';

const extended: ExtendedDeck = {
  deck: {
    id: 'x',
    name: 'Test',
    comments: '',
    player: 'p',
    author: 'p',
    crypt: { count: 3, cards: [{ id: 200001, name: 'Aabbt Kindred', count: 2, comments: '' }, { id: 200002, name: 'Aaron Bathurst', count: 1, comments: '' }] },
    library: {
      count: 16,
      cards: [
        { type: 'Action', count: 10, cards: [{ id: 101228, name: 'Govern the Unaligned', count: 10, comments: '' }] },
        { type: 'Action/Combat', count: 6, cards: [{ id: 100730, name: 'Carrion Crows', count: 6, comments: '' }] },
      ],
    },
  },
  stats: { cryptSize: 3, librarySize: 16, groups: ['1', '2'], bannedCards: false, summary: '' },
  errors: [],
};

describe('entriesFromExtendedDeck', () => {
  it('flattens crypt + type-grouped library into DeckEntry[]', () => {
    const entries = entriesFromExtendedDeck(extended);
    expect(entries).toHaveLength(4);
    expect(entries.filter((e) => e.isCrypt).map((e) => e.name)).toEqual(['Aabbt Kindred', 'Aaron Bathurst']);
    const crows = entries.find((e) => e.name === 'Carrion Crows')!;
    expect(crows.isCrypt).toBe(false);
    expect(crows.types).toEqual(['Action', 'Combat']);
  });

  it('returns [] for a null deck', () => {
    expect(entriesFromExtendedDeck(null)).toEqual([]);
  });
});

describe('entriesToContents', () => {
  it('emits crypt block, blank line, then library, in "{count} x {name}" form', () => {
    const text = entriesToContents(entriesFromExtendedDeck(extended));
    expect(text).toBe(
      ['2 x Aabbt Kindred', '1 x Aaron Bathurst', '', '10 x Govern the Unaligned', '6 x Carrion Crows'].join('\n'),
    );
  });

  it('round-trips through the flatten step (structure preserved)', () => {
    const first = entriesFromExtendedDeck(extended);
    const text = entriesToContents(first);
    // re-parse the text the same way DeckParser would split count/name
    const reparsed: DeckEntry[] = text
      .split('\n')
      .filter(Boolean)
      .map((l) => {
        const m = l.match(/^(\d+) x (.+)$/)!;
        return { cardId: m[2], name: m[2], count: Number(m[1]), isCrypt: false, types: [], banned: false };
      });
    expect(reparsed.map((e) => `${e.count}:${e.name}`).sort()).toEqual(
      first.map((e) => `${e.count}:${e.name}`).sort(),
    );
  });
});

describe('enrichEntries', () => {
  it('fills type/group/banned/advanced from the detail map, leaves unknowns alone', () => {
    const entries = entriesFromExtendedDeck(extended);
    const details = new Map<string, CardDetail>([
      [
        '200001',
        {
          id: '200001', name: 'Aabbt Kindred', crypt: true, types: ['Vampire'], group: '2', banned: false,
          advanced: false, sets: [], clan: 'Follower of Set', path: null, capacity: 4, disciplines: ['for'],
          andDisciplines: [], orDisciplines: [], requirementClans: [], requirementPath: null, poolCost: null, bloodCost: null,
        },
      ],
    ]);
    const enriched = enrichEntries(entries, details);
    expect(enriched.find((e) => e.cardId === '200001')!.group).toBe('2');
    expect(enriched.find((e) => e.cardId === '200002')!.group).toBeUndefined();
  });
});

describe('entryIds', () => {
  it('lists every card id', () => {
    expect(entryIds(entriesFromExtendedDeck(extended))).toEqual(['200001', '200002', '101228', '100730']);
  });
});
