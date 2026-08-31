import type { CSSProperties, ReactNode } from 'react';

/**
 * Tailwind card shell — the Tailwind-based counterpart of the Bootstrap
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
      className={`rounded-lg border border-line-accent bg-surface/85 shadow-lg overflow-hidden ${className}`.trim()}
      style={style}
    >
      {children}
    </div>
  );
}

export function CardHeader({ children, className = '' }: { children: ReactNode; className?: string }) {
  return (
    <div className={`px-4 py-2 border-b border-line bg-panel/60 ${className}`.trim()}>{children}</div>
  );
}

export function CardTitle({ children }: { children: ReactNode }) {
  return <span className="text-sm font-semibold tracking-wide text-ink">{children}</span>;
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
    <div id={id} className={`p-4 ${className}`.trim()}>
      {children}
    </div>
  );
}
