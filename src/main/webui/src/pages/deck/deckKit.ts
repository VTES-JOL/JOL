// Pure deck-analysis helpers + types, ported from the jol-quarkus rewrite's
// features/deck/{types,deckUtils}.ts. No API calls, no rendering — safe to use
// now (e.g. to compute the existing deck preview's summary) and the vocabulary
// the future Tailwind deck editor is built on.

export interface DeckSummary {
  crypt: number;
  library: number;
  /** Sorted group numbers joined by "/", e.g. "4/5". Null when all crypt are ANY-group. */
  groups: string | null;
}

export interface DeckEntry {
  cardId: string;
  name: string;
  count: number;
  isCrypt: boolean;
  /** Crypt: ["Vampire"] | ["Imbued"]. Library: the card's type list. */
  types: string[];
  /** Crypt only: "1"–"7" | "ANY". */
  group?: string;
  banned: boolean;
  advanced?: boolean;
}

export interface CardGroup {
  key: string;
  entries: DeckEntry[];
  total: number;
}

/**
 * Groups entries for display: crypt cards form a single "Crypt" group; library
 * cards group by their type key (multi-type → combined "Action/Combat" key).
 * Cards within a group are sorted by name; Crypt first, then groups alphabetically.
 */
export function groupEntries(entries: DeckEntry[]): CardGroup[] {
  const map = new Map<string, DeckEntry[]>();

  for (const entry of entries) {
    const key = entry.isCrypt ? 'Crypt' : entry.types.join('/');
    const existing = map.get(key) ?? [];
    existing.push(entry);
    map.set(key, existing);
  }

  return [...map.entries()]
    .sort(([a], [b]) => {
      if (a === 'Crypt') return -1;
      if (b === 'Crypt') return 1;
      return a.localeCompare(b);
    })
    .map(([key, groupEntries]) => ({
      key,
      entries: [...groupEntries].sort((a, b) => a.name.localeCompare(b.name)),
      total: groupEntries.reduce((sum, e) => sum + e.count, 0),
    }));
}

/** Live summary from the current entries; null only when the deck is empty. */
export function computeSummary(entries: DeckEntry[]): DeckSummary | null {
  if (entries.length === 0) return null;

  const cryptEntries = entries.filter((e) => e.isCrypt);
  const libEntries = entries.filter((e) => !e.isCrypt);

  const crypt = cryptEntries.reduce((sum, e) => sum + e.count, 0);
  const library = libEntries.reduce((sum, e) => sum + e.count, 0);

  const groups =
    [...new Set(cryptEntries.filter((e) => e.group && e.group !== 'ANY').map((e) => e.group!))]
      .sort((a, b) => parseInt(a) - parseInt(b))
      .join('/') || null;

  return { crypt, library, groups };
}

/** Parses the compact "{crypt},{library},{groups}" summary string. */
export function parseSummary(compact: string | null): DeckSummary | null {
  if (!compact) return null;
  const [cryptStr, libraryStr, groupsStr] = compact.split(',');
  return {
    crypt: parseInt(cryptStr, 10),
    library: parseInt(libraryStr, 10),
    groups: groupsStr || null,
  };
}

export function formatSummaryCompact(summary: DeckSummary): string {
  return `${summary.crypt},${summary.library},${summary.groups ?? ''}`;
}

export function getBannedEntries(entries: DeckEntry[]): DeckEntry[] {
  return entries.filter((e) => e.banned);
}
