import { Fragment, useEffect, useRef } from 'react';
import type { ChatData } from '../../api/types';
import { highlightMentions } from '../main/chatFormatting';
import { useCardTooltips } from '../../hooks/useCardTooltips';

// Shared by GameChatPanel (current turn, live) and HistoryPanel (any turn,
// on demand) — mirrors ds.js's renderGameChat(). ChatData is already
// structured JSON (via the already-dedicated GET .../history endpoint), so
// unlike legacy there's no "timestamp||source||message" string to parse.
//
// When showCommands is on (judges only — the server only sends line.invocation
// to a judge watching a game they are not seated in), the raw command a player
// submitted is shown once above each run of consecutive lines it produced.
export function GameChatLog({
  lines,
  viewerName,
  showCommands = false,
}: {
  lines: ChatData[];
  viewerName: string | null;
  showCommands?: boolean;
}) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [lines]);

  useEffect(() => {
    const el = ref.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [lines]);

  return (
    <div ref={ref} className="bg-surface text-ink p-1 scrollable">
      {lines.map((line, i) => {
        const { html } = highlightMentions(line.message, viewerName);
        const showInvocation =
          showCommands && !!line.invocation && line.invocation !== lines[i - 1]?.invocation;
        return (
          <Fragment key={i}>
            {showInvocation && (
              <p className="chat-command">
                <span className="chat-command-marker">&raquo;</span>{' '}
                {line.invocationBy && <span className="chat-command-by">{line.invocationBy}</span>}{' '}
                <code>{line.invocation}</code>
              </p>
            )}
            <p className="chat">
              <span className="chat-timestamp">{line.timestamp}</span>{' '}
              {line.source && line.source !== 'null' && <b>{line.source}</b>}{' '}
              <span dangerouslySetInnerHTML={{ __html: html }} />
            </p>
          </Fragment>
        );
      })}
    </div>
  );
}
