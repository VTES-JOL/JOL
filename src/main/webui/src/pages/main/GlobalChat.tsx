import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { ChevronsDown } from 'lucide-react';
import { api } from '../../api/client';
import type { ChatEntry } from '../../api/types';
import { Card, CardHeader, CardTitle } from '../../components/ui/Card';
import { subscribe } from '../../stores/socket';
import { useAuth } from '../../auth/useAuth';
import { useCardTooltips } from '../../hooks/useCardTooltips';
import { dayLabel, highlightMentions, localTimeTitle, utcTime } from './chatFormatting';
import { runRequest } from '../../api/mutate';
import './GlobalChat.css';

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
  const queryClient = useQueryClient();
  const [entries, setEntries] = useState<ChatEntry[]>([]);
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const [hasNewMessages, setHasNewMessages] = useState(false);

  const outputRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
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

  // Tracks the last-seen entry's timestamp for the delta fetch below — a
  // ref (not state) since it's only ever read from event handlers, never
  // during render.
  const cursorRef = useRef<string | null>(null);

  const appendChat = (delta: ChatEntry[]) => {
    if (delta.length === 0) return;
    setEntries((log) => [...log, ...delta]);
    cursorRef.current = delta[delta.length - 1].timestamp;
    // Keep the ['main-chat-history'] cache in sync with what's actually been
    // seen — its staleTime: Infinity means a remounted GlobalChat (e.g. after
    // navigating away and back) reads straight from this cache with no
    // refetch, so without this it would reset `entries` back to whatever was
    // cached at the very first load, silently dropping every message
    // appended since (including any card-link messages, which is why hover
    // on them "stopped working" — the message itself was gone).
    queryClient.setQueryData<ChatEntry[]>(['main-chat-history'], (old) => [...(old ?? []), ...delta]);
  };

  // First load only: /main/chat/history ignores this player's read cursor
  // entirely and returns recent history unconditionally (marking it seen
  // server-side) — using the delta endpoint here would show nothing
  // whenever the cursor already happens to be caught up. staleTime:
  // Infinity since this is a one-shot load, never meant to be refetched.
  //
  // Deliberately NOT keyed ['main-chat', 'history'] — the generic
  // useQueryInvalidation.ts bridge does prefix matching, so any query
  // whose key starts with ['main-chat'] gets force-refetched (bypassing
  // staleTime) on every chat push, which would replace `entries` wholesale
  // via the effect below at the same time the WS handler further down
  // appends the same message again. A key with no shared prefix keeps
  // this query outside the invalidation system entirely, as intended.
  const { data: history } = useQuery({
    queryKey: ['main-chat-history'],
    queryFn: () => api.get<ChatEntry[]>('/main/chat/history'),
    staleTime: Infinity,
  });
  useEffect(() => {
    if (!history) return;
    setEntries(history);
    if (history.length > 0) cursorRef.current = history[history.length - 1].timestamp;
  }, [history]);

  useEffect(
    () =>
      // Deliberately NOT routed through ws/useQueryInvalidation.ts's generic
      // invalidateQueries bridge: chat is an ever-growing, append-only log,
      // not a "refetch replaces the cache" resource — a blind invalidate
      // would need a plain useQuery backing the full log, and GET /main/chat
      // is a stateful delta-since-cursor endpoint that can't be one (see
      // MainResource.chat()'s doc comment). Subscribe directly to the same
      // underlying WS envelope instead, and merge the delta by hand.
      subscribe('invalidate', (msg) => {
        const key = msg.key;
        if (!Array.isArray(key) || key.length !== 1 || key[0] !== 'main-chat') return;
        const since = cursorRef.current;
        api
          .get<ChatEntry[]>(`/main/chat${since ? `?since=${encodeURIComponent(since)}` : ''}`)
          .then(appendChat)
          .catch((err) => console.error('Failed to load /main/chat', err));
      }),
    [],
  );

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
    runRequest(api.post<ChatEntry[]>('/chat', { text: message }), 'Failed to send chat message', (delta) => {
      setText('');
      forceScrollRef.current = true;
      appendChat(delta);
    }).finally(() => setSending(false));
  };

  // Disabling the input while sending blurs it (a disabled element can't
  // hold focus). Restore focus once React re-renders it enabled again —
  // calling .focus() straight from send()'s finally() is too early, since
  // the DOM node is still disabled until this effect's render commits.
  // Skips the initial mount so this doesn't steal focus on page load.
  const wasSendingRef = useRef(false);
  useEffect(() => {
    if (wasSendingRef.current && !sending) inputRef.current?.focus();
    wasSendingRef.current = sending;
  }, [sending]);

  return (
    <Card className="flex flex-col flex-1 min-h-0 overflow-hidden">
      <CardHeader>
        <CardTitle>Global Chat</CardTitle>
      </CardHeader>
      <div className="relative flex flex-col flex-1 min-h-0 p-2">
        {/*
          overflowY/minHeight inline as well as in the co-located
          #globalChatOutput rule (GlobalChat.css) — belt-and-braces so the
          element still scrolls correctly (instead of growing and dragging
          every ancestor's height along with it) even before the stylesheet
          paints, matching every other scroll container in the app that sets
          this inline.
        */}
        <div
          id="globalChatOutput"
          ref={outputRef}
          className="flex-1"
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
              <p className={`chat${isMention ? ' bg-arcane/10 rounded px-1' : ''}`}>
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
          className={`hover-success absolute rounded bg-online text-surface p-2 justify-between items-center cursor-pointer ${
            hasNewMessages ? 'flex' : 'hidden'
          }`}
          style={{ bottom: '3.3rem', left: '1rem', width: 'calc(100% - 2rem)' }}
          onClick={scrollToBottom}
        >
          <ChevronsDown size={16} />
          <span>New Messages</span>
          <ChevronsDown size={16} />
        </div>
        <div className="flex gap-2 mt-2">
          <input
            ref={inputRef}
            className="w-full rounded-full border border-line-accent bg-surface/70 px-4 py-1.5 text-sm text-ink placeholder:text-ink-muted outline-none focus:border-accent/60"
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
