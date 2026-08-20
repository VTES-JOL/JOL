import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { api } from '../../api/client';
import type { ChatEntry } from '../../api/types';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { useJolSocket } from '../../ws/useJolSocket';
import { useAuth } from '../../nav/useAuth';
import { useCardTooltips } from '../../hooks/useCardTooltips';
import { dayLabel, highlightMentions, localTimeTitle, utcTime } from './chatFormatting';

const AT_BOTTOM_THRESHOLD_PX = 20;

interface RenderedEntry {
  entry: ChatEntry;
  day: string;
  showDayBreak: boolean;
  showPlayerLabel: boolean;
  html: string;
  isMention: boolean;
}

function buildRenderedEntries(entries: ChatEntry[], player: string | null): RenderedEntry[] {
  let lastDay: string | null = null;
  let lastPlayer: string | null = null;
  return entries.map((entry) => {
    const day = dayLabel(entry.timestamp);
    const showDayBreak = day !== lastDay;
    const showPlayerLabel = showDayBreak || entry.player !== lastPlayer;
    lastDay = day;
    lastPlayer = entry.player;
    const { html, isMention } = highlightMentions(entry.message, player);
    return { entry, day, showDayBreak, showPlayerLabel, html, isMention };
  });
}

export function GlobalChat() {
  const { player } = useAuth();
  const [entries, setEntries] = useState<ChatEntry[]>([]);
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const [hasNewMessages, setHasNewMessages] = useState(false);

  const outputRef = useRef<HTMLDivElement>(null);
  // Updated on every scroll event rather than computed inside the entries
  // effect, since by the time that effect runs the DOM (and scrollHeight)
  // already reflects the newly-appended content — too late to tell whether
  // the user *was* at the bottom before it arrived.
  const isAtBottomRef = useRef(true);
  // Forces a scroll-to-bottom regardless of prior position: true on the
  // initial history load and right after this player sends their own
  // message, mirroring ds.js's `scrollChat` flag.
  const forceScrollRef = useRef(true);

  const rendered = useMemo(() => buildRenderedEntries(entries, player), [entries, player]);
  useCardTooltips(outputRef, [entries]);

  const appendChat = (delta: ChatEntry[]) => {
    if (delta.length > 0) setEntries((log) => [...log, ...delta]);
  };

  const refresh = () => {
    api
      .get<ChatEntry[]>('/main/chat')
      .then(appendChat)
      .catch((err) => console.error('Failed to load /main/chat', err));
  };

  useEffect(() => {
    // First load only: /main/chat/history ignores this player's read cursor
    // entirely and returns recent history unconditionally (marking it seen
    // server-side) — using the delta endpoint here would show nothing
    // whenever the cursor already happens to be caught up.
    api
      .get<ChatEntry[]>('/main/chat/history')
      .then(setEntries)
      .catch((err) => console.error('Failed to load /main/chat/history', err));
  }, []);
  useJolSocket('main:chat', refresh);

  const scrollToBottom = () => {
    const el = outputRef.current;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
    isAtBottomRef.current = true;
    setHasNewMessages(false);
  };

  const handleScroll = () => {
    const el = outputRef.current;
    if (!el) return;
    const distance = el.scrollHeight - el.scrollTop - el.clientHeight;
    isAtBottomRef.current = distance < AT_BOTTOM_THRESHOLD_PX;
    if (isAtBottomRef.current) setHasNewMessages(false);
  };

  useLayoutEffect(() => {
    if (entries.length === 0) return;
    if (forceScrollRef.current || isAtBottomRef.current) {
      scrollToBottom();
    } else {
      setHasNewMessages(true);
    }
    forceScrollRef.current = false;
  }, [entries]);

  const send = () => {
    const message = text.trim();
    if (!message || sending) return;
    setSending(true);
    // The POST response already carries this message in its own delta — no
    // need for a second round trip to see what was just sent.
    api
      .post<ChatEntry[]>('/chat', { text: message })
      .then((delta) => {
        setText('');
        forceScrollRef.current = true;
        appendChat(delta);
      })
      .catch((err) => console.error('Failed to send chat message', err))
      .finally(() => setSending(false));
  };

  return (
    <Card className="flex-fill d-flex flex-column" style={{ minHeight: 0 }}>
      <CardHeader>
        <CardTitle>Global Chat</CardTitle>
      </CardHeader>
      <div className="card-body position-relative flex-fill d-flex flex-column p-2" style={{ minHeight: 0 }}>
        {/*
          overflowY/minHeight inline, not left to the matching #globalChatOutput
          rule in the site's own styles.css: that stylesheet is injected via a
          JS-appended <link> tag (legacyStyles.ts) which doesn't block
          rendering, so relying on it alone for the one property that makes
          this element actually scroll (instead of growing and dragging every
          ancestor's height along with it) is a real race, not just belt-and-braces.
        */}
        <div
          id="globalChatOutput"
          ref={outputRef}
          className="flex-fill"
          style={{ minHeight: 0, overflowY: 'auto' }}
          onScroll={handleScroll}
        >
          {rendered.map(({ entry, day, showDayBreak, showPlayerLabel, html, isMention }, i) => (
            <div key={i}>
              {showDayBreak && (
                <div className="chat-day-break">
                  <span className="chat-day-label">{day}</span>
                </div>
              )}
              <p className={`chat${isMention ? ' bg-warning-subtle rounded px-1' : ''}`}>
                <span className="chat-timestamp" title={localTimeTitle(entry.timestamp)}>
                  {utcTime(entry.timestamp)}
                </span>
                <span
                  dangerouslySetInnerHTML={{
                    __html: ` ${showPlayerLabel ? `<b>${entry.player}</b> ` : ''}${html}`,
                  }}
                />
              </p>
            </div>
          ))}
        </div>
        <div
          className={`text-center p-2 text-bg-success rounded hover-success position-absolute d-${hasNewMessages ? 'flex' : 'none'} justify-content-between align-items-center`}
          style={{ bottom: '3.3rem', left: '1rem', width: 'calc(100% - 2rem)', cursor: 'pointer' }}
          onClick={scrollToBottom}
        >
          <i className="bi bi-chevron-double-down"></i>
          <span>New Messages</span>
          <i className="bi bi-chevron-double-down"></i>
        </div>
        <div className="d-flex gap-2 mt-2">
          <input
            className="form-control rounded-pill border border-secondary-subtle"
            placeholder="Chat with players..."
            value={text}
            disabled={sending}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && send()}
          />
        </div>
      </div>
    </Card>
  );
}
