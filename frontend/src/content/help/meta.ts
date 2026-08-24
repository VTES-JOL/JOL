export interface HelpSectionMeta {
  slug: string;
  title: string;
  summary: string;
}

// Ordered list driving both the sidebar/accordion nav and the section
// routes — add a new section by adding one entry here plus a matching
// `<slug>.mdx` file in this directory.
export const HELP_SECTIONS: HelpSectionMeta[] = [
  { slug: 'lobby', title: 'Lobby & Joining Games', summary: 'Create or join a game, and register a deck to play it.' },
  { slug: 'deck-editor', title: 'Deck Editor', summary: 'Card names, the deck text format, and the deck editor.' },
  { slug: 'game-table-basics', title: 'Game Table Basics', summary: 'Commands, and how they target a card.' },
  { slug: 'playing-and-moving-cards', title: 'Playing & Moving Cards', summary: 'play, influence, draw, discard, burn, move, shuffle.' },
  { slug: 'pool-blood-counters', title: 'Pool, Blood & Counters', summary: 'pool, blood, transfer.' },
  { slug: 'card-details', title: 'Card Details & Labels', summary: 'lock, label, clan, capacity, disciplines, contest, sect, path, votes, flip.' },
  { slug: 'game-management', title: 'Game Management', summary: 'Victory points, timeouts, seating order, the edge, hidden choices.' },
  { slug: 'tournament-administration', title: 'Tournament Administration', summary: 'Running a tournament from creation through finals.' },
  { slug: 'chat-formatting', title: 'Chat & Text Formatting', summary: 'Card links, emoji, and the direct-action icon.' },
];
