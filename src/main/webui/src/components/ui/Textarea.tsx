import type { ReactNode, TextareaHTMLAttributes } from 'react';
import { forwardRef } from 'react';

/**
 * Labelled `<textarea>` — the Tailwind-based counterpart of a `form-control`
 * textarea. Same prop shape as `ui/Input`.
 */
interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: ReactNode;
  srLabel?: string;
  error?: string;
  hint?: string;
}

const BASE =
  'w-full px-3 py-2 rounded bg-surface/70 border border-line text-ink placeholder:text-ink-muted outline-none focus:border-line-accent focus:ring-1 focus:ring-accent/30 resize-y';

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(function Textarea(
  { label, srLabel, error, hint, className, id, ...rest },
  ref,
) {
  const labelContent = label ?? srLabel;
  const labelClass = label ? 'block text-xs text-ink-muted mb-1' : 'sr-only';

  return (
    <div className="w-full">
      {labelContent && (
        <label htmlFor={id} className={labelClass}>
          {labelContent}
        </label>
      )}
      <textarea ref={ref} id={id} className={[BASE, className].filter(Boolean).join(' ')} {...rest} />
      {error && (
        <p className="text-blood text-sm mt-1" role="alert">
          {error}
        </p>
      )}
      {hint && !error && <p className="text-ink-muted text-xs mt-1">{hint}</p>}
    </div>
  );
});
