import type { ReactNode } from 'react';

/** Small uppercase section heading for grouping fields/rows inside a Panel. */
export function SectionLabel({ children, className }: { children: ReactNode; className?: string }) {
  return (
    <p
      className={`jt:text-xs jt:font-semibold jt:uppercase jt:tracking-wide jt:text-ink-muted jt:mb-2 ${className ?? ''}`.trim()}
    >
      {children}
    </p>
  );
}
