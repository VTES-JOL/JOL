import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { Loader2 } from 'lucide-react';

/**
 * Control button, ported from the jol-quarkus rewrite. Tailwind-based (all
 * classes Tailwind-based — see styles/tailwind.css); does NOT use the
 * legacy Bootstrap `.btn` classes. Use on pages already migrated off
 * Bootstrap.
 */
type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'accent-ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  icon?: ReactNode;
}

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: 'bg-accent text-surface hover:bg-accent-dim',
  secondary: 'border border-line-accent text-ink-secondary hover:text-ink hover:bg-hover',
  ghost: 'text-ink-secondary hover:text-ink hover:bg-hover',
  'accent-ghost': 'text-accent hover:text-accent-dim hover:bg-accent/10',
  danger: 'border border-blood/40 text-blood hover:bg-blood/10',
};

const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: 'text-xs px-2 py-1 rounded',
  md: 'text-sm px-3 py-1.5 rounded',
  lg: 'text-sm px-4 py-2 rounded',
};

export function Button({
  variant = 'primary',
  size = 'md',
  loading = false,
  icon,
  children,
  className,
  disabled,
  ...rest
}: ButtonProps) {
  const isDisabled = disabled || loading;

  return (
    <button
      {...rest}
      disabled={isDisabled}
      className={[
        'inline-flex items-center gap-1.5 transition-colors cursor-pointer',
        'disabled:opacity-60 disabled:cursor-not-allowed',
        VARIANT_CLASSES[variant],
        SIZE_CLASSES[size],
        className,
      ]
        .filter(Boolean)
        .join(' ')}
    >
      {loading ? (
        <Loader2 size={14} className="animate-spin shrink-0" />
      ) : icon ? (
        <span className="shrink-0">{icon}</span>
      ) : null}
      {children}
    </button>
  );
}
