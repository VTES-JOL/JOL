// Mirrors net.deckserver.game.enums.Sect — same keys, same order.
export const SECT = {
  CAMARILLA: 'Camarilla',
  SABBAT: 'Sabbat',
  INDEPENDENT: 'Independent',
  LAIBON: 'Laibon',
  ANARCH: 'Anarch',
} as const;

export type SectCode = keyof typeof SECT;

// See Clan.tsx's resolveClan for why this accepts either the raw enum
// constant or the human description, and mirrors Sect.java's from()/of().
export function resolveSect(value?: string | null): SectCode | undefined {
  if (!value) return undefined;
  const key = value.toUpperCase().replace(/[ -]/g, '_');
  if (key in SECT) return key as SectCode;
  const entry = (Object.entries(SECT) as [SectCode, string][]).find(([, name]) => name.toLowerCase() === value.toLowerCase());
  return entry?.[0];
}

/**
 * Sect label — unlike Clan/Path there's no icon-font glyph for sect, so this
 * renders the real word (no accessibility gap to begin with), but still
 * normalizes casing: the raw enum constant this used to render directly
 * read as "CAMARILLA" rather than "Camarilla". Renders nothing for an
 * unresolved/NONE value, so callers don't need their own `!== 'NONE'` guard.
 */
export function Sect({ value, className }: { value?: string | null; className?: string }) {
  const code = resolveSect(value);
  if (!code) return null;
  return <small className={`sect${className ? ` ${className}` : ''}`}>{SECT[code]}</small>;
}
