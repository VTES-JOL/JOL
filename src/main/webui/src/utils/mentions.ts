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

export type MentionPart = { text: string } | { mention: string };

/** Split a run of text on any mention of `player` (or "@All"). */
export function splitMentions(text: string, player: string | null): MentionPart[] {
  if (!player) return [{ text }];
  const parts: MentionPart[] = [];
  const re = mentionPattern(player);
  let last = 0;
  for (const m of text.matchAll(re)) {
    const start = m.index ?? 0;
    if (start > last) parts.push({ text: text.slice(last, start) });
    parts.push({ mention: m[1] });
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
