// Tournament game names follow "<Tournament>: Round N - Table M"; everything
// else is a casual game. Splitting the name lets both watch tabs show a
// clean title + a "Round N · Table M" subtitle and a Tournament/Casual badge.
const TOURNAMENT_NAME = /^(.*): Round (\d+) - Table (\d+)$/;

export interface GameTitle {
  tournament: boolean;
  title: string;
  /** "Round 2 · Table 3" for tournament games, else "". */
  sub: string;
}

export function parseGameTitle(name: string | null | undefined): GameTitle {
  const m = TOURNAMENT_NAME.exec(name ?? '');
  if (m) return { tournament: true, title: m[1], sub: `Round ${m[2]} · Table ${m[3]}` };
  return { tournament: false, title: name || 'Unnamed game', sub: '' };
}
