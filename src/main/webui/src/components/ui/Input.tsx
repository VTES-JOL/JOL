import type { InputHTMLAttributes, ReactNode } from 'react';
import { forwardRef } from 'react';

/** Labelled text input, ported from jol-quarkus. Tailwind `jt:` -prefixed. */
interface InputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'size'> {
  label?: ReactNode;
  srLabel?: string;
  error?: string;
  hint?: string;
  right?: ReactNode;
  size?: 'sm' | 'md';
}

const BASE_MD =
  'jt:w-full jt:px-4 jt:py-2 jt:rounded jt:bg-surface/70 jt:border jt:border-line jt:text-ink jt:placeholder:text-ink-muted jt:focus:outline-none jt:focus:border-line-accent jt:focus:ring-1 jt:focus:ring-accent/30';
const BASE_SM =
  'jt:w-full jt:px-3 jt:py-1.5 jt:text-xs jt:rounded jt:border jt:border-line/60 jt:bg-panel/30 jt:text-ink jt:placeholder:text-ink-muted jt:outline-none jt:focus:border-accent/60';

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { label, srLabel, error, hint, right, size = 'md', className, id, ...rest },
  ref,
) {
  const base = size === 'sm' ? BASE_SM : BASE_MD;
  const inputClass = [base, right ? 'jt:pr-10' : '', className].filter(Boolean).join(' ');
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
        <input ref={ref} id={id} className={inputClass} {...rest} />
        {right && <span className="jt:absolute jt:right-3 jt:top-1/2 jt:-translate-y-1/2">{right}</span>}
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
