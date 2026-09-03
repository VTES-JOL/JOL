import type { ReactNode } from 'react';

/**
 * Framed content panel, ported from the jol-quarkus rewrite. Tailwind-based
 * (all classes Tailwind-based — see styles/tailwind.css). Header row with an
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
  /**
   * Extra classes for the header row. Used by MasterDetailView panes to drop
   * the header on mobile (`hidden lg:flex`) when the view's own tab strip
   * already labels the pane — see DeckAnalyticsPanel.
   */
  headerClassName?: string;
  /** Extra classes for the title `<h2>` — e.g. `hidden lg:block` to hide a
   *  label the tab strip already shows while keeping the header's actions. */
  titleClassName?: string;
};

export function Panel({
  title,
  children,
  right,
  footer,
  size = 'default',
  className,
  headerClassName,
  titleClassName,
}: Props) {
  const headerPy = size === 'compact' ? 'py-1.5' : 'py-2';
  return (
    <div
      className={`relative flex flex-col h-full min-h-0 overflow-hidden rounded-lg border border-line-accent bg-surface/85 backdrop-blur-md shadow-lg ${className ?? ''}`}
    >
      <div
        className={`flex justify-between items-center px-4 ${headerPy} border-b border-line bg-panel/60 shrink-0 ${headerClassName ?? ''}`}
      >
        <h2 className={`tracking-wide text-ink ${titleClassName ?? ''}`}>{title}</h2>
        {right && <div>{right}</div>}
      </div>

      <div className="relative flex-1 min-h-0 flex flex-col">{children}</div>

      {footer && <div className="shrink-0 border-t border-line/75 bg-panel/20">{footer}</div>}
    </div>
  );
}
