// Discipline / virtue codes that have an `.icon.<code>` rule in
// styles/card-visuals.css. Mirrors net.deckserver.services.ParserService's
// server-side set. Used to decide whether a `[disc:<code>]` token renders as
// an icon or falls back to bracketed text (an unknown class renders as an
// invisible empty span otherwise).
const DISCIPLINE_CODES = new Set([
  'abo', 'ani', 'aus', 'cel', 'chi', 'dai', 'dem', 'dom', 'for', 'mal', 'mel',
  'myt', 'nec', 'obe', 'obf', 'obl', 'obt', 'pot', 'pre', 'pro', 'qui', 'san',
  'ser', 'spi', 'str', 'tem', 'tha', 'thn', 'val', 'vic', 'vin', 'vis',
  'flight',
]);

export function isKnownDisciplineCode(code: string): boolean {
  return DISCIPLINE_CODES.has(code.toLowerCase());
}
