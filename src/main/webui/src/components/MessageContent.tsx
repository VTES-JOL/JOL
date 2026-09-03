import { Fragment, type ReactNode } from 'react';
import { parseMessageTokens } from '../utils/parseMessageTokens';
import { splitMentions } from '../utils/mentions';
import { CardToken } from './CardToken';

// Renders a chat / game-log message: plain text with server-substituted tokens
// (see utils/parseMessageTokens.ts) turned into the right inline elements.
// Replaces the old dangerouslySetInnerHTML path — the server no longer emits
// HTML for chat. Card segments render as a.card-name links, which the log
// container's useCardTooltips hook then decorates with hover previews.
export function MessageContent({
  message,
  viewer,
}: {
  message: string;
  viewer: string | null;
}) {
  return <>{parseMessageTokens(message).map((seg, i) => renderSegment(seg, i, viewer))}</>;
}

function renderSegment(
  seg: ReturnType<typeof parseMessageTokens>[number],
  key: number,
  viewer: string | null,
): ReactNode {
  switch (seg.type) {
    case 'card':
      return <CardToken key={key} id={seg.id} name={seg.name} advanced={seg.advanced} />;
    case 'disc':
      return <span key={key} className={`icon ${seg.code}`} />;
    case 'daction':
      return <span key={key} className="icon D" />;
    case 'style':
      return (
        <span key={key} className="game-name">
          {seg.content}
        </span>
      );
    case 'text':
      return (
        <Fragment key={key}>
          {splitMentions(seg.content, viewer).map((part, j) =>
            'mention' in part ? (
              <span key={j} className={`chat-mention${part.self ? ' chat-mention-self' : ''}`}>
                @{part.mention}
              </span>
            ) : (
              <Fragment key={j}>{part.text}</Fragment>
            ),
          )}
        </Fragment>
      );
  }
}
