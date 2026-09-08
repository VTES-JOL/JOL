import type { ReactNode } from 'react';

/** Small status/label pill, ported from jol-quarkus. Tailwind Tailwind-based. */
export type BadgeVariant = 'format' | 'accent' | 'blood' | 'online' | 'gold' | 'muted';
type BadgeSize = 'xs' | 'sm';

interface BadgeProps {
  variant?: BadgeVariant;
  size?: BadgeSize;
  children: ReactNode;
  className?: string;
}

const VARIANT_CLASSES: Record<BadgeVariant, string> = {
  // *-label text tokens flip deep↔pale between themes — the *-soft tokens are
  // mid-tone tints and only cleared ~2:1 as chip text in dark mode.
  format: 'bg-arcane/15 border border-arcane/30 text-arcane-label uppercase tracking-tight rounded-full',
  accent: 'bg-accent/15 text-accent-soft rounded',
  blood: 'bg-blood/15 text-blood-label rounded',
  online: 'bg-online/15 text-online rounded',
  gold: 'bg-gold/15 text-gold rounded',
  muted: 'bg-hover text-ink-muted rounded',
};

const SIZE_CLASSES: Record<BadgeSize, string> = {
  xs: 'text-[11px] px-1.5 py-0.5',
  sm: 'text-xs px-2 py-0.5',
};

export function Badge({ variant = 'accent', size = 'xs', children, className }: BadgeProps) {
  return (
    <span
      className={['inline-flex items-center font-medium', VARIANT_CLASSES[variant], SIZE_CLASSES[size], className]
        .filter(Boolean)
        .join(' ')}
    >
      {children}
    </span>
  );
}
