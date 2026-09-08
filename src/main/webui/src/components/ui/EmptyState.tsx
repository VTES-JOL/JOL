import type { ComponentType, ReactNode } from 'react';

/**
 * "Nothing here yet" placeholder for detail panes, ported from jol-quarkus.
 * Tailwind Tailwind-based. Use this on pages migrated to Tailwind (the
 * Bootstrap pages that still exist inline their own empty states).
 */
interface EmptyStateProps {
  icon?: ComponentType<{ size?: number; className?: string }>;
  title: string;
  description?: string;
  action?: ReactNode;
  className?: string;
  /**
   * Add the glass panel backing (border + `bg-surface/92` + blur). Set this
   * when the empty state is rendered directly into a layout slot rather than
   * inside a Panel/Card — e.g. a bare MasterDetailView pane — so its text
   * keeps a WCAG-AA-compliant surface over the route background image instead
   * of floating on the plate.
   */
  framed?: boolean;
}

export function EmptyState({ icon: Icon, title, description, action, className, framed }: EmptyStateProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center gap-3 p-8 text-center flex-1 ${
        framed ? 'rounded-lg border border-line-accent bg-surface/92 backdrop-blur-md shadow-lg' : ''
      } ${className ?? ''}`}
    >
      {Icon && <Icon size={32} className="text-ink-muted/50" />}
      <div className="space-y-1">
        {/* Both lines use ink-secondary (not the fainter ink-muted, still
            less so at reduced alpha) so they clear WCAG AA on the translucent
            panel these render inside — hierarchy comes from size/weight. */}
        <p className="text-sm font-medium text-ink-secondary">{title}</p>
        {description && <p className="text-xs text-ink-secondary">{description}</p>}
      </div>
      {action}
    </div>
  );
}
