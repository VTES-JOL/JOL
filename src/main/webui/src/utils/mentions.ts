// "@name" / "@All" mention detection for chat text.
//
// Messages are plain text now (ParserService decodes the HTML entities it used
// to add), so this matches a literal "@" — the old code matched "&#64;" because
// the message was an HTML fragment at that point.

function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function mentionPattern(player: string): RegExp {
  return new RegExp(`@(${escapeRegExp(player)}|All)\\b`, 'g');
}

// Any "@handle" token. Handles here are player names — letters, digits and the
// punctuation JOL allows in a username. Kept deliberately loose; a false
// positive just renders one word in the mention colour.
const ANY_MENTION_RE = /@([A-Za-z0-9][\w.-]*)/g;

export type MentionPart = { text: string } | { mention: string; self: boolean };

/**
 * Split a run of text on every "@name" mention. `self` is true when the
 * mention targets `player` (or is "@All") — the caller styles those louder.
 */
export function splitMentions(text: string, player: string | null): MentionPart[] {
  const parts: MentionPart[] = [];
  let last = 0;
  for (const m of text.matchAll(ANY_MENTION_RE)) {
    const start = m.index ?? 0;
    if (start > last) parts.push({ text: text.slice(last, start) });
    const name = m[1];
    const self = !!player && (name === player || name === 'All');
    parts.push({ mention: name, self });
    last = start + m[0].length;
  }
  if (last < text.length) parts.push({ text: text.slice(last) });
  return parts.length ? parts : [{ text }];
}

/** Whether `message` mentions `player` (or "@All") anywhere — for the row highlight. */
export function messageHasMention(message: string, player: string | null): boolean {
  if (!player) return false;
  return mentionPattern(player).test(message);
}
