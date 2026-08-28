import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { Loader2 } from 'lucide-react';

/**
 * Control button, ported from the jol-quarkus rewrite. Tailwind-based (all
 * classes `jt:` -prefixed — see styles/tailwind.css); does NOT use the
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
  primary: 'jt:bg-accent jt:text-surface jt:hover:bg-accent-dim',
  secondary: 'jt:border jt:border-line-accent jt:text-ink-secondary jt:hover:text-ink jt:hover:bg-hover',
  ghost: 'jt:text-ink-secondary jt:hover:text-ink jt:hover:bg-hover',
  'accent-ghost': 'jt:text-accent jt:hover:text-accent-dim jt:hover:bg-accent/10',
  danger: 'jt:border jt:border-blood/40 jt:text-blood jt:hover:bg-blood/10',
};

const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: 'jt:text-xs jt:px-2 jt:py-1 jt:rounded',
  md: 'jt:text-sm jt:px-3 jt:py-1.5 jt:rounded',
  lg: 'jt:text-sm jt:px-4 jt:py-2 jt:rounded',
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
        'jt:inline-flex jt:items-center jt:gap-1.5 jt:transition-colors jt:cursor-pointer',
        'jt:disabled:opacity-60 jt:disabled:cursor-not-allowed',
        VARIANT_CLASSES[variant],
        SIZE_CLASSES[size],
        className,
      ]
        .filter(Boolean)
        .join(' ')}
    >
      {loading ? (
        <Loader2 size={14} className="jt:animate-spin jt:shrink-0" />
      ) : icon ? (
        <span className="jt:shrink-0">{icon}</span>
      ) : null}
      {children}
    </button>
  );
}
