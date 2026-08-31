import type { ChangeEvent, ReactNode } from 'react';

/**
 * Toggle switch — the Tailwind-based counterpart of Bootstrap's
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
      className={`flex items-center gap-2.5 text-sm text-ink ${
        disabled ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer'
      } ${className ?? ''}`.trim()}
    >
      <span className="relative inline-flex shrink-0">
        <input
          id={id}
          type="checkbox"
          role="switch"
          checked={checked}
          disabled={disabled}
          onChange={onChange}
          className="peer sr-only"
        />
        <span className="block w-9 h-5 rounded-full bg-hover border border-line transition-colors peer-checked:bg-accent peer-checked:border-accent" />
        <span className="absolute left-0.5 top-0.5 w-4 h-4 rounded-full bg-surface shadow transition-transform peer-checked:translate-x-4" />
      </span>
      {label}
    </label>
  );
}
