import { useRef } from 'react';
import type { ChatData } from '../../api/types';
import { highlightMentions } from '../main/chatFormatting';
import { useCardTooltips } from '../../hooks/useCardTooltips';

// Shared by GameChatPanel (current turn, live) and HistoryPanel (any turn,
// on demand) — mirrors ds.js's renderGameChat(). ChatData is already
// structured JSON (via the already-dedicated GET .../history endpoint), so
// unlike legacy there's no "timestamp||source||message" string to parse.
export function GameChatLog({ lines, viewerName }: { lines: ChatData[]; viewerName: string | null }) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [lines]);

  return (
    <div ref={ref} className="bg-white p-1 scrollable">
      {lines.map((line, i) => {
        const { html } = highlightMentions(line.message, viewerName);
        return (
          <p className="chat" key={i}>
            <span className="chat-timestamp">{line.timestamp}</span>{' '}
            {line.source && line.source !== 'null' && <b>{line.source}</b>}{' '}
            <span dangerouslySetInnerHTML={{ __html: html }} />
          </p>
        );
      })}
    </div>
  );
}
