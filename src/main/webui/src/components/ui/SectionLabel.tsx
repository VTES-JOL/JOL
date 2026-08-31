import type { ReactNode } from 'react';

/** Small uppercase section heading for grouping fields/rows inside a Panel. */
export function SectionLabel({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <p
      className={`text-xs font-semibold uppercase tracking-wide text-ink-muted mb-2 ${className ?? ''}`.trim()}
    >
      {children}
    </p>
  );
}
