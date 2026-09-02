// Chat / game-log messages arrive as plain text with tokens the server
// substituted for the markup a player typed (see
// net.deckserver.services.ParserService):
//
//   [card:<id>:<name>]        a card reference       -> <CardToken>
//   [card:<id>:<name>:adv]    advanced crypt card    -> <CardToken advanced>
//   [disc:<code>]             a discipline icon      -> <span class="icon <code>">
//   [d]                       the (D) directed-action icon
//   [style:<text>]            emphasised "game name" text
//
// parseMessageTokens splits a message into an ordered list of segments so
// MessageContent can render each one as a React node. Anything not matching a
// token (including an unresolved "[Some Name]") stays a plain text segment.

export type MessageSegment =
  | { type: 'text'; content: string }
  | { type: 'card'; id: string; name: string; advanced: boolean }
  | { type: 'disc'; code: string }
  | { type: 'daction' }
  | { type: 'style'; content: string };

// One alternation; whichever branch matched decides the segment type.
//  1,2,3 -> card id, name, optional ":adv"
//  4     -> discipline code
//  5     -> style text
// The card name is non-greedy with an optional trailing ":adv" so a name that
// itself contains a colon still parses (the engine backtracks to the last "]").
const TOKEN_RE =
  /\[card:(\d+):(.+?)(:adv)?\]|\[disc:([A-Za-z]+)\]|\[d\]|\[style:([^\]]*)\]/g;

export function parseMessageTokens(content: string): MessageSegment[] {
  const segments: MessageSegment[] = [];
  let last = 0;

  for (const match of content.matchAll(TOKEN_RE)) {
    const start = match.index ?? 0;
    if (start > last) {
      segments.push({ type: 'text', content: content.slice(last, start) });
    }

    const [full, cardId, cardName, adv, discCode, styleText] = match;
    if (cardId !== undefined) {
      segments.push({ type: 'card', id: cardId, name: cardName, advanced: adv !== undefined });
    } else if (discCode !== undefined) {
      segments.push({ type: 'disc', code: discCode });
    } else if (styleText !== undefined) {
      segments.push({ type: 'style', content: styleText });
    } else {
      segments.push({ type: 'daction' });
    }

    last = start + full.length;
  }

  if (last < content.length) {
    segments.push({ type: 'text', content: content.slice(last) });
  }

  return segments;
}
