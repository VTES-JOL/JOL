import { useCallback, useEffect, useRef, useState } from 'react';

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
  const timerRef = useRef<ReturnType<typeof setTimeout>>(undefined);
  const changeRef = useRef(onCommentsChange);
  useEffect(() => {
    changeRef.current = onCommentsChange;
  });

  const handleChange = useCallback((next: string) => {
    setValue(next);
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => changeRef.current(next.trim()), 1500);
  }, []);

  const flush = useCallback(() => {
    clearTimeout(timerRef.current);
    const trimmed = value.trim();
    if (trimmed !== comments.trim()) changeRef.current(trimmed);
  }, [value, comments]);

  return (
    <div className="jt:px-3 jt:py-1.5 jt:border-b jt:border-line/50">
      <textarea
        value={value}
        onChange={(e) => handleChange(e.target.value)}
        onBlur={flush}
        placeholder="Add a note…"
        rows={2}
        className="jt:w-full jt:bg-transparent jt:text-xs jt:text-ink-secondary jt:placeholder:text-ink-muted jt:outline-none jt:resize-none jt:leading-relaxed"
      />
    </div>
  );
}
