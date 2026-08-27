import './Path.css';

// Mirrors net.deckserver.game.enums.Path — same keys, same order.
export const PATH = {
  CAINE: 'Caine',
  CATHARI: 'Cathari',
  DEATH_AND_THE_SOUL: 'Death and the Soul',
  POWER_AND_THE_INNER_VOICE: 'Power and the Inner Voice',
} as const;

export type PathCode = keyof typeof PATH;

// See Clan.tsx's resolveClan for why this accepts either the raw enum
// constant or the human description, and mirrors Path.java's from()/of().
export function resolvePath(value?: string | null): PathCode | undefined {
  if (!value) return undefined;
  const key = value.toUpperCase().replace(/[ -]/g, '_');
  if (key in PATH) return key as PathCode;
  const entry = (Object.entries(PATH) as [PathCode, string][]).find(([, name]) => name.toLowerCase() === value.toLowerCase());
  return entry?.[0];
}

/**
 * Path glyph — renders through the `.path.<code>` icon-font classes in
 * card-visuals.css. See Clan.tsx's Clan component for why `role="img"` +
 * `aria-label` are needed here (the glyph otherwise has no accessible
 * name). Renders nothing for an unresolved/NONE value, so callers don't
 * need their own `!== 'NONE'` guard.
 */
export function Path({ value, className }: { value?: string | null; className?: string }) {
  const code = resolvePath(value);
  if (!code) return null;
  const name = PATH[code];
  return <span className={`path ${code.toLowerCase()}${className ? ` ${className}` : ''}`} role="img" aria-label={name} title={name} />;
}
