import type { CardDetail, Deck, ExtendedDeck } from '../../api/types';
import { type DeckEntry, groupEntries } from './deckKit';

/**
 * Flattens a stored `Deck` (crypt list + type-grouped library lists) into the
 * flat `DeckEntry[]` the structured editor and the read-only `DeckView` work
 * with.
 *
 * The stored deck carries only id / name / count per card — `group`, `banned`,
 * `advanced` and the crypt display data come from the enrichment detail map
 * (see {@link enrichEntries}); `types` starts as a best-effort placeholder
 * (the library group's type, `["Vampire"]` for crypt) and is refined once
 * detail is merged.
 */
export function entriesFromDeck(deck: Deck | null): DeckEntry[] {
  if (!deck) return [];
  const out: DeckEntry[] = [];

  for (const c of deck.crypt.cards) {
    out.push({
      cardId: String(c.id),
      name: c.name,
      count: c.count,
      isCrypt: true,
      types: ['Vampire'],
      banned: false,
    });
  }
  for (const group of deck.library.cards) {
    for (const c of group.cards) {
      out.push({
        cardId: String(c.id),
        name: c.name,
        count: c.count,
        isCrypt: false,
        types: group.type ? group.type.split('/') : [],
        banned: false,
      });
    }
  }
  return out;
}

/** As {@link entriesFromDeck}, taking the editor's server-resolved `ExtendedDeck` wrapper. */
export function entriesFromExtendedDeck(extended: ExtendedDeck | null): DeckEntry[] {
  return entriesFromDeck(extended?.deck ?? null);
}

/** All card ids referenced by a set of entries — the argument for `/cards/details`. */
export function entryIds(entries: DeckEntry[]): string[] {
  return entries.map((e) => e.cardId);
}

/**
 * Serialises entries back to the canonical deck-list text the server parses
 * (`DeckParser`) — crypt block, blank line, then library grouped by type.
 * Matches `DeckService.getDeckContents`'s `"{count} x {name}"` line format so
 * a load → edit → save → reload round-trips cleanly.
 */
export function entriesToContents(entries: DeckEntry[]): string {
  const line = (e: DeckEntry) => `${e.count} x ${e.name}`;
  const groups = groupEntries(entries);
  const crypt = groups.find((g) => g.key === 'Crypt');
  const library = groups.filter((g) => g.key !== 'Crypt');

  const cryptLines = (crypt?.entries ?? []).map(line);
  const libraryLines = library.flatMap((g) => g.entries.map(line));

  return [...cryptLines, '', ...libraryLines].join('\n');
}

/** Merges `/cards/details` results into entries, filling type/group/banned/advanced. */
export function enrichEntries(entries: DeckEntry[], details: Map<string, CardDetail>): DeckEntry[] {
  return entries.map((e) => {
    const d = details.get(e.cardId);
    if (!d) return e;
    return {
      ...e,
      types: d.types.length ? d.types : e.types,
      group: d.group ?? undefined,
      banned: d.banned,
      advanced: d.advanced,
    };
  });
}
