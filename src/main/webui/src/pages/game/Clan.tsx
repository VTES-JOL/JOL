import './Clan.css';

// Mirrors net.deckserver.game.enums.Clan — same keys, same order, so the two
// are easy to diff against each other. Kept as a plain data object (not a TS
// `enum`) so resolveClan can iterate its entries directly.
export const CLAN = {
  ABOMINATION: 'Abomination',
  AHRIMANE: 'Ahrimane',
  AKUNANSE: 'Akunanse',
  ASSAMITE: 'Assamite',
  BAALI: 'Baali',
  BLOOD_BROTHER: 'Blood Brother',
  BRUJAH: 'Brujah',
  BRUJAH_ANTITRIBU: 'Brujah Antitribu',
  CAITIFF: 'Caitiff',
  DAUGHTER_OF_CACOPHONY: 'Daughter of Cacophony',
  FOLLOWER_OF_SET: 'Follower of Set',
  GANGREL: 'Gangrel',
  GANGREL_ANTITRIBU: 'Gangrel Antitribu',
  GARGOYLE: 'Gargoyle',
  GIOVANNI: 'Giovanni',
  GURUHI: 'Guruhi',
  HARBINGER_OF_SKULLS: 'Harbinger of Skulls',
  ISHTARRI: 'Ishtarri',
  KIASYD: 'Kiasyd',
  LASOMBRA: 'Lasombra',
  MALKAVIAN: 'Malkavian',
  MALKAVIAN_ANTITRIBU: 'Malkavian Antitribu',
  NAGARAJA: 'Nagaraja',
  NOSFERATU: 'Nosferatu',
  NOSFERATU_ANTITRIBU: 'Nosferatu Antitribu',
  HECATA: 'Hecata',
  OSEBO: 'Osebo',
  PANDER: 'Pander',
  RAVNOS: 'Ravnos',
  SALUBRI: 'Salubri',
  SALUBRI_ANTITRIBU: 'Salubri Antitribu',
  SAMEDI: 'Samedi',
  TOREADOR: 'Toreador',
  TOREADOR_ANTITRIBU: 'Toreador Antitribu',
  TREMERE: 'Tremere',
  TREMERE_ANTITRIBU: 'Tremere Antitribu',
  TRUE_BRUJAH: 'True Brujah',
  TZIMISCE: 'Tzimisce',
  VENTRUE: 'Ventrue',
  VENTRUE_ANTITRIBU: 'Ventrue Antitribu',
  BANU_HAQIM: 'Banu Haqim',
  MINISTRY: 'Ministry',
  AVENGER: 'Avenger',
  DEFENDER: 'Defender',
  INNOCENT: 'Innocent',
  JUDGE: 'Judge',
  MARTYR: 'Martyr',
  REDEEMER: 'Redeemer',
  VISIONARY: 'Visionary',
} as const;

export type ClanCode = keyof typeof CLAN;

/**
 * Resolves either form the app currently sends for a clan into its code:
 * the raw enum constant (GameSnapshotFactory's `.clan(detail.getClan().
 * toString())`, e.g. "TOREADOR_ANTITRIBU"; CardSimple's `clanClasses`,
 * already lowercased/underscored the same way) or the human description
 * (a `Card.clanClasses()` entry / raw clan name from vtescrypt.csv, e.g.
 * "Toreador Antitribu"). Mirrors Clan.java's own from()/of() pair — this is
 * a lookup against that same closed set, not a second source of truth for
 * what a "clan" is. Returns undefined for NONE/blank/unrecognized input.
 */
export function resolveClan(value?: string | null): ClanCode | undefined {
  if (!value) return undefined;
  const key = value.toUpperCase().replace(/[ -]/g, '_');
  if (key in CLAN) return key as ClanCode;
  const entry = (Object.entries(CLAN) as [ClanCode, string][]).find(([, name]) => name.toLowerCase() === value.toLowerCase());
  return entry?.[0];
}

/**
 * Clan glyph — renders through the `.clan.<code>` icon-font classes in
 * card-visuals.css. `role="img"` + `aria-label` give the glyph an actual
 * accessible name (the human clan name) instead of the silence or raw
 * "TOREADOR_ANTITRIBU"-style enum constant a stray `title` used to expose.
 * Renders nothing for an unresolved/NONE value, so callers don't need their
 * own `!== 'NONE'` guard before using it.
 */
export function Clan({ value, className }: { value?: string | null; className?: string }) {
  const code = resolveClan(value);
  if (!code) return null;
  const name = CLAN[code];
  return <span className={`clan ${code.toLowerCase()}${className ? ` ${className}` : ''}`} role="img" aria-label={name} title={name} />;
}
