import { Fragment, useEffect, useRef, type CSSProperties } from 'react';
import type { ChatData, CommandError } from '../../api/types';
import { MessageContent } from '../../components/MessageContent';
import { useCardTooltips } from '../../hooks/useCardTooltips';
import { accentFor, dayLabel, shortTime } from './chatLogStyle';

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

// Fallback for rows predating postedAt/occurredAt: "d-MMM HH:mm" -> epoch ms in
// the current year (year isn't in the string; a turn spanning New Year is not
// worth handling here). Unknown formats sort last.
function fallbackKey(ts: string): number {
  const m = /^(\d+)-([A-Za-z]+)\s+(\d+):(\d+)/.exec((ts ?? '').trim());
  if (!m) return Number.MAX_SAFE_INTEGER;
  const mon = MONTHS.indexOf(m[2].slice(0, 3).toLowerCase());
  if (mon < 0) return Number.MAX_SAFE_INTEGER;
  return new Date(new Date().getFullYear(), mon, +m[1], +m[3], +m[4]).getTime();
}

// Chronological sort key. Prefer the full-precision ISO stamp
// (ChatData.postedAt / CommandError.occurredAt) so a mistyped attempt lands
// exactly where it happened relative to the chat lines around it; fall back to
// the minute-granularity display string for older rows that carry neither.
function rowKey(row: Row): number {
  const iso = row.kind === 'chat' ? row.data.postedAt : row.data.occurredAt;
  if (iso) {
    const t = Date.parse(iso);
    if (!Number.isNaN(t)) return t;
  }
  return fallbackKey(row.data.timestamp);
}

type Row = { kind: 'chat'; data: ChatData; i: number } | { kind: 'error'; data: CommandError };

// Shared by chat lines and the (judge-only) mistyped-attempt rows so both
// format and hover identically (U6): "HH:mm" shown, full stamp on hover.
function LogTimestamp({ iso, legacy }: { iso?: string; legacy: string }) {
  return (
    <span className="chat-timestamp" title={iso || legacy}>
      {shortTime(iso, legacy)}
    </span>
  );
}

export function GameChatLog({
  lines,
  viewerName,
  showCommands = false,
  errors = [],
  seating,
}: {
  lines: ChatData[];
  viewerName: string | null;
  showCommands?: boolean;
  errors?: CommandError[];
  /** Player names in seating order — drives the per-actor accent colour. */
  seating?: string[];
}) {
  const ref = useRef<HTMLDivElement>(null);
  useCardTooltips(ref, [lines]);

  useEffect(() => {
    const el = ref.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [lines, errors]);

  // Merge chat lines and (judge-only) error attempts into one time-ordered
  // stream. rowKey uses full-precision timestamps where present; sort() is
  // stable, so a genuine key tie (only possible when both sides fell back to
  // the same minute) keeps chat-before-error insertion order.
  const rows: Row[] = lines.map((data, i) => ({ kind: 'chat', data, i }) as Row);
  if (showCommands && errors.length) {
    for (const e of errors) rows.push({ kind: 'error', data: e });
    rows.sort((a, b) => rowKey(a) - rowKey(b));
  }

  let lastDay = '';
  let lastActor: string | null = null;

  return (
    <div ref={ref} className="bg-surface text-ink p-1 scrollable">
      {rows.map((row, idx) => {
        const iso = row.kind === 'chat' ? row.data.postedAt : row.data.occurredAt;
        const day = dayLabel(iso, row.data.timestamp);
        const newDay = day && day !== lastDay;
        const dateSep = newDay ? (
          <p className="chat-date-sep" key={`d${idx}`}>
            {day}
          </p>
        ) : null;
        if (day) lastDay = day;
        // A date separator breaks the visual run — the line under it starts a
        // fresh group (full-strength name, no "repeat" dimming).
        if (newDay) lastActor = null;

        if (row.kind === 'error') {
          lastActor = null;
          return (
            <Fragment key={`e${idx}`}>
              {dateSep}
              <p className="chat-attempt">
                <LogTimestamp iso={row.data.occurredAt} legacy={row.data.timestamp} />{' '}
                <span className="chat-attempt-icon">⚠</span>{' '}
                <b>{row.data.player}</b> tried <code>{row.data.command}</code>
                {row.data.error && <span className="chat-attempt-error"> — {row.data.error}</span>}
              </p>
            </Fragment>
          );
        }

        const line = row.data;
        // Machine-generated lines (phase markers, turn markers, moves, judge
        // rulings) get the same quiet channel as global chat's SYSTEM lines —
        // see .chat-system in GamePage.css / GlobalChat.css.
        const isSystem = line.source === 'SYSTEM';
        const prev = row.i > 0 ? lines[row.i - 1] : undefined;
        // One header per command *submission*, not per distinct command text:
        // dedup on invocationSeq (shared across the lines one submission emits,
        // distinct for the next) so five identical `transfer ready 1 -1`s each
        // get their own header. Pre-V20 rows have no seq — fall back to the
        // text comparison, which still collapses a multi-line single command.
        const showInvocation =
          showCommands &&
          !!line.invocation &&
          (line.invocationSeq != null
            ? line.invocationSeq !== prev?.invocationSeq
            : line.invocation !== prev?.invocation);

        const accent = isSystem ? null : accentFor(line.source, seating);
        // Group a run of adjacent lines from the same actor: the invocation
        // header and any SYSTEM line break the run. The repeated name is dimmed
        // (kept in the DOM for copy-paste / screen readers), not removed.
        const sameRun = !isSystem && !showInvocation && line.source === lastActor;
        lastActor = isSystem ? null : line.source;

        const style = accent
          ? ({ '--log-accent': accent } as CSSProperties)
          : undefined;
        const className = [
          'chat',
          isSystem ? 'chat-system' : '',
          accent ? 'chat-accent' : '',
          sameRun ? 'chat-run' : '',
        ]
          .filter(Boolean)
          .join(' ');

        return (
          <Fragment key={`c${row.i}`}>
            {dateSep}
            {showInvocation && (
              <p className="chat-command">
                <span className="chat-command-marker">&raquo;</span>{' '}
                {line.invocationBy && <span className="chat-command-by">{line.invocationBy}</span>}{' '}
                <code>{line.invocation}</code>
              </p>
            )}
            <p className={className} style={style}>
              <LogTimestamp iso={line.postedAt} legacy={line.timestamp} />{' '}
              {!isSystem && line.source && line.source !== 'null' && (
                <b className={sameRun ? 'chat-name chat-name-repeat' : 'chat-name'}>{line.source}</b>
              )}{' '}
              <span>
                <MessageContent message={line.message} viewer={viewerName} />
              </span>
            </p>
          </Fragment>
        );
      })}
    </div>
  );
}
