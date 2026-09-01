import { Fragment, useEffect, useRef } from 'react';
import type { ChatData, CommandError } from '../../api/types';
import { highlightMentions } from '../main/chatFormatting';
import { useCardTooltips } from '../../hooks/useCardTooltips';

// Shared by GameChatPanel (current turn, live) and HistoryPanel (any turn,
// on demand) — mirrors ds.js's renderGameChat(). ChatData is already
// structured JSON (via the already-dedicated GET .../history endpoint), so
// unlike legacy there's no "timestamp||source||message" string to parse.
//
// When showCommands is on (judges only), two extras appear:
//  - the raw command a player submitted, once above each run of lines it
//    produced (line.invocation, sent only to judges);
//  - failed command attempts (`errors`), interleaved by time — mistypes that
//    produced no chat, for misplay investigation.

const MONTHS = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];

// "d-MMM HH:mm" -> a sortable tuple; unknown formats sort last.
function tsKey(ts: string): number {
  const m = /^(\d+)-([A-Za-z]+)\s+(\d+):(\d+)/.exec((ts ?? '').trim());
  if (!m) return Number.MAX_SAFE_INTEGER;
  const mon = MONTHS.indexOf(m[2].slice(0, 3).toLowerCase());
  return ((mon < 0 ? 12 : mon) * 100 + +m[1]) * 10000 + +m[3] * 100 + +m[4];
}

type Row = { kind: 'chat'; data: ChatData; i: number } | { kind: 'error'; data: CommandError };

export function GameChatLog({
  lines,
  viewerName,
  showCommands = false,
  errors = [],
}: {
  lines: ChatData[];
  viewerName: string | null;
  showCommands?: boolean;
  errors?: CommandError[];
}) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [lines]);

  useEffect(() => {
    const el = ref.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [lines, errors]);

  // Merge chat lines and (judge-only) error attempts by time; an error at the
  // same minute as a chat line sorts just after it.
  const rows: Row[] = lines.map((data, i) => ({ kind: 'chat', data, i }) as Row);
  if (showCommands && errors.length) {
    for (const e of errors) rows.push({ kind: 'error', data: e });
    rows.sort((a, b) => {
      const d = tsKey(a.data.timestamp) - tsKey(b.data.timestamp);
      return d !== 0 ? d : (a.kind === 'error' ? 1 : 0) - (b.kind === 'error' ? 1 : 0);
    });
  }

  return (
    <div ref={ref} className="bg-surface text-ink p-1 scrollable">
      {rows.map((row, idx) => {
        if (row.kind === 'error') {
          return (
            <p className="chat-attempt" key={`e${idx}`}>
              <span className="chat-timestamp">{row.data.timestamp}</span>{' '}
              <span className="chat-attempt-icon">⚠</span>{' '}
              <b>{row.data.player}</b> tried <code>{row.data.command}</code>
              {row.data.error && <span className="chat-attempt-error"> — {row.data.error}</span>}
            </p>
          );
        }
        const line = row.data;
        const prev = row.i > 0 ? lines[row.i - 1] : undefined;
        const { html } = highlightMentions(line.message, viewerName);
        const showInvocation =
          showCommands && !!line.invocation && line.invocation !== prev?.invocation;
        return (
          <Fragment key={`c${row.i}`}>
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
