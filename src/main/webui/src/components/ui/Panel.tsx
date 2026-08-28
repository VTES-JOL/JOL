import type { ReactNode } from 'react';

/**
 * Framed content panel, ported from the jol-quarkus rewrite. Tailwind-based
 * (all classes `jt:` -prefixed — see styles/tailwind.css). Header row with an
 * optional right slot, scrollable body, optional footer.
 */
type PanelSize = 'default' | 'compact';

type Props = {
  title: ReactNode;
  children: ReactNode;
  right?: ReactNode;
  footer?: ReactNode;
  size?: PanelSize;
  className?: string;
};

export function Panel({ title, children, right, footer, size = 'default', className }: Props) {
  const headerPy = size === 'compact' ? 'jt:py-1.5' : 'jt:py-2';
  return (
    <div
      className={`jt:relative jt:flex jt:flex-col jt:h-full jt:min-h-0 jt:overflow-hidden jt:rounded-lg jt:border jt:border-line-accent jt:bg-surface/85 jt:backdrop-blur-md jt:shadow-lg ${className ?? ''}`}
    >
      <div
        className={`jt:flex jt:justify-between jt:items-center jt:px-4 ${headerPy} jt:border-b jt:border-line jt:bg-panel/60 jt:shrink-0`}
      >
        <h2 className="jt:tracking-wide jt:text-ink">{title}</h2>
        {right && <div>{right}</div>}
      </div>

      <div className="jt:relative jt:flex-1 jt:min-h-0 jt:flex jt:flex-col">{children}</div>

      {footer && <div className="jt:shrink-0 jt:border-t jt:border-line/75 jt:bg-panel/20">{footer}</div>}
    </div>
  );
}
