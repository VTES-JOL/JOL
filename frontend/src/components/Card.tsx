import type { CSSProperties, ReactNode } from 'react';

/**
 * Shared card shell for every widget on the main dashboard (and any future
 * page) — extracted after the header styling had drifted across
 * components (GlobalChat's card-header was missing bg-body-secondary and
 * the fw-semibold small title wrapper every other widget used; Resources
 * had no header row at all, just a text label inline in its body). Header
 * *content* still varies per widget (a plain title, a collapse toggle
 * button, a tab bar) — CardHeader only enforces the consistent container
 * styling, callers supply whatever's inside it.
 */
export function Card({
  children,
  className = '',
  style,
}: {
  children: ReactNode;
  className?: string;
  style?: CSSProperties;
}) {
  return (
    <div className={`card shadow ${className}`.trim()} style={style}>
      {children}
    </div>
  );
}

export function CardHeader({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <div className={`card-header bg-body-secondary ${className}`.trim()}>{children}</div>;
}

export function CardTitle({ children }: { children: ReactNode }) {
  return <span className="fw-semibold small">{children}</span>;
}
