import { useState } from 'react';
import { MessageSquarePlus, SendHorizonal } from 'lucide-react';
import { QuickChatModal } from './QuickChatModal';

// The "Say something to the table…" box at the foot of the chat window
// (Main.dc.html talk rail / Mobile.dc.html sheet). Table talk belongs with the
// log it lands in, not buried in the Commands panel — so this rides whichever
// surface the chat log is on. Rendered by GamePage inside `chatPanel`, only
// when the viewer may chat (seated player or judge).
export function ChatCompose({ onSend, disabled }: { onSend: (text: string) => void; disabled?: boolean }) {
  const [text, setText] = useState('');
  const [showQuick, setShowQuick] = useState(false);

  const send = () => {
    const trimmed = text.trim();
    if (!trimmed || disabled) return;
    onSend(trimmed);
    setText('');
  };

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        send();
      }}
      className="flex shrink-0 items-center gap-1.5 border-t border-line-accent bg-panel/60 px-2 py-1.5"
      autoComplete="off"
    >
      <button
        type="button"
        aria-label="Quick chat shortcuts"
        title="Canned combat / vote call-outs"
        disabled={disabled}
        onClick={() => setShowQuick(true)}
        className="inline-flex min-h-11 min-w-11 md:min-h-0 md:min-w-0 shrink-0 items-center justify-center rounded border border-line-accent p-1.5 text-ink-muted hover:text-ink disabled:opacity-40"
      >
        <MessageSquarePlus size={15} />
      </button>
      <input
        type="text"
        value={text}
        disabled={disabled}
        onChange={(e) => setText(e.target.value)}
        placeholder="Say something to the table…"
        aria-label="Chat"
        className="min-w-0 flex-1 rounded border border-line bg-surface/70 px-2 py-1.5 text-sm text-ink outline-none focus:border-accent/60 disabled:opacity-40"
      />
      <button
        type="submit"
        disabled={disabled || !text.trim()}
        className="inline-flex min-h-11 min-w-11 md:min-h-0 md:min-w-0 shrink-0 items-center justify-center rounded bg-accent px-2 py-1.5 text-surface hover:bg-accent-dim disabled:opacity-40"
        aria-label="Send"
      >
        <SendHorizonal size={15} />
      </button>
      {showQuick && (
        <QuickChatModal
          onSend={(m) => {
            onSend(m);
            setShowQuick(false);
          }}
          onClose={() => setShowQuick(false)}
        />
      )}
    </form>
  );
}
