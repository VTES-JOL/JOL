// Presentation helpers for the game chat log (GameChatLog.tsx). Purely
// client-side — nothing here rides on ChatData.

// Log-specific accent palette (NOT the board's seat colours): one stable
// colour per seat, used for the left accent bar and the actor-name tint so a
// run of lines from one player reads as a block. Mid-tone 600-ish hues that
// stay legible on both the light and dark chat surface.
const LOG_ACCENTS = [
  '#ea580c', // orange
  '#0284c7', // sky
  '#16a34a', // green
  '#9333ea', // violet
  '#dc2626', // red
  '#ca8a04', // amber
  '#0d9488', // teal
  '#db2777', // pink
];

/**
 * Accent colour for a chat line's `source`, by its index in `seating`.
 * Returns null for non-seated sources (SYSTEM, "Judge - X") — those keep the
 * default log styling.
 */
export function accentFor(source: string, seating: string[] | undefined): string | null {
  if (!seating || !source) return null;
  const i = seating.indexOf(source);
  return i < 0 ? null : LOG_ACCENTS[i % LOG_ACCENTS.length];
}

// "HH:mm" for the row; the full stamp goes in a title attribute. Prefer the
// ISO postedAt/occurredAt; fall back to slicing the legacy "d-MMM HH:mm".
export function shortTime(iso: string | undefined, legacy: string): string {
  if (iso) {
    const d = new Date(iso);
    if (!Number.isNaN(d.getTime())) {
      return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    }
  }
  const m = /(\d{1,2}:\d{2})/.exec(legacy ?? '');
  return m ? m[1] : (legacy ?? '');
}

// Coarse day key so the log can drop a date separator when it changes.
// ISO -> "Wed 3 Feb"; legacy "3-Feb 14:05" -> "3 Feb"; unknown -> ''.
export function dayLabel(iso: string | undefined, legacy: string): string {
  if (iso) {
    const d = new Date(iso);
    if (!Number.isNaN(d.getTime())) {
      return d.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric', month: 'short' });
    }
  }
  const m = /^(\d{1,2})-([A-Za-z]{3})/.exec((legacy ?? '').trim());
  return m ? `${m[1]} ${m[2]}` : '';
}
