import type { InputHTMLAttributes, ReactNode } from 'react';
import { forwardRef } from 'react';

/** Labelled text input, ported from jol-quarkus. Tailwind Tailwind-based. */
interface InputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'size'> {
  label?: ReactNode;
  srLabel?: string;
  error?: string;
  hint?: string;
  right?: ReactNode;
  size?: 'sm' | 'md';
}

const BASE_MD =
  'w-full px-4 py-2 rounded bg-surface/70 border border-line text-ink placeholder:text-ink-muted focus:outline-none focus:border-line-accent focus:ring-1 focus:ring-accent/30';
const BASE_SM =
  'w-full px-3 py-1.5 text-xs rounded border border-line/60 bg-panel/30 text-ink placeholder:text-ink-muted outline-none focus:border-accent/60';

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, srLabel, error, hint, right, size = 'md', className, id, ...rest },
  ref,
) {
  const base = size === 'sm' ? BASE_SM : BASE_MD;
  const inputClass = [base, right ? 'pr-10' : '', className].filter(Boolean).join(' ');
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
        <input ref={ref} id={id} className={inputClass} {...rest} />
        {right && <span className="absolute right-3 top-1/2 -translate-y-1/2">{right}</span>}
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
