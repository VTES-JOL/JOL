import type { CSSProperties, ReactNode } from 'react';

/**
 * Tailwind card shell — the `jt:` -prefixed counterpart of the Bootstrap
 * `components/Card.tsx`. Same `Card` / `CardHeader` / `CardTitle` split and
 * the same `children` / `className` / `style` props, so migrating a page is a
 * straight import swap. Use on pages already off Bootstrap.
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
    <div
      className={`jt:rounded-lg jt:border jt:border-line-accent jt:bg-surface/85 jt:shadow-lg jt:overflow-hidden ${className}`.trim()}
      style={style}
    >
      {children}
    </div>
  );
}

export function CardHeader({ children, className = '' }: { children: ReactNode; className?: string }) {
  return (
    <div className={`jt:px-4 jt:py-2 jt:border-b jt:border-line jt:bg-panel/60 ${className}`.trim()}>{children}</div>
  );
}

export function CardTitle({ children }: { children: ReactNode }) {
  return <span className="jt:text-sm jt:font-semibold jt:tracking-wide jt:text-ink">{children}</span>;
}

/** Standard padded body — optional convenience matching the Bootstrap `.card-body`. */
export function CardBody({
  children,
  className = '',
  id,
}: {
  children: ReactNode;
  className?: string;
  id?: string;
}) {
  return (
    <div id={id} className={`jt:p-4 ${className}`.trim()}>
      {children}
    </div>
  );
}
