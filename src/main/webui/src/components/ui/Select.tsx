import type { ReactNode, SelectHTMLAttributes } from 'react';
import { forwardRef } from 'react';
import { ChevronDown } from 'lucide-react';

/**
 * Labelled `<select>` — the `jt:` -prefixed counterpart of `form-select` +
 * `<label class="form-label">`. Same prop shape as `ui/Input`. Renders a real
 * `<label htmlFor={id}>` + `<select id={id}>` so `getByLabelText` /
 * `selectOptions` in tests keep working.
 */
interface SelectProps extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'size'> {
  label?: ReactNode;
  srLabel?: string;
  error?: string;
  hint?: string;
  size?: 'sm' | 'md';
}

const BASE_MD =
  'jt:w-full jt:px-3 jt:py-2 jt:pr-9 jt:rounded jt:bg-surface/70 jt:border jt:border-line jt:text-ink jt:focus:outline-none jt:focus:border-line-accent jt:focus:ring-1 jt:focus:ring-accent/30';
const BASE_SM =
  'jt:w-full jt:px-2.5 jt:py-1.5 jt:pr-8 jt:text-xs jt:rounded jt:border jt:border-line/60 jt:bg-panel/30 jt:text-ink jt:outline-none jt:focus:border-accent/60';

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { label, srLabel, error, hint, size = 'md', className, id, children, ...rest },
  ref,
) {
  const base = size === 'sm' ? BASE_SM : BASE_MD;
  const labelContent = label ?? srLabel;
  const labelClass = label ? 'jt:block jt:text-xs jt:text-ink-muted jt:mb-1' : 'jt:sr-only';

  return (
    <div className="jt:w-full">
      {labelContent && (
        <label htmlFor={id} className={labelClass}>
          {labelContent}
        </label>
      )}
      <div className="jt:relative">
        <select ref={ref} id={id} className={[base, className].filter(Boolean).join(' ')} {...rest}>
          {children}
        </select>
        <ChevronDown className="jt:pointer-events-none jt:absolute jt:right-2.5 jt:top-1/2 jt:-translate-y-1/2 jt:w-4 jt:h-4 jt:text-ink-muted" />
      </div>
      {error && (
        <p className="jt:text-blood jt:text-sm jt:mt-1" role="alert">
          {error}
        </p>
      )}
      {hint && !error && <p className="jt:text-ink-muted jt:text-xs jt:mt-1">{hint}</p>}
    </div>
  );
});
