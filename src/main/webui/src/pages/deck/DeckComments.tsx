import { useState } from 'react';
import { useDebouncedCallback } from '../../hooks/useDebouncedCallback';

/**
 * Debounced deck-notes textarea. Ported from jol-quarkus; Tailwind `jt:`
 * -prefixed. Flushes 1.5s after the last keystroke, and immediately on blur.
 *
 * Seeds from `comments` once — the owning editor pane is remounted (keyed by
 * deck id) on deck switch, so there's no need to re-sync from the prop.
 */
interface Props {
  comments: string;
  onCommentsChange: (comments: string) => void;
}

export function DeckComments({ comments, onCommentsChange }: Props) {
  const [value, setValue] = useState(comments);
  const { call: scheduleCommit, flush } = useDebouncedCallback(onCommentsChange, 1500);

  return (
    <div className="jt:px-3 jt:py-1.5 jt:border-b jt:border-line/50">
      <textarea
        value={value}
        onChange={(e) => {
          setValue(e.target.value);
          scheduleCommit(e.target.value.trim());
        }}
        onBlur={flush}
        placeholder="Add a note…"
        rows={2}
        className="jt:w-full jt:bg-transparent jt:text-xs jt:text-ink-secondary jt:placeholder:text-ink-muted jt:outline-none jt:resize-none jt:leading-relaxed"
      />
    </div>
  );
}
