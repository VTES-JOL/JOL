import type { ReactNode, SelectHTMLAttributes } from 'react';
import { forwardRef } from 'react';
import { ChevronDown } from 'lucide-react';

/**
 * Labelled `<select>` — the Tailwind-based counterpart of `form-select` +
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
  'w-full px-3 py-2 pr-9 rounded bg-surface/70 border border-line text-ink focus:outline-none focus:border-line-accent focus:ring-1 focus:ring-accent/30';
const BASE_SM =
  'w-full px-2.5 py-1.5 pr-8 text-xs rounded border border-line/60 bg-panel/30 text-ink outline-none focus:border-accent/60';

export const Select = forwardRef<HTMLSelectElement, SelectProps>(function Select(
  { label, srLabel, error, hint, size = 'md', className, id, children, ...rest },
  ref,
) {
  const base = size === 'sm' ? BASE_SM : BASE_MD;
  const labelContent = label ?? srLabel;
  const labelClass = label ? 'block text-xs text-ink-muted mb-1' : 'sr-only';

  return (
    <div className="w-full">
      {labelContent && (
        <label htmlFor={id} className={labelClass}>
          {labelContent}
        </label>
      )}
      <div className="relative">
        <select ref={ref} id={id} className={[base, className].filter(Boolean).join(' ')} {...rest}>
          {children}
        </select>
        <ChevronDown className="pointer-events-none absolute right-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-muted" />
      </div>
      {error && (
        <p className="text-blood text-sm mt-1" role="alert">
          {error}
        </p>
      )}
      {hint && !error && <p className="text-ink-muted text-xs mt-1">{hint}</p>}
    </div>
  );
});
