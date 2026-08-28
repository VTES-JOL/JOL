import type { ReactNode } from 'react';

/** Small status/label pill, ported from jol-quarkus. Tailwind `jt:` -prefixed. */
type BadgeVariant = 'format' | 'accent' | 'blood' | 'online' | 'muted';
type BadgeSize = 'xs' | 'sm';

interface BadgeProps {
  variant?: BadgeVariant;
  size?: BadgeSize;
  children: ReactNode;
  className?: string;
}

const VARIANT_CLASSES: Record<BadgeVariant, string> = {
  format: 'jt:bg-arcane/10 jt:border jt:border-arcane/20 jt:text-arcane-soft jt:uppercase jt:tracking-tight jt:rounded-full',
  accent: 'jt:bg-accent/15 jt:text-accent-soft jt:rounded',
  blood: 'jt:bg-blood/15 jt:text-blood-soft jt:rounded',
  online: 'jt:bg-online/15 jt:text-online jt:rounded',
  muted: 'jt:bg-hover jt:text-ink-muted jt:rounded',
};

const SIZE_CLASSES: Record<BadgeSize, string> = {
  xs: 'jt:text-[11px] jt:px-1.5 jt:py-0.5',
  sm: 'jt:text-xs jt:px-2 jt:py-0.5',
};

export function Badge({ variant = 'accent', size = 'xs', children, className }: BadgeProps) {
  return (
    <span
      className={['jt:inline-flex jt:items-center jt:font-medium', VARIANT_CLASSES[variant], SIZE_CLASSES[size], className]
        .filter(Boolean)
        .join(' ')}
    >
      {children}
    </span>
  );
}
