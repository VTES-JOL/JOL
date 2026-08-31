import type { ChangeEvent, ReactNode } from 'react';

/**
 * Toggle switch — the `jt:` -prefixed counterpart of Bootstrap's
 * `.form-check.form-switch`. A visually-hidden `role="switch"` checkbox drives
 * a pill track so keyboard/AT behaviour is native.
 */
interface SwitchProps {
  id: string;
  label: ReactNode;
  checked: boolean;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  disabled?: boolean;
  title?: string;
  className?: string;
}

export function Switch({ id, label, checked, onChange, disabled, title, className }: SwitchProps) {
  return (
    <label
      htmlFor={id}
      title={title}
      className={`jt:flex jt:items-center jt:gap-2.5 jt:text-sm jt:text-ink ${
        disabled ? 'jt:opacity-60 jt:cursor-not-allowed' : 'jt:cursor-pointer'
      } ${className ?? ''}`.trim()}
    >
      <span className="jt:relative jt:inline-flex jt:shrink-0">
        <input
          id={id}
          type="checkbox"
          role="switch"
          checked={checked}
          disabled={disabled}
          onChange={onChange}
          className="jt:peer jt:sr-only"
        />
        <span className="jt:block jt:w-9 jt:h-5 jt:rounded-full jt:bg-hover jt:border jt:border-line jt:transition-colors jt:peer-checked:bg-accent jt:peer-checked:border-accent" />
        <span className="jt:absolute jt:left-0.5 jt:top-0.5 jt:w-4 jt:h-4 jt:rounded-full jt:bg-surface jt:shadow jt:transition-transform jt:peer-checked:translate-x-4" />
      </span>
      {label}
    </label>
  );
}
