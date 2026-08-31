import type { ReactNode, TextareaHTMLAttributes } from 'react';
import { forwardRef } from 'react';

/**
 * Labelled `<textarea>` — the `jt:` -prefixed counterpart of a `form-control`
 * textarea. Same prop shape as `ui/Input`.
 */
interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: ReactNode;
  srLabel?: string;
  error?: string;
  hint?: string;
}

const BASE =
  'jt:w-full jt:px-3 jt:py-2 jt:rounded jt:bg-surface/70 jt:border jt:border-line jt:text-ink jt:placeholder:text-ink-muted jt:outline-none jt:focus:border-line-accent jt:focus:ring-1 jt:focus:ring-accent/30 jt:resize-y';

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(
  { label, srLabel, error, hint, className, id, ...rest },
  ref,
) {
  const labelContent = label ?? srLabel;
  const labelClass = label ? 'jt:block jt:text-xs jt:text-ink-muted jt:mb-1' : 'jt:sr-only';

  return (
    <div className="jt:w-full">
      {labelContent && (
        <label htmlFor={id} className={labelClass}>
          {labelContent}
        </label>
      )}
      <textarea ref={ref} id={id} className={[BASE, className].filter(Boolean).join(' ')} {...rest} />
      {error && (
        <p className="jt:text-blood jt:text-sm jt:mt-1" role="alert">
          {error}
        </p>
      )}
      {hint && !error && <p className="jt:text-ink-muted jt:text-xs jt:mt-1">{hint}</p>}
    </div>
  );
});
